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
import java.lang.reflect.Modifier;

import java.lang.reflect.Type;
import java.util.*;

/**
 * The Introspector class provides a standard way for tools to learn about
 * the properties, events, and methods supported by a target Java Bean.
 *
 * <p>For each of these kinds of information, the Introspector will
 * separately analyze the bean's class and superclasses looking for
 * either explicit or implicit information and use that information to
 * build a BeanInfo object that comprehensively describes the target bean.</p>
 *
 * <p>We use low-level reflection to study the methods of the class and
 * apply standard design patterns to identify property accessors,
 * event sources, or public methods.  We then proceed to analyze the class's
 * superclass and add in the information from it (and possibly on up the
 * superclass chain).</p>
 *
 * <p>Because the Introspector caches BeanInfo classes for better performance,
 * take care if you use it in an application that uses
 * multiple class loaders.</p>
 *
 * <p>For more information about introspection and design patterns, please
 * consult the
 * <a href="http://java.sun.com/products/javabeans/docs/index.html">JavaBeans
 * specification</a>.</p>
 */
public final class Introspector {

    private final static EventSetDescriptor[] EMPTY_EVENTSETDESCRIPTORS = new EventSetDescriptor[0];

    //======================================================================
    // 				Public methods
    //======================================================================

    /**
     * Introspect on a Java Bean and learn about all its properties, exposed
     * methods, and events.
     *
     * <p>If the BeanInfo class for a Java Bean has been previously
     * Introspected then the BeanInfo class is retrieved from the BeanInfo
     * cache.</p>
     *
     * @param beanClass The bean class to be analyzed.
     * @return A BeanInfo object describing the target bean.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass)
            throws IntrospectionException {
        return getBeanInfo(beanClass, null, true);
    }

    /**
     * Introspect on a Java Bean and learn about all its properties, exposed
     * methods, and events.
     *
     * <p>If the BeanInfo class for a Java Bean has been previously
     * Introspected then the BeanInfo class is retrieved from the BeanInfo
     * cache.</p>
     *
     * @param beanClass The bean class to be analyzed.
     * @param stopClass The base class at which to stop the analysis.  Any
     *    methods/properties/events in the stopClass or in its base classes
     *    will be ignored in the analysis.
     * @return A BeanInfo object describing the target bean.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass)
            throws IntrospectionException {
        boolean useAllInfo = (stopClass == null);

        // Check stopClass is a superClass of startClass.
        if (!useAllInfo) {
            boolean isSuper = false;
            for (Class c = beanClass.getSuperclass(); c != null; c = c.getSuperclass()) {
                if (c == stopClass) {
                    isSuper = true;
                    break;
                }
            }
            if (!isSuper) {
                throw new IntrospectionException(stopClass.getName() + " not superclass of " +
                                        beanClass.getName());
            }
        }

        return getBeanInfo(beanClass, stopClass, useAllInfo);
    }

    /**
     * Introspect on a Java Bean and learn about all its properties,
     * exposed methods and events, below a given {@code stopClass} point
     * subject to the <code>useAllInfo</code> flag.
     * <p>
     * Any methods/properties/events in the {@code stopClass}
     * or in its parent classes will be ignored in the analysis.</p>
     * <p>
     * If the BeanInfo class for a Java Bean has been
     * previously introspected based on the same arguments then
     * the BeanInfo class is retrieved from the BeanInfo cache.</p>
     *
     * @param beanClass the bean class to be analyzed
     * @param stopClass the parent class at which to stop the analysis
     * @param useAllInfo <code>true</code> to indicate all BeanInfo that can
     *     be discovered should be used, otherwise
     *     <code>false</code> to indicate there is a stopping point
     * @return a BeanInfo object describing the target bean
     * @throws IntrospectionException if an exception occurs during introspection
     */
    private static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass,
            boolean useAllInfo) throws IntrospectionException {
        BeanInfo superBeanInfo;
        Class superClass = beanClass.getSuperclass();
        BeanInfo beanInfo;

        if(useAllInfo) {
            beanInfo = IntrospectorSupport.getCachedBeanInfo(beanClass);
            if(beanInfo != null) {
                return beanInfo;
            }
        }

        if ((superClass != null) && (superClass != stopClass)) {
            superBeanInfo = getBeanInfo(superClass, stopClass, useAllInfo);
        } else {
            superBeanInfo = null;
        }

        Introspector introspector = new Introspector(beanClass, superBeanInfo);

        BeanDescriptor bd = introspector.getTargetBeanDescriptor();
        MethodDescriptor mds[] = introspector.getTargetMethodInfo();
        EventSetDescriptor esds[] = introspector.getTargetEventInfo();

        PDStore pdStore = introspector.getTargetPropertyInfo();
        Map properties = processPropertyDescriptors(pdStore);

        // Allocate and populate the result array.
        PropertyDescriptor pds[] = new PropertyDescriptor[properties.size()];
        pds = (PropertyDescriptor[]) properties.values().toArray(pds);

        beanInfo = new GenericBeanInfo(bd, esds, pds, mds);

        if(useAllInfo) {
            IntrospectorSupport.putCachedBeanInfo(beanClass, beanInfo);
        }

        return beanInfo;
    }

    /**
     * Flush all of the Introspector's internal caches.  This method is
     * not normally required.  It is normally only needed by advanced
     * tools that update existing "Class" objects in-place and need
     * to make the Introspector re-analyze existing Class objects.
     */

    public static void flushCaches() {
        IntrospectorSupport.flushCaches();
    }

    /**
     * Flush the Introspector's internal cached information for a given class.
     * This method is not normally required.  It is normally only needed
     * by advanced tools that update existing "Class" objects in-place
     * and need to make the Introspector re-analyze an existing Class object.
     *
     * Note that only the direct state associated with the target Class
     * object is flushed.  We do not flush state for other Class objects
     * with the same name, nor do we flush state for any related Class
     * objects (such as subclasses), even though their state may include
     * information indirectly obtained from the target Class object.
     *
     * @param clz Class object to be flushed
     * @throws NullPointerException If the Class object is null
     */
    public static void flushFromCaches(Class<?> clz) {
        IntrospectorSupport.flushFromCaches(clz);
    }

    //======================================================================
    // 			Private implementation methods
    //======================================================================

    private final Class beanClass;
    private final BeanInfo superBeanInfo;

    private Introspector(Class beanClass, BeanInfo superBeanInfo) {
        this.beanClass = beanClass;
        this.superBeanInfo = superBeanInfo;
    }

    //======================================================================
    // 			Create bean descriptor
    //======================================================================

    private BeanDescriptor getTargetBeanDescriptor() {
        // Fabricate a default BeanDescriptor.
        return new BeanDescriptor(this.beanClass);
    }

    //======================================================================
    // 			Create property descriptors
    //======================================================================

    /**
     * @return An array of PropertyDescriptors describing the
     * properties supported by the target bean.
     */
    private PDStore getTargetPropertyInfo() {
        // Map of property names to a list of property descriptors
        PDStore pdStore = new PDStore();

        if (superBeanInfo != null) {
            // We have no explicit BeanInfo properties.  Check with our parent.
            PropertyDescriptor supers[] = superBeanInfo.getPropertyDescriptors();
            for (int i = 0; i < supers.length; i++) {
                pdStore.addPropertyDescriptor(supers[i]);
            }
        }

        // Apply some reflection to the current class.

        // First get an array of all the public methods at this level
        Method methodList[] = IntrospectorSupport.getPublicDeclaredMethods(beanClass);

        // Now analyze each method.
        for (int i = 0; i < methodList.length; i++) {
            Method method = methodList[i];
            if (method == null) {
                continue;
            }
            // skip static methods.
            int mods = method.getModifiers();
            if (Modifier.isStatic(mods)) {
                continue;
            }
            String name = method.getName();
            Class argTypes[] = method.getParameterTypes();
            Class resultType = method.getReturnType();
            int argCount = argTypes.length;
            PropertyDescriptor pd = null;

            if ((name.length() <= 3) && !name.startsWith(IntrospectorSupport.IS_PREFIX)) {
                // Optimization. Don't bother with invalid propertyNames.
                continue;
            }

            try {
                if (argCount == 0) {
                    if (name.startsWith(IntrospectorSupport.GET_PREFIX)) {
                        // Simple getter
                        pd = new PropertyDescriptor(NameGenerator.decapitalize(name.substring(3)),
                                method, null);
                    } else if ((resultType == boolean.class) && name.startsWith(IntrospectorSupport.IS_PREFIX)) {
                        // Boolean getter
                        pd = new PropertyDescriptor(NameGenerator.decapitalize(name.substring(2)),
                                method, null);
                    }
                } else if (argCount == 1) {
                    if ((argTypes[0] == int.class) && name.startsWith(IntrospectorSupport.GET_PREFIX)) {
                        pd = new IndexedPropertyDescriptor(
                                NameGenerator.decapitalize(name.substring(3)),
                                null, null,
                                method, null);
                    } else if ((resultType == void.class) && name.startsWith(IntrospectorSupport.SET_PREFIX)) {
                        // Simple setter
                        pd = new PropertyDescriptor(NameGenerator.decapitalize(name.substring(3)),
                                null, method);
                    }
                } else if (argCount == 2) {
                    if ((argTypes[0] == int.class) && name.startsWith(IntrospectorSupport.SET_PREFIX)) {
                        pd = new IndexedPropertyDescriptor(
                                NameGenerator.decapitalize(name.substring(3)),
                                null, null,
                                null, method);
                    }
                }
            } catch (IntrospectionException ex) {
                // This happens if a PropertyDescriptor or IndexedPropertyDescriptor
                // constructor finds that the method violates details of the design
                // pattern, e.g. by having an empty name, or a getter returning
                // void , or whatever.
                pd = null;
            }

            if (pd != null) {
                pdStore.addPropertyDescriptor(pd);
            }
        }

        return pdStore;
    }

    /**
     * Populates the property descriptor table by merging the
     * lists of Property descriptors.
     */
    private static Map processPropertyDescriptors(PDStore pdStore) {
        List list;
        PropertyDescriptor pd, gpd, spd;
        IndexedPropertyDescriptor ipd, igpd, ispd;

        Collection propertyDescriptors = pdStore.getPropertyDescriptors();

        // properties maps from String names to PropertyDescriptors
        // Map implicitly ordered by property name
        Map properties = new TreeMap();

        Iterator it = propertyDescriptors.iterator();
        while (it.hasNext()) {
            pd = null; gpd = null; spd = null;
            ipd = null; igpd = null; ispd = null;

            list = (List) it.next();

            // First pass. Find the latest getter method. Merge properties
            // of previous getter methods.
            for (int i = 0; i < list.size(); i++) {
                pd = (PropertyDescriptor) list.get(i);
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
                pd = (PropertyDescriptor) list.get(i);
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
                pd = (PropertyDescriptor) list.get(0);
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

        Class propType = pd.getPropertyType();
        Class ipropType = ipd.getIndexedPropertyType();

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
                            new Class[]{read.getReturnType()});
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


    //======================================================================
    // 			Create method descriptors
    //======================================================================

    /**
     * @return An array of MethodDescriptors describing the private
     * methods supported by the target bean.
     */
    private MethodDescriptor[] getTargetMethodInfo() {
        // Methods maps from Method objects to MethodDescriptors
        Map methods = new HashMap(60);

        if (superBeanInfo != null) {
            // We have no explicit BeanInfo methods.  Check with our parent.
            MethodDescriptor supers[] = superBeanInfo.getMethodDescriptors();
            for (int i = 0; i < supers.length; i++) {
                addMethod(methods, supers[i]);
            }
        }

        // Apply some reflection to the current class.

        // First get an array of all the beans methods at this level
        Method methodList[] = IntrospectorSupport.getPublicDeclaredMethods(beanClass);

        // Now analyze each method.
        for (int i = 0; i < methodList.length; i++) {
            Method method = methodList[i];
            if (method != null) {
                MethodDescriptor md = new MethodDescriptor(method);
                addMethod(methods, md);
            }
        }

        // Allocate and populate the result array.
        MethodDescriptor result[] = new MethodDescriptor[methods.size()];
        result = (MethodDescriptor[]) methods.values().toArray(result);

        return result;
    }

    private static void addMethod(Map methods, MethodDescriptor md) {
        // We have to be careful here to distinguish method by both name
        // and argument lists.
        // This method gets called a *lot*, so we try to be efficient.
        String name = md.getName();

        MethodDescriptor old = (MethodDescriptor) methods.get(name);
        if (old == null) {
            // This is the common case.
            methods.put(name, md);
            return;
        }

        // We have a collision on method names.  This is rare.

        // Check if old and md have the same type.
        String[] p1 = md.getParamNames();
        String[] p2 = old.getParamNames();

        boolean match = false;
        if (p1.length == p2.length) {
            match = true;
            for (int i = 0; i < p1.length; i++) {
                if (p1[i] != p2[i]) {
                    match = false;
                    break;
                }
            }
        }
        if (match) {
            MethodDescriptor composite = new MethodDescriptor(old, md);
            methods.put(name, composite);
            return;
        }

        // We have a collision on method names with different type signatures.
        // This is very rare.

        String longKey = makeQualifiedMethodName(name, p1);
        old = (MethodDescriptor) methods.get(longKey);
        if (old == null) {
            methods.put(longKey, md);
            return;
        }
        MethodDescriptor composite = new MethodDescriptor(old, md);
        methods.put(longKey, composite);
    }

    /**
     * Creates a key for a method in a method cache.
     */
    private static String makeQualifiedMethodName(String name, String[] params) {
        StringBuilder sb = new StringBuilder(name);
        sb.append('=');
        for (int i = 0; i < params.length; i++) {
            sb.append(':');
            sb.append(params[i]);
        }
        return sb.toString();
    }

    //======================================================================
    // 			Create event descriptors
    //======================================================================

    /**
     * @return An array of EventSetDescriptors describing the kinds of
     * events fired by the target bean.
     */
    private EventSetDescriptor[] getTargetEventInfo() throws IntrospectionException {
        Map events = new HashMap();

        // Check if the bean has its own BeanInfo that will provide
        // explicit information.
        EventSetDescriptor[] explicitEvents = null;

        if (superBeanInfo != null) {
            // We have no explicit BeanInfo events.  Check with our parent.
            EventSetDescriptor supers[] = superBeanInfo.getEventSetDescriptors();
            for (int i = 0 ; i < supers.length; i++) {
                addEvent(events, supers[i]);
            }
        }

        {
            // Apply some reflection to the current class.

            // Get an array of all the public beans methods at this level
            Method methodList[] = IntrospectorSupport.getPublicDeclaredMethods(beanClass);

            // Find all suitable "add", "remove" and "get" Listener methods
            // The name of the listener type is the key for these hashtables
            // i.e, ActionListener
            Map adds = null;
            Map removes = null;
            Map gets = null;

            for (int i = 0; i < methodList.length; i++) {
                Method method = methodList[i];
                if (method == null) {
                    continue;
                }
                // skip static methods.
                int mods = method.getModifiers();
                if (Modifier.isStatic(mods)) {
                    continue;
                }
                String name = method.getName();
                // Optimization avoid getParameterTypes
                if (!name.startsWith(IntrospectorSupport.ADD_PREFIX)
                        && !name.startsWith(IntrospectorSupport.REMOVE_PREFIX)
                        && !name.startsWith(IntrospectorSupport.GET_PREFIX)) {
                    continue;
                }

                if (name.startsWith(IntrospectorSupport.ADD_PREFIX)) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == void.class) {
                        Type[] parameterTypes = method.getGenericParameterTypes();
                        if (parameterTypes.length == 1) {
                            Class<?> type = TypeResolver.erase(TypeResolver.resolveInClass(beanClass, parameterTypes[0]));
                            if (IntrospectorSupport.isSubclass(type, IntrospectorSupport.eventListenerType)) {
                                String listenerName = name.substring(3);
                                if (listenerName.length() > 0 &&
                                        type.getName().endsWith(listenerName)) {
                                    if (adds == null) {
                                        adds = new HashMap();
                                    }
                                    adds.put(listenerName, method);
                                }
                            }
                        }
                    }
                }
                else if (name.startsWith(IntrospectorSupport.REMOVE_PREFIX)) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == void.class) {
                        Type[] parameterTypes = method.getGenericParameterTypes();
                        if (parameterTypes.length == 1) {
                            Class<?> type = TypeResolver.erase(TypeResolver.resolveInClass(beanClass, parameterTypes[0]));
                            if (IntrospectorSupport.isSubclass(type, IntrospectorSupport.eventListenerType)) {
                                String listenerName = name.substring(6);
                                if (listenerName.length() > 0 &&
                                        type.getName().endsWith(listenerName)) {
                                    if (removes == null) {
                                        removes = new HashMap();
                                    }
                                    removes.put(listenerName, method);
                                }
                            }
                        }
                    }
                }
                else if (name.startsWith(IntrospectorSupport.GET_PREFIX)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 0) {
                        Class<?> returnType = IntrospectorSupport.getReturnType(beanClass, method);
                        if (returnType.isArray()) {
                            Class<?> type = returnType.getComponentType();
                            if (IntrospectorSupport.isSubclass(type, IntrospectorSupport.eventListenerType)) {
                                String listenerName  = name.substring(3, name.length() - 1);
                                if (listenerName.length() > 0 &&
                                        type.getName().endsWith(listenerName)) {
                                    if (gets == null) {
                                        gets = new HashMap();
                                    }
                                    gets.put(listenerName, method);
                                }
                            }
                        }
                    }
                }
            }

            if ((adds != null) && (removes != null)) {
                // Now look for matching addFooListener+removeFooListener pairs.
                // Bonus if there is a matching getFooListeners method as well.
                Iterator keys = adds.keySet().iterator();
                while (keys.hasNext()) {
                    String listenerName = (String) keys.next();
                    // Skip any "add" which doesn't have a matching "remove" or
                    // a listener name that doesn't end with Listener
                    if (removes.get(listenerName) == null || !listenerName.endsWith("Listener")) {
                        continue;
                    }
                    String eventName = NameGenerator.decapitalize(listenerName.substring(0, listenerName.length()-8));
                    Method addMethod = (Method)adds.get(listenerName);
                    Method removeMethod = (Method)removes.get(listenerName);
                    Method getMethod = null;
                    if (gets != null) {
                        getMethod = (Method)gets.get(listenerName);
                    }
                    Class argType = IntrospectorSupport.getParameterTypes(beanClass, addMethod)[0];

                    // generate a list of Method objects for each of the target methods:
                    Method allMethods[] = IntrospectorSupport.getPublicDeclaredMethods(argType);
                    List validMethods = new ArrayList(allMethods.length);
                    for (int i = 0; i < allMethods.length; i++) {
                        if (allMethods[i] == null) {
                            continue;
                        }

                        if (IntrospectorSupport.isEventHandler(beanClass, allMethods[i])) {
                            validMethods.add(allMethods[i]);
                        }
                    }
                    Method[] methods = (Method[])validMethods.toArray(new Method[validMethods.size()]);

                    EventSetDescriptor esd = new EventSetDescriptor(eventName, argType,
                            methods, addMethod,
                            removeMethod,
                            getMethod);

                    // If the adder method throws the TooManyListenersException then it
                    // is a Unicast event source.
                    if (IntrospectorSupport.throwsException(addMethod,
                            java.util.TooManyListenersException.class)) {
                        esd.setUnicast(true);
                    }
                    addEvent(events, esd);
                }
            } // if (adds != null ...
        }
        EventSetDescriptor[] result;
        if (events.size() == 0) {
            result = EMPTY_EVENTSETDESCRIPTORS;
        } else {
            // Allocate and populate the result array.
            result = new EventSetDescriptor[events.size()];
            result = (EventSetDescriptor[])events.values().toArray(result);
        }
        return result;
    }

    private void addEvent(Map events, EventSetDescriptor esd) {
        String key = esd.getName();
/*
        if (esd.getName().equals("propertyChange")) {
            propertyChangeSource = true;
        }
*/
        EventSetDescriptor old = (EventSetDescriptor)events.get(key);
        if (old == null) {
            events.put(key, esd);
            return;
        }
        EventSetDescriptor composite = new EventSetDescriptor(old, esd);
        events.put(key, composite);
    }



    /**
     * A mapping of property names to a list of PropertyDescriptors.
     * By convention, the list property descriptors is ordered from
     * most abstract to most specific object inheritance.
     */
    private static final class PDStore {
        private final HashMap pdStore = new HashMap(); /*<String propName, List<PropertyDescriptor>>*/

        /**
         * Adds the property descriptor to the list store.
         */
        public void addPropertyDescriptor(PropertyDescriptor pd) {
            String propName = pd.getName();
            List list = (List) pdStore.get(propName);
            if (list == null) {
                list = new ArrayList();
                pdStore.put(propName, list);
            }
            list.add(pd);
        }

        /**
         * Get all the property descriptor lists accumulated.
         *
         * @return a Collection of List&lt;PropertyDescriptor&gt; objects
         */
        public Collection getPropertyDescriptors() {
            return pdStore.values();
        }
    } // end class PDStore

} // end class Introspector
