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
 * build a <code>BeanInfo</code> object that comprehensively describes
 * the target bean.</p>
 *
 * <p>We use low-level reflection to study the methods of the class and
 * apply standard design patterns to identify property accessors,
 * event sources, or public methods.  We then proceed to analyze the class's
 * superclass and add in the information from it (and possibly on up the
 * superclass chain).</p>
 *
 * <p>Because the Introspector caches <code>BeanInfo</code> classes for
 * better performance, take care if you use it in an application that uses
 * multiple class loaders.</p>
 *
 * <p>For more information about introspection and design patterns, please
 * consult the
 * <a href="http://java.sun.com/products/javabeans/docs/index.html">JavaBeans
 * specification</a>.</p>
 */
public final class Introspector {

    // Flags that can be used to control getBeanInfo:
    /**
     * Constant value to indicate that the <code>Introspector</code> will use
     * all <code>BeanInfo</code> classes that have been found.
     * This is the default.
     */
    public final static int USE_ALL_BEANINFO           = 1;
    /**
     * Constant value to indicate that the <code>Introspector</code> will
     * ignore the <code>BeanInfo</code> class of the current bean class,
     * but use any <code>BeanInfo</code> classes of the bean superclasses.
     */
    public final static int IGNORE_IMMEDIATE_BEANINFO  = 2;
    /**
     * Constant value to indicate that the <code>Introspector</code> will
     * ignore all <code>BeanInfo</code> classes, including any bean
     * superclasses.
     */
    public final static int IGNORE_ALL_BEANINFO        = 3;

    private final static EventSetDescriptor[] EMPTY_EVENTSETDESCRIPTORS = new EventSetDescriptor[0];
    private final static BeanInfo[] EMPTY_BEANINFO = new BeanInfo[0];

    //======================================================================
    // 				Public methods
    //======================================================================

    /**
     * Introspect on a Java Bean and learn about all its properties, exposed
     * methods, and events.
     *
     * <p>If the <code>BeanInfo</code> class for a Java Bean has been previously
     * introspected then the <code>BeanInfo</code> class is retrieved from
     * the <code>BeanInfo</code> cache.</p>
     *
     * @param beanClass The bean class to be analyzed.
     * @return A <code>BeanInfo</code> object describing the target bean.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass)
            throws IntrospectionException {
        return getBeanInfo(beanClass, null, USE_ALL_BEANINFO, ReflectUtil.isPackageAccessible(beanClass));
    }

    /**
     * Introspect on a Java bean and learn about all its properties, exposed
     * methods, and events, subject to some control flags.
     * <p>
     * If the <code>BeanInfo</code> class for a Java Bean has been previously
     * introspected based on the same arguments then the <code>BeanInfo</code>
     * class is retrieved from the <code>BeanInfo</code> cache.
     *
     * @param beanClass  The bean class to be analyzed.
     * @param flags  Flags to control the introspection.
     * @return  A BeanInfo object describing the target bean.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, int flags)
            throws IntrospectionException {

        if((flags < USE_ALL_BEANINFO) || (flags > IGNORE_ALL_BEANINFO)) {
            throw new IntrospectionException("Invalid flag for getBeanInfo");
        }

        boolean useBeanInfoCache = (flags == USE_ALL_BEANINFO);
        return getBeanInfo(beanClass, null, flags, useBeanInfoCache);
    }

    /**
     * Introspect on a Java Bean and learn about all its properties, exposed
     * methods, and events.
     *
     * <p>If the <code>BeanInfo</code> class for a Java Bean has been previously
     * introspected then the <code>BeanInfo</code> class is retrieved from
     * the <code>BeanInfo</code> cache.</p>
     *
     * @param beanClass The bean class to be analyzed.
     * @param stopClass The base class at which to stop the analysis.  Any
     *    methods/properties/events in the stopClass or in its base classes
     *    will be ignored in the analysis.
     * @return A <code>BeanInfo</code> object describing the target bean.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass)
            throws IntrospectionException {
        boolean useBeanInfoCache = (stopClass == null);

        // Check stopClass is a superClass of startClass.
        if (!useBeanInfoCache) {
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

        return getBeanInfo(beanClass, stopClass, USE_ALL_BEANINFO, useBeanInfoCache);
    }

    /**
     * Introspect on a Java Bean and learn about all its properties, exposed
     * methods, and events.
     *
     * <p>If the <code>BeanInfo</code> class for a Java Bean has been previously
     * introspected then the <code>BeanInfo</code> class is retrieved from
     * the <code>BeanInfo</code> cache.</p>
     *
     * <p>Flags to control the introspection are as follows.</p>
     * <dl>
     * <dt><code>flags == USE_ALL_BEANINFO</code></dt>
     * <dd>use all of the BeanInfo classes we can discover.</dd>
     * <dt><code>flags == IGNORE_IMMEDIATE_BEANINFO</code></dt>
     * <dd>ignore any BeanInfo associated with the specified beanClass,
     * but use any <code>BeanInfo</code> classes of the bean superclasses.</dd>
     * <dt><code>flags == IGNORE_ALL_BEANINFO</code></dt>
     * <dd>ignore all BeanInfo associated with the specified beanClass or
     * any of its parent classes.</dd>
     * </dl>
     *
     * @param beanClass The bean class to be analyzed.
     * @param stopClass The base class at which to stop the analysis.  Any
     *    methods/properties/events in the stopClass or in its base classes
     *    will be ignored in the analysis.
     * @param flags flags to control the introspection of explicit bean info.
     * @return A <code>BeanInfo</code> object describing the target bean.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass,
                                       int flags) throws IntrospectionException {
        if((flags < USE_ALL_BEANINFO) || (flags > IGNORE_ALL_BEANINFO)) {
            throw new IntrospectionException("Invalid flag for getBeanInfo");
        }

        boolean useBeanInfoCache = (stopClass == null) && (flags == USE_ALL_BEANINFO);

        // Check stopClass is a superClass of startClass.
        if (stopClass != null) {
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

        return getBeanInfo(beanClass, stopClass, flags, useBeanInfoCache);
    }

    /**
     * Introspect on a Java Bean and learn about all its properties,
     * exposed methods and events, below a given {@code stopClass} point
     * subject to the <code>useAllInfo</code> flag.
     * <p>
     * Any methods/properties/events in the {@code stopClass}
     * or in its parent classes will be ignored in the analysis.</p>
     * <p>
     * If the <code>BeanInfo</code> class for a Java Bean has been
     * previously introspected based on the same arguments then
     * the <code>BeanInfo</code> class is retrieved from the
     * <code>BeanInfo</code> cache.</p>
     *
     * @param beanClass the bean class to be analyzed
     * @param stopClass the parent class at which to stop the analysis
     * @param flags the flags for controlling how explicit bean info is
     *              included in the result
     * @param useBeanInfoCache <code>true</code> to indicate the
     *     <code>BeanInfo</code> cache should be used where possible, otherwise
     *     <code>false</code> to avoid the <code>BeanInfo</code> cache
     * @return a BeanInfo object describing the target bean
     * @throws IntrospectionException if an exception occurs during introspection
     */
    private static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass,
            int flags, boolean useBeanInfoCache) throws IntrospectionException {
        BeanInfo superBeanInfo;
        Class superClass = beanClass.getSuperclass();
        BeanInfo beanInfo;

        if(useBeanInfoCache) {
            beanInfo = IntrospectorSupport.getCachedBeanInfo(beanClass);
            if(beanInfo != null) {
                return beanInfo;
            }
        }

        if ((superClass != null) && (superClass != stopClass)) {
            int newFlags = flags;
            if (newFlags == IGNORE_IMMEDIATE_BEANINFO) {
                newFlags = USE_ALL_BEANINFO;
            }
            boolean newUseBeanInfoCache = useBeanInfoCache || ((stopClass == null) && (newFlags == USE_ALL_BEANINFO));

            superBeanInfo = getBeanInfo(superClass, stopClass, newFlags, newUseBeanInfoCache);
        } else {
            superBeanInfo = null;
        }

        Introspector introspector = new Introspector(beanClass, superBeanInfo, flags == USE_ALL_BEANINFO);

        BeanDescriptor bd = introspector.getTargetBeanDescriptor();

        MDStore mdStore = introspector.getTargetMethodInfo();
        Collection methodColl = mdStore.getMethods();

        // Allocate and populate the result array.
        MethodDescriptor mds[] = new MethodDescriptor[methodColl.size()];
        mds = (MethodDescriptor[]) methodColl.toArray(mds);

        ESDStore esdStore = introspector.getTargetEventInfo();
        EventSetDescriptor esds[];
        int defaultEventIndex;

        if (esdStore.isEmpty()) {
            esds = EMPTY_EVENTSETDESCRIPTORS;
            defaultEventIndex = -1;
        } else {
            // Allocate and populate the result array.
            Collection eventColl = esdStore.getEventSets();
            esds = new EventSetDescriptor[eventColl.size()];
            esds = (EventSetDescriptor[])eventColl.toArray(esds);

            // Set the default index.
            String defaultEventName = esdStore.getDefaultEventName();
            defaultEventIndex = findFeatureIndex(esds, defaultEventName);
        }

        PDStore pdStore = introspector.getTargetPropertyInfo(esdStore.isPropertyChangeSource());
        Map properties = IntrospectorSupport.processPropertyDescriptors(pdStore.getPropertyDescriptors());

        // Allocate and populate the result array.
        PropertyDescriptor pds[] = new PropertyDescriptor[properties.size()];
        pds = (PropertyDescriptor[]) properties.values().toArray(pds);

        // Set the default index.
        String defaultPropertyName = pdStore.getDefaultPropertyName();
        int defaultPropertyIndex = findFeatureIndex(pds, defaultPropertyName);

        beanInfo = new GenericBeanInfo(bd, esds, defaultEventIndex, pds,
                defaultPropertyIndex, mds);

        if(useBeanInfoCache) {
            IntrospectorSupport.putCachedBeanInfo(beanClass, beanInfo);
        }

        return beanInfo;
    }

    /**
     * Gets the list of package names that will be used for
     *          finding <code>BeanInfo</code> classes.
     *
     * @return  The array of package names that will be searched in
     *          order to find BeanInfo classes. The default value
     *          for this array is implementation-dependent; e.g.
     *          Sun implementation initially sets to {"sun.beans.infos"}.
     */

    public static String[] getBeanInfoSearchPath() {
        return ThreadGroupContext.getContext().getBeanInfoFinder().getPackages();
    }

    /**
     * Change the list of package names that will be used for
     *          finding <code>BeanInfo</code> classes.  The behaviour of
     *          this method is undefined if parameter path
     *          is <code>null</code>.
     *
     * <p>First, if there is a security manager, its <code>checkPropertiesAccess</code>
     * method is called. This could result in a <code>SecurityException</code>.</p>
     *
     * @param path  Array of package names.
     * @throws SecurityException  if a security manager exists and its
     *             <code>checkPropertiesAccess</code> method doesn't allow setting
     *              of system properties.
     * @see SecurityManager#checkPropertiesAccess
     */

    public static void setBeanInfoSearchPath(String[] path) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        ThreadGroupContext.getContext().getBeanInfoFinder().setPackages(path);
    }

    /**
     * Flush all of the Introspector's internal caches.  This method is
     * not normally required.  It is normally only needed by advanced
     * tools that update existing <code>Class</code> objects in-place and need
     * to make the Introspector re-analyze existing <code>Class</code> objects.
     */

    public static void flushCaches() {
        IntrospectorSupport.flushCaches();
    }

    /**
     * Flush the Introspector's internal cached information for a given class.
     * This method is not normally required.  It is normally only needed
     * by advanced tools that update existing <code>Class</code> objects in-place
     * and need to make the Introspector re-analyze an existing
     * <code>Class</code> object.
     * <p>
     * Note that only the direct state associated with the target <code>Class</code>
     * object is flushed.  We do not flush state for other <code>Class</code>
     * objects with the same name, nor do we flush state for any related
     * <code>Class</code> objects (such as subclasses), even though their state
     * may include information indirectly obtained from the target
     * <code>Class</code> object.</p>
     *
     * @param clz <code>Class</code> object to be flushed
     * @throws NullPointerException If the <code>Class</code> object is
     * <code>null</code>
     */
    public static void flushFromCaches(Class<?> clz) {
        IntrospectorSupport.flushFromCaches(clz);
    }

    //======================================================================
    // 			Private implementation methods
    //======================================================================

    private final Class beanClass;
    private final BeanInfo superBeanInfo;
    private final BeanInfo explicitBeanInfo;
    private final BeanInfo additionalBeanInfo[];

    private Introspector(Class beanClass, BeanInfo superBeanInfo, boolean useBeanInfo) {
        this.beanClass = beanClass;
        this.superBeanInfo = superBeanInfo;
        if(useBeanInfo) {
            this.explicitBeanInfo = findExplicitBeanInfo(beanClass);
        } else {
            this.explicitBeanInfo = null;
        }
        BeanInfo moreBeanInfo[] = null;
        if (explicitBeanInfo != null) {
            moreBeanInfo = explicitBeanInfo.getAdditionalBeanInfo();
        }
        additionalBeanInfo = (moreBeanInfo == null) ? EMPTY_BEANINFO : moreBeanInfo;
    }

    /**
     * Looks for an explicit BeanInfo class that corresponds to the Class.
     * First it looks in the existing package that the Class is defined in,
     * then it checks to see if the class is its own BeanInfo. Finally,
     * the BeanInfo search path is prepended to the class and searched.
     *
     * @param beanClass  the class type of the bean
     * @return Instance of an explicit BeanInfo class or null if one isn't found.
     */
    private static BeanInfo findExplicitBeanInfo(Class beanClass) {
        return ThreadGroupContext.getContext().getBeanInfoFinder().find(beanClass);
    }

    private static int findFeatureIndex(FeatureDescriptor fds[], String name) {
        int index = -1;

        if (name != null) {
            for (int i = 0; i < fds.length; i++) {
                if (name.equals(fds[i].getName())) {
                    index = i;
                }
            }
        }

        return index;
    }

    //======================================================================
    // 			Create bean descriptor
    //======================================================================

    private BeanDescriptor getTargetBeanDescriptor() {
        // Use explicit info, if available,
        if (explicitBeanInfo != null) {
            BeanDescriptor bd = explicitBeanInfo.getBeanDescriptor();
            if (bd != null) {
                return bd;
            }
        }
        // OK, fabricate a default BeanDescriptor.
        return new BeanDescriptor(this.beanClass);
    }

    //======================================================================
    // 			Create property descriptors
    //======================================================================

    /**
     * @return An array of PropertyDescriptors describing the
     * properties supported by the target bean.
     */
    private PDStore getTargetPropertyInfo(boolean propertyChangeSource) {
        // Map of property names to a list of property descriptors
        PDStore pdStore = new PDStore();

        // Check if the bean has its own BeanInfo that will provide
        // explicit information.
        PropertyDescriptor[] explicitProperties = null;
        if (explicitBeanInfo != null) {
            explicitProperties = explicitBeanInfo.getPropertyDescriptors();
            int index = explicitBeanInfo.getDefaultPropertyIndex();
            if ((index >= 0) && (index < explicitProperties.length)) {
                pdStore.setDefaultPropertyName(explicitProperties[index].getName());
            }
        }

        if ((explicitProperties == null) && (superBeanInfo != null)) {
            // We have no explicit BeanInfo properties.  Check with our parent.
            PropertyDescriptor supers[] = superBeanInfo.getPropertyDescriptors();
            pdStore.addProperties(beanClass, supers);
        }

        for (int i = 0; i < additionalBeanInfo.length; i++) {
            if(additionalBeanInfo[i] != null) {
                PropertyDescriptor[] additional = additionalBeanInfo[i].getPropertyDescriptors();
                pdStore.addProperties(beanClass, additional);
            }
        }

        if (explicitProperties != null) {
            // Add the explicit BeanInfo data to our results.
            pdStore.addProperties(beanClass, explicitProperties);

        } else {

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
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                String name = method.getName();
                Class resultType = method.getReturnType();
                int argCount = method.getParameterCount();
                int nameLen = name.length();
                PropertyDescriptor pd = null;

                // Optimization. Don't bother with invalid properties.
                if (argCount == 0) {
                    if (nameLen < 3) {
                        continue;
                    }
                } else if ((argCount > 2) || (nameLen < 4)) {
                    continue;
                }

                try {
                    switch (argCount) {
                        case 0:
                            if ((resultType != void.class) && name.startsWith(IntrospectorSupport.GET_PREFIX) && (nameLen > 3)) {
                                // Simple getter
                                pd = new PropertyDescriptor(this.beanClass, name.substring(3), method, null);
                            } else if ((resultType == boolean.class) && name.startsWith(IntrospectorSupport.IS_PREFIX)) {
                                // Boolean getter
                                pd = new PropertyDescriptor(this.beanClass, name.substring(2), method, null);
                            }
                            break;
                        case 1:
                            if (void.class == resultType) {
                                if (name.startsWith(IntrospectorSupport.SET_PREFIX)) {
                                    // Simple setter
                                    pd = new PropertyDescriptor(this.beanClass, name.substring(3), null, method);
                                    if (IntrospectorSupport.throwsException(method, PropertyVetoException.class)) {
                                        pd.setConstrained(true);
                                    }
                                }
                            } else if (name.startsWith(IntrospectorSupport.GET_PREFIX)) {
                                Class<?>[] parameterTypes = method.getParameterTypes();
                                if ((parameterTypes.length > 0) && (int.class == parameterTypes[0])) {
                                    pd = new IndexedPropertyDescriptor(this.beanClass, name.substring(3), null, null, method, null);
                                }
                            }
                            break;
                        case 2:
                            if ((void.class == resultType) && name.startsWith(IntrospectorSupport.SET_PREFIX)) {
                                Class<?>[] parameterTypes = method.getParameterTypes();
                                if ((parameterTypes.length > 0) && (int.class == parameterTypes[0])) {
                                    pd = new IndexedPropertyDescriptor(this.beanClass, name.substring(3), null, null, null, method);
                                    if (IntrospectorSupport.throwsException(method, PropertyVetoException.class)) {
                                        pd.setConstrained(true);
                                    }
                                }
                            }
                            break;
                    }
                } catch (IntrospectionException ex) {
                    // This happens if a PropertyDescriptor or IndexedPropertyDescriptor
                    // constructor finds that the method violates details of the design
                    // pattern, e.g. by having an empty name, or a getter returning
                    // void , or whatever.
                    pd = null;
                }

                if (pd != null) {
                    // If this class or one of its base classes is a PropertyChange
                    // source, then we assume that any properties we discover are "bound".
                    if (propertyChangeSource) {
                        pd.setBound(true);
                    }
                    pdStore.addProperty(beanClass, pd);
                }
            }
        }

        return pdStore;
    }

    //======================================================================
    // 			Create event descriptors
    //======================================================================

    /**
     * @return An array of EventSetDescriptors describing the kinds of
     * events fired by the target bean.
     */
    private ESDStore getTargetEventInfo() throws IntrospectionException {
        ESDStore esdStore = new ESDStore();

        // Check if the bean has its own BeanInfo that will provide
        // explicit information.
        EventSetDescriptor[] explicitEvents = null;
        if (explicitBeanInfo != null) {
            explicitEvents = explicitBeanInfo.getEventSetDescriptors();
            int ix = explicitBeanInfo.getDefaultEventIndex();
            if ((ix >= 0) && (ix < explicitEvents.length)) {
                esdStore.setDefaultEventName(explicitEvents[ix].getName());
            }
        }

        if ((explicitEvents == null) && (superBeanInfo != null)) {
            // We have no explicit BeanInfo events.  Check with our parent.
            EventSetDescriptor supers[] = superBeanInfo.getEventSetDescriptors();
            esdStore.addEvents(supers);
        }

        for (int i = 0; i < additionalBeanInfo.length; i++) {
            EventSetDescriptor additional[] = additionalBeanInfo[i].getEventSetDescriptors();
            esdStore.addEvents(additional);
        }

        if (explicitEvents != null) {
            // Add the explicit explicitBeanInfo data to our results.
            esdStore.addEvents(explicitEvents);

        } else {
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
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                String name = method.getName();
                int argCount = method.getParameterCount();

                if (name.startsWith(IntrospectorSupport.ADD_PREFIX)) {
                    Class<?> returnType = method.getReturnType();
                    if ((returnType == void.class) && (argCount == 1)) {
                        Type[] parameterTypes = method.getGenericParameterTypes();
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
                } else if (name.startsWith(IntrospectorSupport.REMOVE_PREFIX)) {
                    Class<?> returnType = method.getReturnType();
                    if ((returnType == void.class) && (argCount == 1)) {
                        Type[] parameterTypes = method.getGenericParameterTypes();
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
                } else if (name.startsWith(IntrospectorSupport.GET_PREFIX)) {
                    if (argCount == 0) {
                        Class<?> returnType = IntrospectorSupport.getReturnType(beanClass, method);
                        if (returnType.isArray()) {
                            Class<?> type = returnType.getComponentType();
                            if (IntrospectorSupport.isSubclass(type, IntrospectorSupport.eventListenerType)) {
                                String listenerName = name.substring(3, name.length() - 1);
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
                    if ((removes.get(listenerName) == null) || !listenerName.endsWith("Listener")) {
                        continue;
                    }
                    String eventName = NameGenerator.decapitalize(listenerName.substring(0, listenerName.length() - 8));
                    Method addMethod = (Method) adds.get(listenerName);
                    Method removeMethod = (Method) removes.get(listenerName);
                    Method getMethod = null;
                    if (gets != null) {
                        getMethod = (Method) gets.get(listenerName);
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
                    Method[] methods = new Method[validMethods.size()];
                    methods = (Method[]) validMethods.toArray(methods);

                    EventSetDescriptor esd = new EventSetDescriptor(eventName, argType,
                            methods, addMethod, removeMethod, getMethod);

                    // If the adder method throws the TooManyListenersException then it
                    // is a Unicast event source.
                    if (IntrospectorSupport.throwsException(addMethod,
                            java.util.TooManyListenersException.class)) {
                        esd.setUnicast(true);
                    }
                    esdStore.addEvent(esd);
                }
            } // if (adds != null ...
        }

        return esdStore;
    }

    //======================================================================
    // 			Create method descriptors
    //======================================================================

    /**
     * @return An array of MethodDescriptors describing the private
     * methods supported by the target bean.
     */
    private MDStore getTargetMethodInfo() {
        // Methods maps from Method objects to MethodDescriptors
        MDStore mdStore = new MDStore();

        // Check if the bean has its own BeanInfo that will provide
        // explicit information.
        MethodDescriptor[] explicitMethods = null;
        if (explicitBeanInfo != null) {
            explicitMethods = explicitBeanInfo.getMethodDescriptors();
        }

        if ((explicitMethods == null) && (superBeanInfo != null)) {
            // We have no explicit BeanInfo methods.  Check with our parent.
            MethodDescriptor supers[] = superBeanInfo.getMethodDescriptors();
            mdStore.addMethods(supers);
        }

        for (int i = 0; i < additionalBeanInfo.length; i++) {
            MethodDescriptor additional[] = additionalBeanInfo[i].getMethodDescriptors();
            mdStore.addMethods(additional);
        }

        if (explicitMethods != null) {
            // Add the explicit explicitBeanInfo data to our results.
            mdStore.addMethods(explicitMethods);

        } else {
            // Apply some reflection to the current class.

            // First get an array of all the beans methods at this level
            Method methodList[] = IntrospectorSupport.getPublicDeclaredMethods(beanClass);

            // Now analyze each method.
            for (int i = 0; i < methodList.length; i++) {
                Method method = methodList[i];
                if (method != null) {
                    MethodDescriptor md = new MethodDescriptor(method);
                    mdStore.addMethod(md);
                }
            }
        }

        return mdStore;
    }


    //======================================================================
    // 			Private classes
    //======================================================================

    /**
     * A mapping of property names to a list of PropertyDescriptors.
     * By convention, the list property descriptors is ordered from
     * most abstract to most specific object inheritance.
     */
    private static final class PDStore {
        private final HashMap pdStore = new HashMap(); /*<String propName, List<PropertyDescriptor>>*/
        private String defaultPropertyName;

        public void addProperties(Class beanClass, PropertyDescriptor pds[]) {
            if(pds != null) {
                for(int i = 0; i < pds.length; i++) {
                    addProperty(beanClass, pds[i]);
                }
            }
        }

        /**
         * Adds the property descriptor to the list store.
         */
        public void addProperty(Class beanClass, PropertyDescriptor pd) {
            String propName = pd.getName();
            List list = (List) pdStore.get(propName);
            if (list == null) {
                list = new ArrayList();
                pdStore.put(propName, list);
            }
            if (beanClass != pd.getClass0()) {
                // replace existing property descriptor
                // only if we have types to resolve
                // in the context of this.beanClass
                Method read = pd.getReadMethod();
                Method write = pd.getWriteMethod();
                boolean cls = true;
                if (read != null)
                    cls = cls && (read.getGenericReturnType() instanceof Class);
                if (write != null)
                    cls = cls && (write.getGenericParameterTypes()[0] instanceof Class);
                if (pd instanceof IndexedPropertyDescriptor) {
                    IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
                    Method readI = ipd.getIndexedReadMethod();
                    Method writeI = ipd.getIndexedWriteMethod();
                    if (readI != null)
                        cls = cls && (readI.getGenericReturnType() instanceof Class);
                    if (writeI != null)
                        cls = cls && (writeI.getGenericParameterTypes()[1] instanceof Class);
                    if (!cls) {
                        pd = new IndexedPropertyDescriptor(ipd);
                        pd.updateGenericsFor(beanClass);
                    }
                }
                else if (!cls) {
                    pd = new PropertyDescriptor(pd);
                    pd.updateGenericsFor(beanClass);
                }
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

        public String getDefaultPropertyName() {
            return defaultPropertyName;
        }

        public void setDefaultPropertyName(String defaultPropertyName) {
            this.defaultPropertyName = defaultPropertyName;
        }
    } // end class PDStore

    private static final class ESDStore {
        private final Map events = new HashMap(); /*<String eventName, EventSetDescriptor>*/
        private String defaultEventName;
        private boolean propertyChangeSource = false;

        public void addEvents(EventSetDescriptor esds[]) {
            if(esds != null) {
                for(int i = 0; i < esds.length; i++) {
                    addEvent(esds[i]);
                }
            }
        }

        private void addEvent(EventSetDescriptor esd) {
            String key = esd.getName();
            if ("propertyChange".equals(esd.getName())) {
                propertyChangeSource = true;
            }
            EventSetDescriptor old = (EventSetDescriptor)events.get(key);
            if (old == null) {
                events.put(key, esd);
                return;
            }
            EventSetDescriptor composite = new EventSetDescriptor(old, esd);
            events.put(key, composite);
        }

        public boolean isEmpty() {
            return events.isEmpty();
        }

        public boolean isPropertyChangeSource() {
            return propertyChangeSource;
        }

        public Collection getEventSets() {
            return events.values();
        }

        public String getDefaultEventName() {
            return defaultEventName;
        }

        public void setDefaultEventName(String defaultEventName) {
            this.defaultEventName = defaultEventName;
        }
    } // end class ESDStore

    private static final class MDStore {
        private final Map methods = new HashMap(60);/*<String methodName, MethodDescriptor>*/

        public void addMethods(MethodDescriptor mds[]) {
            if(mds != null) {
                for(int i = 0; i < mds.length; i++) {
                    addMethod(mds[i]);
                }
            }
        }

        public void addMethod(MethodDescriptor md) {
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

        public Collection getMethods() {
            return methods.values();
        }
    } // end class MDStore
} // end class Introspector
