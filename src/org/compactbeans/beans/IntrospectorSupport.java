/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.compactbeans.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Support methods, including the caches, for the Introspector.
 * Factored out from Introspector and Descriptor classes.
 * <p>
 * There are two caches managed here.</p>
 * <ol>
 *     <li>The <code>declaredMethodCache</code>. This is a weak mapping of class
 *     objects to the publicly declared methods. This is accessed and
 *     updated <em>during</em> introspection.</li>
 *     <li>The <code>ThreadGroupContext</code> cache. This caches entire
 *     <code>BeanInfo</code> objects. It is updated with the <em>results</em>
 *     of introspection, and queried <em>instead</em> of introspection.</li>
 * </ol>
 * <p>They are both synchronized on the same mutex object.</p>
 */
final class IntrospectorSupport {

    // Static Cache to speed up introspection.
    private static final WeakCache<Class<?>, Method[]> declaredMethodCache =
            new WeakCache<Class<?>, Method[]>();

    static final String ADD_PREFIX = "add";
    static final String REMOVE_PREFIX = "remove";
    static final String GET_PREFIX = "get";
    static final String SET_PREFIX = "set";
    static final String IS_PREFIX = "is";

    static final Class<?> eventListenerType = EventListener.class;

    private IntrospectorSupport() {
    }

    //======================================================================
    // Caching and cache management.
    //======================================================================

    /**
     * A cache of BeanInfo objects previously built by the introspector.
     * Note that this only makes sense when the BeanInfo is fully populated,
     * ie. when there is no <code>stopClass</code> that prematurely
     * terminates the introspector search.
     *
     * @param beanClass the previously cached bean class
     * @return the BeanInfo cached from previous introspection, or
     * <code>null</code> if there is no previously cached bean info
     */
    static BeanInfo getCachedBeanInfo(Class<?> beanClass) {
        ThreadGroupContext context = ThreadGroupContext.getContext();
        BeanInfo beanInfo;

        synchronized (declaredMethodCache) {
            beanInfo = context.getBeanInfo(beanClass);
        }
        return beanInfo;
    }

    /**
     * A cache of BeanInfo objects previously built by the introspector.
     * Note that this only makes sense when the BeanInfo is fully populated,
     * ie. when there is no <code>stopClass</code> that prematurely
     * terminates the introspector search.
     *
     * @param beanClass the introspected bean class
     * @param beanInfo the BeanInfo data to be cached
     */
    static void putCachedBeanInfo(Class<?> beanClass, BeanInfo beanInfo) {
        ThreadGroupContext context = ThreadGroupContext.getContext();

        synchronized (declaredMethodCache) {
            context.putBeanInfo(beanClass, beanInfo);
        }
    }

    /**
     * Flush all of the Introspector's internal caches.  This method is
     * not normally required.  It is normally only needed by advanced
     * tools that update existing "Class" objects in-place and need
     * to make the Introspector re-analyze existing Class objects.
     */
    static void flushCaches() {
        synchronized (declaredMethodCache) {
            ThreadGroupContext.getContext().clearBeanInfoCache();
            declaredMethodCache.clear();
        }
    }

    /**
     * Flush the Introspector's internal cached information for a given class.
     * This method is not normally required.  It is normally only needed
     * by advanced tools that update existing "Class" objects in-place
     * and need to make the Introspector re-analyze an existing Class object.
     *
     * <p>Note that only the direct state associated with the target Class
     * object is flushed.  We do not flush state for other Class objects
     * with the same name, nor do we flush state for any related Class
     * objects (such as subclasses), even though their state may include
     * information indirectly obtained from the target Class object.</p>
     *
     * @param clz Class object to be flushed.
     * @throws NullPointerException If the Class object is <code>null</code>.
     */
    static void flushFromCaches(Class<?> clz) {
        if (clz == null) {
            throw new NullPointerException();
        }
        synchronized (declaredMethodCache) {
            ThreadGroupContext.getContext().removeBeanInfo(clz);
            declaredMethodCache.put(clz, null);
        }
    }

    /*
     * Internal method to return *public* methods within a class.
     */
    static Method[] getPublicDeclaredMethods(Class<?> clz) {
        // Looking up Class.getDeclaredMethods is relatively expensive,
        // so we cache the results.
        if (!ReflectUtil.isPackageAccessible(clz)) {
            return new Method[0];
        }
        synchronized (declaredMethodCache) {
            Method[] result = declaredMethodCache.get(clz);
            if (result == null) {
                result = clz.getMethods();
                for (int i = 0; i < result.length; i++) {
                    Method method = result[i];
                    if (!method.getDeclaringClass().equals(clz)) {
                        result[i] = null;
                    }
                }
                declaredMethodCache.put(clz, result);
            }
            return result;
        }
    }

    //======================================================================
    // Package private support methods.
    //======================================================================

    /**
     * Populates the property descriptor table by merging the
     * lists of Property descriptors.
     */
    static Map<String, PropertyDescriptor> processPropertyDescriptors(Collection<List<PropertyDescriptor>> propertyDescriptors) {
        List<PropertyDescriptor> list;
        PropertyDescriptor pd, gpd, spd;
        IndexedPropertyDescriptor ipd, igpd, ispd;

        // properties maps from String names to PropertyDescriptors
        // Map implicitly ordered by property name
        Map<String, PropertyDescriptor> properties = new TreeMap<String, PropertyDescriptor>();

        Iterator<List<PropertyDescriptor>> it = propertyDescriptors.iterator();
        while (it.hasNext()) {
            pd = null; gpd = null; spd = null;
            ipd = null; igpd = null; ispd = null;

            list = it.next();

            // First pass. Find the latest getter method. Merge properties
            // of previous getter methods.
            for (int i = 0; i < list.size(); i++) {
                pd = list.get(i);
                if (pd instanceof IndexedPropertyDescriptor) {
                    ipd = (IndexedPropertyDescriptor) pd;
                    if (ipd.getIndexedReadMethod() != null) {
                        if (igpd != null) {
                            igpd = new IndexedPropertyDescriptor(igpd, ipd);
                        } else {
                            igpd = ipd;
                        }
                    }
                } else {
                    if (pd.getReadMethod() != null) {
                        if (gpd != null) {
                            // Don't replace the existing read
                            // method if it starts with "is"
                            Method method = gpd.getReadMethod();
                            if (!method.getName().startsWith(IntrospectorSupport.IS_PREFIX)) {
                                gpd = new PropertyDescriptor(gpd, pd);
                            }
                        } else {
                            gpd = pd;
                        }
                    }
                }
            }

            // Second pass. Find the latest setter method which
            // has the same type as the getter method.
            for (int i = 0; i < list.size(); i++) {
                pd = list.get(i);
                if (pd instanceof IndexedPropertyDescriptor) {
                    ipd = (IndexedPropertyDescriptor) pd;
                    if (ipd.getIndexedWriteMethod() != null) {
                        if (igpd != null) {
                            if (igpd.getIndexedPropertyType()
                                    == ipd.getIndexedPropertyType()) {
                                if (ispd != null) {
                                    ispd = new IndexedPropertyDescriptor(ispd, ipd);
                                } else {
                                    ispd = ipd;
                                }
                            }
                        } else {
                            if (ispd != null) {
                                ispd = new IndexedPropertyDescriptor(ispd, ipd);
                            } else {
                                ispd = ipd;
                            }
                        }
                    }
                } else {
                    if (pd.getWriteMethod() != null) {
                        if (gpd != null) {
                            if (gpd.getPropertyType() == pd.getPropertyType()) {
                                if (spd != null) {
                                    spd = new PropertyDescriptor(spd, pd);
                                } else {
                                    spd = pd;
                                }
                            }
                        } else {
                            if (spd != null) {
                                spd = new PropertyDescriptor(spd, pd);
                            } else {
                                spd = pd;
                            }
                        }
                    }
                }
            }

            // At this stage we should have either PDs or IPDs for the
            // representative getters and setters. The order in which the
            // property descriptors are determined represent the
            // precedence of the property ordering.
            pd = null; ipd = null;

            if ((igpd != null) && (ispd != null)) {
                // Complete indexed properties set
                // Merge any classic property descriptors
                if (gpd != null) {
                    PropertyDescriptor tpd = mergePropertyDescriptorWithIndexed(igpd, gpd);
                    if (tpd instanceof IndexedPropertyDescriptor) {
                        igpd = (IndexedPropertyDescriptor) tpd;
                    }
                }
                if (spd != null) {
                    PropertyDescriptor tpd = mergePropertyDescriptorWithIndexed(ispd, spd);
                    if (tpd instanceof IndexedPropertyDescriptor) {
                        ispd = (IndexedPropertyDescriptor) tpd;
                    }
                }
                if (igpd == ispd) {
                    pd = igpd;
                } else {
                    pd = mergeIndexedPropertyDescriptors(igpd, ispd);
                }
            } else if ((gpd != null) && (spd != null)) {
                // Complete simple properties set
                if (gpd == spd) {
                    pd = gpd;
                } else {
                    pd = mergePropertyDescriptors(gpd, spd);
                }
            } else if (ispd != null) {
                // indexed setter
                pd = ispd;
                // Merge any classic property descriptors
                if (spd != null) {
                    pd = mergePropertyDescriptorWithIndexed(ispd, spd);
                }
                if (gpd != null) {
                    pd = mergePropertyDescriptorWithIndexed(ispd, gpd);
                }
            } else if (igpd != null) {
                // indexed getter
                pd = igpd;
                // Merge any classic property descriptors
                if (gpd != null) {
                    pd = mergePropertyDescriptorWithIndexed(igpd, gpd);
                }
                if (spd != null) {
                    pd = mergePropertyDescriptorWithIndexed(igpd, spd);
                }
            } else if (spd != null) {
                // simple setter
                pd = spd;
            } else if (gpd != null) {
                // simple getter
                pd = gpd;
            }

            // Very special case to ensure that an IndexedPropertyDescriptor
            // doesn't contain less information than the enclosed
            // PropertyDescriptor. If it does, then recreate as a
            // PropertyDescriptor. See 4168833
            if (pd instanceof IndexedPropertyDescriptor) {
                ipd = (IndexedPropertyDescriptor) pd;
                if ((ipd.getIndexedReadMethod() == null) && (ipd.getIndexedWriteMethod() == null)) {
                    pd = new PropertyDescriptor(ipd);
                }
            }

            // Find the first property descriptor
            // which does not have getter and setter methods.
            // See regression bug 4984912.
            if ((pd == null) && (!list.isEmpty())) {
                pd = list.get(0);
            }

            if (pd != null) {
                properties.put(pd.getName(), pd);
            }
        }

        return properties;
    }

    /**
     * Adds the property descriptor to the indexed property descriptor
     * only if the types are the same.
     *
     * <p>The most specific property descriptor will take precedence.</p>
     */
    private static PropertyDescriptor mergePropertyDescriptorWithIndexed(
            IndexedPropertyDescriptor ipd, PropertyDescriptor pd) {
        PropertyDescriptor result = null;

        Class<?> propType = pd.getPropertyType();
        Class<?> ipropType = ipd.getIndexedPropertyType();

        if (propType.isArray() && (propType.getComponentType() == ipropType)) {
            if (pd.getClass0().isAssignableFrom(ipd.getClass0())) {
                result = new IndexedPropertyDescriptor(pd, ipd);
            } else {
                result = new IndexedPropertyDescriptor(ipd, pd);
            }
        } else {
            // Cannot merge the pd because of type mismatch
            // Return the most specific pd
            if (pd.getClass0().isAssignableFrom(ipd.getClass0())) {
                result = ipd;
            } else {
                result = pd;
                // Try to add methods which may have been lost in the type change
                // See 4168833
                Method write = result.getWriteMethod();
                Method read = result.getReadMethod();

                if ((read == null) && (write != null)) {
                    read = IntrospectorSupport.findMethod(result.getClass0(),
                            IntrospectorSupport.GET_PREFIX + NameGenerator.capitalize(result.getName()), 0);
                    if (read != null) {
                        try {
                            result.setReadMethod(read);
                        } catch (IntrospectionException ex) {
                            // no consequences for failure.
                        }
                    }
                }
                if ((write == null) && (read != null)) {
                    write = IntrospectorSupport.findMethod(result.getClass0(),
                            IntrospectorSupport.SET_PREFIX + NameGenerator.capitalize(result.getName()), 1,
                            new Class<?>[]{read.getReturnType()});
                    if (write != null) {
                        try {
                            result.setWriteMethod(write);
                        } catch (IntrospectionException ex) {
                            // no consequences for failure.
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Handle regular property descriptor merge.
     */
    private static PropertyDescriptor mergePropertyDescriptors(
            PropertyDescriptor pd1, PropertyDescriptor pd2) {
        if (pd1.getClass0().isAssignableFrom(pd2.getClass0())) {
            return new PropertyDescriptor(pd1, pd2);
        } else {
            return new PropertyDescriptor(pd2, pd1);
        }
    }

    /**
     * Handle regular indexed property descriptor merge.
     */
    private static PropertyDescriptor mergeIndexedPropertyDescriptors(
            IndexedPropertyDescriptor ipd1, IndexedPropertyDescriptor ipd2) {
        if (ipd1.getClass0().isAssignableFrom(ipd2.getClass0())) {
            return new IndexedPropertyDescriptor(ipd1, ipd2);
        } else {
            return new IndexedPropertyDescriptor(ipd2, ipd1);
        }
    }

    /**
     * Internal support for finding a target methodName with a given
     * parameter list on a given class.
     */
    private static Method internalFindMethod(Class<?> start, String methodName,
                                             int argCount, Class<?> args[]) {
        // For overridden methods we need to find the most derived version.
        // So we start with the given class and walk up the superclass chain.

        Method method = null;

        for (Class<?> cl = start; cl != null; cl = cl.getSuperclass()) {
            Method methods[] = getPublicDeclaredMethods(cl);
            for (int i = 0; i < methods.length; i++) {
                method = methods[i];
                if (method == null) {
                    continue;
                }

                // make sure method signature matches.
                if (method.getName().equals(methodName)) {
                    Type[] params = method.getGenericParameterTypes();
                    if (params.length == argCount) {
                        if (args != null) {
                            boolean different = false;
                            if (argCount > 0) {
                                for (int j = 0; j < argCount; j++) {
                                    if (TypeResolver.erase(TypeResolver.resolveInClass(start, params[j])) != args[j]) {
                                        different = true;
                                        break;
                                    }
                                }
                                if (different) {
                                    continue;
                                }
                            }
                        }
                        return method;
                    }
                }
            }
        }
        method = null;

        // Now check any inherited interfaces.  This is necessary both when
        // the argument class is itself an interface, and when the argument
        // class is an abstract class.
        Class<?> ifcs[] = start.getInterfaces();
        for (int i = 0; i < ifcs.length; i++) {
            // Note: The original implementation had both methods calling
            // the 3 arg method. This is preserved but perhaps it should
            // pass the args array instead of null.
            method = internalFindMethod(ifcs[i], methodName, argCount, null);
            if (method != null) {
                break;
            }
        }
        return method;
    }

    /**
     * Find a target methodName on a given class.
     */
    static Method findMethod(Class<?> cls, String methodName, int argCount) {
        if (methodName == null) {
            return null;
        }
        return internalFindMethod(cls, methodName, argCount, null);
    }

    /**
     * Find a target methodName with specific parameter list on a given class.
     *
     * <p>Used in the constructors of the EventSetDescriptor,
     * PropertyDescriptor and the IndexedPropertyDescriptor.</p>
     *
     * @param cls The Class object on which to retrieve the method.
     * @param methodName Name of the method.
     * @param argCount Number of arguments for the desired method.
     * @param args Array of argument types for the method.
     * @return the method or null if not found
     */
    static Method findMethod(Class<?> cls, String methodName, int argCount,
                             Class<?> args[]) {
        if (methodName == null) {
            return null;
        }
        return internalFindMethod(cls, methodName, argCount, args);
    }

    /**
     * Return true if class a is either equivalent to class b, or
     * if class a is a subclass of class b, i.e. if a either "extends"
     * or "implements" b.
     * <p>
     * Note tht either or both "Class" objects may represent interfaces.</p>
     */
    static boolean isSubclass(Class<?> a, Class<?> b) {
        // We rely on the fact that for any given java class or
        // primitive type there is a unique Class object, so
        // we can use object equivalence in the comparisons.
        if (a == b) {
            return true;
        }
        if ((a == null) || (b == null)) {
            return false;
        }
        for (Class<?> x = a; x != null; x = x.getSuperclass()) {
            if (x == b) {
                return true;
            }
            if (b.isInterface()) {
                Class<?> interfaces[] = x.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    if (isSubclass(interfaces[i], b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return true iff the given method throws the given exception.
     */
    static boolean throwsException(Method method, Class<?> exception) {
        Class<?> exs[] = method.getExceptionTypes();
        for (int i = 0; i < exs.length; i++) {
            if (exs[i] == exception) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves the return type of the method.
     *
     * @param base the class that contains the method in the hierarchy
     * @param method the object that represents the method
     * @return a class identifying the return type of the method
     *
     * @see Method#getGenericReturnType
     * @see Method#getReturnType
     */
    static Class<?> getReturnType(Class<?> base, Method method) {
        if (base == null) {
            base = method.getDeclaringClass();
        }
        return TypeResolver.erase(TypeResolver.resolveInClass(base, method.getGenericReturnType()));
    }

    /**
     * Resolves the parameter types of the method.
     *
     * @param base the class that contains the method in the hierarchy
     * @param method the object that represents the method
     * @return an array of classes identifying the parameter types of the method
     *
     * @see Method#getGenericParameterTypes
     * @see Method#getParameterTypes
     */
    static Class<?>[] getParameterTypes(Class<?> base, Method method) {
        if (base == null) {
            base = method.getDeclaringClass();
        }
        return TypeResolver.erase(TypeResolver.resolveInClass(base, method.getGenericParameterTypes()));
    }

    /**
     * Get the classname portion of the given class, excluding the
     * package name.
     *
     * @param cls the class
     * @return the class name portion, or <code>null</code> if cls
     * is <code>null</code>
     */
    static String getClassName(Class<?> cls) {
        if(cls == null) {
            return null;
        }
        String className = cls.getName();
        int i = className.lastIndexOf('.');
        if(i > 0) {
            return className.substring(i + 1);
        } else {
            return className;
        }
    }

    static boolean isEventHandler(Class<?> beanClass, Method m) {
        // We assume that a method is an event handler if it has a single
        // argument, whose type inherit from java.util.Event.
        Type argTypes[] = m.getGenericParameterTypes();
        if (argTypes.length != 1) {
            return false;
        }
        return isSubclass(TypeResolver.erase(TypeResolver.resolveInClass(beanClass, argTypes[0])), EventObject.class);
    }

    /**
     * Package private helper method for Descriptor .equals methods.
     *
     * @param a first method to compare
     * @param b second method to compare
     * @return boolean to indicate that the methods are equivalent
     */
    static boolean compareMethods(Method a, Method b) {
        // Note: perhaps this should be a protected method in FeatureDescriptor
        if ((a == null) != (b == null)) {
            return false;
        }

        if ((a != null) && (b != null)) {
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }
}
