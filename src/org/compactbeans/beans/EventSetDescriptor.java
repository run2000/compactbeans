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

import java.lang.ref.Reference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

/**
 * An EventSetDescriptor describes a group of events that a given Java
 * bean fires.
 * <p>
 * The given group of events are all delivered as method calls on a single
 * event listener interface, and an event listener object can be registered
 * via a call on a registration method supplied by the event source.</p>
 */
public final class EventSetDescriptor implements FeatureDescriptor {

    private MethodDescriptor[] listenerMethodDescriptors;
    private MethodDescriptor addMethodDescriptor;
    private MethodDescriptor removeMethodDescriptor;
    private MethodDescriptor getMethodDescriptor;

    private Reference<Method[]> listenerMethodsRef;
    private Reference<Class> listenerTypeRef;

    private Reference<Class> classRef;

    private final String name;
    private boolean unicast;

    private final DescriptorData descriptorData;

    /**
     * Creates an <code>EventSetDescriptor</code> from scratch using
     * string names.
     *
     * @param sourceClass  The class firing the event.
     * @param eventSetName The programmatic name of the event set.
     *          Note that this should normally start with a lower-case character.
     * @param listenerType  The Class of the target interface that events
     *          will get delivered to.
     * @param listenerMethodNames The names of the methods that will get called
     *          when the event gets delivered to its target listener interface.
     * @param addListenerMethodName  The name of the method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethodName  The name of the method on the event source
     *          that can be used to de-register an event listener object.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public EventSetDescriptor(Class<?> sourceClass,
                              String eventSetName,
                              Class<?> listenerType,
                              String listenerMethodNames[],
                              String addListenerMethodName,
                              String removeListenerMethodName)
            throws IntrospectionException {
        this(sourceClass, eventSetName, listenerType,
                listenerMethodNames, addListenerMethodName,
                removeListenerMethodName, null, null);
    }

    /**
     * Creates an <code>EventSetDescriptor</code> from scratch using
     * string names.
     *
     * @param sourceClass  The class firing the event.
     * @param eventSetName The programmatic name of the event set.
     *          Note that this should normally start with a lower-case character.
     * @param listenerType  The Class of the target interface that events
     *          will get delivered to.
     * @param listenerMethodNames The names of the methods that will get called
     *          when the event gets delivered to its target listener interface.
     * @param addListenerMethodName  The name of the method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethodName  The name of the method on the event source
     *          that can be used to de-register an event listener object.
     * @param descriptorData the descriptor data for this event set descriptor,
     *                       possibly <code>null</code>
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public EventSetDescriptor(Class<?> sourceClass,
                              String eventSetName,
                              Class<?> listenerType,
                              String listenerMethodNames[],
                              String addListenerMethodName,
                              String removeListenerMethodName,
                              DescriptorData descriptorData)
            throws IntrospectionException {
        this(sourceClass, eventSetName, listenerType,
                listenerMethodNames, addListenerMethodName,
                removeListenerMethodName, null, descriptorData);
    }

    /**
     * This constructor creates an EventSetDescriptor from scratch using
     * string names.
     *
     * @param sourceClass  The class firing the event.
     * @param eventSetName The programmatic name of the event set.
     *          Note that this should normally start with a lower-case character.
     * @param listenerType  The Class of the target interface that events
     *          will get delivered to.
     * @param listenerMethodNames The names of the methods that will get called
     *          when the event gets delivered to its target listener interface.
     * @param addListenerMethodName  The name of the method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethodName  The name of the method on the event source
     *          that can be used to de-register an event listener object.
     * @param getListenerMethodName The method on the event source that
     *          can be used to access the array of event listener objects.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     * @since 1.4
     */
    public EventSetDescriptor(Class<?> sourceClass,
                              String eventSetName,
                              Class<?> listenerType,
                              String listenerMethodNames[],
                              String addListenerMethodName,
                              String removeListenerMethodName,
                              String getListenerMethodName)
            throws IntrospectionException {
        this(sourceClass, eventSetName, listenerType, listenerMethodNames,
                addListenerMethodName, removeListenerMethodName, getListenerMethodName,
                null);
    }

    /**
     * This constructor creates an EventSetDescriptor from scratch using
     * string names.
     *
     * @param sourceClass  The class firing the event.
     * @param eventSetName The programmatic name of the event set.
     *          Note that this should normally start with a lower-case character.
     * @param listenerType  The Class of the target interface that events
     *          will get delivered to.
     * @param listenerMethodNames The names of the methods that will get called
     *          when the event gets delivered to its target listener interface.
     * @param addListenerMethodName  The name of the method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethodName  The name of the method on the event source
     *          that can be used to de-register an event listener object.
     * @param getListenerMethodName The method on the event source that
     *          can be used to access the array of event listener objects.
     * @param descriptorData the descriptor data for this event set descriptor,
     *                       possibly <code>null</code>
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     * @since 1.4
     */
    public EventSetDescriptor(Class<?> sourceClass,
                              String eventSetName,
                              Class<?> listenerType,
                              String listenerMethodNames[],
                              String addListenerMethodName,
                              String removeListenerMethodName,
                              String getListenerMethodName,
                              DescriptorData descriptorData)
            throws IntrospectionException {
        if (sourceClass == null || eventSetName == null || listenerType == null) {
            throw new NullPointerException();
        }
        this.name = eventSetName;
        setClass0(sourceClass);
        setListenerType(listenerType);

        Method[] listenerMethods = new Method[listenerMethodNames.length];
        for (int i = 0; i < listenerMethodNames.length; i++) {
            // Check for null names
            if (listenerMethodNames[i] == null) {
                throw new NullPointerException();
            }
            listenerMethods[i] = getMethod(listenerType, listenerMethodNames[i], 1);
        }
        setListenerMethods(listenerMethods);

        setAddListenerMethod(getMethod(sourceClass, addListenerMethodName, 1));
        setRemoveListenerMethod(getMethod(sourceClass, removeListenerMethodName, 1));

        // Be more forgiving of not finding the getListener method.
        Method method = IntrospectorSupport.findMethod(sourceClass, getListenerMethodName, 0);
        if (method != null) {
            setGetListenerMethod(method);
        }
        this.descriptorData = descriptorData;
    }

    /**
     * Creates an <code>EventSetDescriptor</code> from scratch using
     * <code>java.lang.reflect.Method</code> and <code>java.lang.Class</code>
     * objects.
     *
     * @param eventSetName The programmatic name of the event set.
     * @param listenerType The Class for the listener interface.
     * @param listenerMethods  An array of Method objects describing each
     *          of the event handling methods in the target listener.
     * @param addListenerMethod  The method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethod  The method on the event source
     *          that can be used to de-register an event listener object.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public EventSetDescriptor(String eventSetName,
                              Class<?> listenerType,
                              Method listenerMethods[],
                              Method addListenerMethod,
                              Method removeListenerMethod)
            throws IntrospectionException {
        this(eventSetName, listenerType, listenerMethods,
                addListenerMethod, removeListenerMethod, null, null);
    }

    /**
     * Creates an <code>EventSetDescriptor</code> from scratch using
     * <code>java.lang.reflect.Method</code> and <code>java.lang.Class</code>
     * objects.
     *
     * @param eventSetName The programmatic name of the event set.
     * @param listenerType The Class for the listener interface.
     * @param listenerMethods  An array of Method objects describing each
     *          of the event handling methods in the target listener.
     * @param addListenerMethod  The method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethod  The method on the event source
     *          that can be used to de-register an event listener object.
     * @param descriptorData the descriptor data for this event set descriptor,
     *                       possibly <code>null</code>
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public EventSetDescriptor(String eventSetName,
                              Class<?> listenerType,
                              Method listenerMethods[],
                              Method addListenerMethod,
                              Method removeListenerMethod,
                              DescriptorData descriptorData)
            throws IntrospectionException {
        this(eventSetName, listenerType, listenerMethods,
                addListenerMethod, removeListenerMethod, null, descriptorData);
    }

    /**
     * This constructor creates an EventSetDescriptor from scratch using
     * java.lang.reflect.Method and java.lang.Class objects.
     *
     * @param eventSetName The programmatic name of the event set.
     * @param listenerType The Class for the listener interface.
     * @param listenerMethods  An array of Method objects describing each
     *          of the event handling methods in the target listener.
     * @param addListenerMethod  The method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethod  The method on the event source
     *          that can be used to de-register an event listener object.
     * @param getListenerMethod The method on the event source
     *          that can be used to access the array of event listener objects.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     * @since 1.4
     */
    public EventSetDescriptor(String eventSetName,
                              Class<?> listenerType,
                              Method listenerMethods[],
                              Method addListenerMethod,
                              Method removeListenerMethod,
                              Method getListenerMethod)
            throws IntrospectionException {
        this(eventSetName, listenerType, listenerMethods,
                addListenerMethod, removeListenerMethod,
                getListenerMethod,  null);
    }

    /**
     * This constructor creates an EventSetDescriptor from scratch using
     * java.lang.reflect.Method and java.lang.Class objects.
     *
     * @param eventSetName The programmatic name of the event set.
     * @param listenerType The Class for the listener interface.
     * @param listenerMethods  An array of Method objects describing each
     *          of the event handling methods in the target listener.
     * @param addListenerMethod  The method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethod  The method on the event source
     *          that can be used to de-register an event listener object.
     * @param getListenerMethod The method on the event source
     *          that can be used to access the array of event listener objects.
     * @param descriptorData the descriptor data for this event set descriptor,
     *                       possibly <code>null</code>
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     * @since 1.4
     */
    public EventSetDescriptor(String eventSetName,
                              Class<?> listenerType,
                              Method listenerMethods[],
                              Method addListenerMethod,
                              Method removeListenerMethod,
                              Method getListenerMethod,
                              DescriptorData descriptorData)
            throws IntrospectionException {
        this.name = eventSetName;
        setListenerMethods(listenerMethods);
        setAddListenerMethod(addListenerMethod);
        setRemoveListenerMethod( removeListenerMethod);
        setGetListenerMethod(getListenerMethod);
        setListenerType(listenerType);
        this.descriptorData = descriptorData;
    }

    /**
     * Creates an <code>EventSetDescriptor</code> from scratch using
     * <code>java.lang.reflect.MethodDescriptor</code> and
     * <code>java.lang.Class</code> objects.
     *
     * @param eventSetName The programmatic name of the event set.
     * @param listenerType The Class for the listener interface.
     * @param listenerMethodDescriptors  An array of MethodDescriptor objects
     *           describing each of the event handling methods in the
     *           target listener.
     * @param addListenerMethod  The method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethod  The method on the event source
     *          that can be used to de-register an event listener object.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public EventSetDescriptor(String eventSetName,
                Class<?> listenerType,
                MethodDescriptor listenerMethodDescriptors[],
                Method addListenerMethod,
                Method removeListenerMethod)
                throws IntrospectionException {
        this(eventSetName, listenerType, listenerMethodDescriptors,
                addListenerMethod, removeListenerMethod, null);
    }

    /**
     * Creates an <code>EventSetDescriptor</code> from scratch using
     * <code>java.lang.reflect.MethodDescriptor</code> and
     * <code>java.lang.Class</code> objects.
     *
     * @param eventSetName The programmatic name of the event set.
     * @param listenerType The Class for the listener interface.
     * @param listenerMethodDescriptors  An array of MethodDescriptor objects
     *           describing each of the event handling methods in the
     *           target listener.
     * @param addListenerMethod  The method on the event source
     *          that can be used to register an event listener object.
     * @param removeListenerMethod  The method on the event source
     *          that can be used to de-register an event listener object.
     * @param descriptorData the descriptor data for this event set descriptor,
     *                       possibly <code>null</code>
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public EventSetDescriptor(String eventSetName,
                Class<?> listenerType,
                MethodDescriptor listenerMethodDescriptors[],
                Method addListenerMethod,
                Method removeListenerMethod,
                DescriptorData descriptorData)
                throws IntrospectionException {
        this.name = eventSetName;
        this.listenerMethodDescriptors = listenerMethodDescriptors;
        setAddListenerMethod(addListenerMethod);
        setRemoveListenerMethod(removeListenerMethod);
        setListenerType(listenerType);
        this.descriptorData = descriptorData;
    }

    /*
     * Package-private constructor
     * Merge two event set descriptors.  Where they conflict, give the
     * second argument (y) priority over the first argument (x).
     *
     * @param x  The first (lower priority) EventSetDescriptor
     * @param y  The second (higher priority) EventSetDescriptor
     */
    EventSetDescriptor(EventSetDescriptor x, EventSetDescriptor y) {
        name = y.name;
        classRef = x.classRef;
        if (y.classRef != null) {
            classRef = y.classRef;
        }
        listenerMethodDescriptors = x.listenerMethodDescriptors;
        if (y.listenerMethodDescriptors != null) {
            listenerMethodDescriptors = y.listenerMethodDescriptors;
        }

        listenerTypeRef = x.listenerTypeRef;
        if (y.listenerTypeRef != null) {
            listenerTypeRef = y.listenerTypeRef;
        }

        addMethodDescriptor = x.addMethodDescriptor;
        if (y.addMethodDescriptor != null) {
            addMethodDescriptor = y.addMethodDescriptor;
        }

        removeMethodDescriptor = x.removeMethodDescriptor;
        if (y.removeMethodDescriptor != null) {
            removeMethodDescriptor = y.removeMethodDescriptor;
        }

        getMethodDescriptor = x.getMethodDescriptor;
        if (y.getMethodDescriptor != null) {
            getMethodDescriptor = y.getMethodDescriptor;
        }

        unicast = y.unicast;

        if(x.descriptorData == null) {
            if(y.descriptorData == null) {
                descriptorData = null;
            } else {
                descriptorData = (DescriptorData) y.descriptorData.clone();
            }
        } else if(y.descriptorData == null) {
            descriptorData = (DescriptorData) x.descriptorData.clone();
        } else {
            descriptorData = new DescriptorData(x.descriptorData, y.descriptorData);
        }
    }

    /*
     * Package-private dup constructor
     * This must isolate the new object from any changes to the old object.
     */
    EventSetDescriptor(EventSetDescriptor old) {
        name = old.name;
        classRef = old.classRef;

        if (old.listenerMethodDescriptors != null) {
            int len = old.listenerMethodDescriptors.length;
            listenerMethodDescriptors = new MethodDescriptor[len];
            for (int i = 0; i < len; i++) {
                listenerMethodDescriptors[i] = new MethodDescriptor(
                                        old.listenerMethodDescriptors[i]);
            }
        }
        listenerTypeRef = old.listenerTypeRef;

        addMethodDescriptor = old.addMethodDescriptor;
        removeMethodDescriptor = old.removeMethodDescriptor;
        getMethodDescriptor = old.getMethodDescriptor;

        unicast = old.unicast;
        descriptorData = old.getDescriptorData();
    }

    /**
     * Duplicate constructor, with new <code>DescriptorData</code>.
     * This must isolate the new object from any changes to the old object.
     *
     * @param old the event set descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    public EventSetDescriptor(EventSetDescriptor old, DescriptorData newData) {
        name = old.name;
        classRef = old.classRef;

        if (old.listenerMethodDescriptors != null) {
            int len = old.listenerMethodDescriptors.length;
            listenerMethodDescriptors = new MethodDescriptor[len];
            for (int i = 0; i < len; i++) {
                listenerMethodDescriptors[i] = new MethodDescriptor(
                                        old.listenerMethodDescriptors[i]);
            }
        }
        listenerTypeRef = old.listenerTypeRef;

        addMethodDescriptor = old.addMethodDescriptor;
        removeMethodDescriptor = old.removeMethodDescriptor;
        getMethodDescriptor = old.getMethodDescriptor;

        unicast = old.unicast;
        descriptorData = newData;
    }

    private static Method getMethod(Class cls, String name, int args)
            throws IntrospectionException {
        if (name == null) {
            return null;
        }
        Method method = IntrospectorSupport.findMethod(cls, name, args);
        if ((method == null) || Modifier.isStatic(method.getModifiers())) {
            throw new IntrospectionException("Method not found: " + name +
                    " on class " + cls.getName());
        }
        return method;
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the event
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.EVENT_SET</code> to indicate this is an
     * EventSetDescriptor object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.EVENT_SET;
    }

    /**
     * Gets the <code>Class</code> object for the target interface.
     *
     * @return The Class object for the target interface that will
     * get invoked when the event is fired.
     */
    public Class<?> getListenerType() {
        return RefUtil.getObject(this.listenerTypeRef);
    }

    private void setListenerType(Class cls) {
        this.listenerTypeRef = RefUtil.createWeakReference(cls);
    }

    /**
     * Gets the methods of the target listener interface.
     *
     * @return An array of <code>Method</code> objects for the target methods
     * within the target listener interface that will get called when
     * events are fired.
     */
    public synchronized Method[] getListenerMethods() {
        Method[] methods = getListenerMethods0();
        if (methods == null) {
            if (listenerMethodDescriptors != null) {
                methods = new Method[listenerMethodDescriptors.length];
                for (int i = 0; i < methods.length; i++) {
                    methods[i] = listenerMethodDescriptors[i].getMethod();
                }
            }
            setListenerMethods(methods);
        }
        return methods;
    }

    private void setListenerMethods(Method[] methods) {
        if (methods == null) {
            return;
        }
        if (listenerMethodDescriptors == null) {
            listenerMethodDescriptors = new MethodDescriptor[methods.length];
            for (int i = 0; i < methods.length; i++) {
                listenerMethodDescriptors[i] = new MethodDescriptor(methods[i]);
            }
        }
        this.listenerMethodsRef = RefUtil.createSoftReference(methods);
    }

    private Method[] getListenerMethods0() {
        return RefUtil.getObject(this.listenerMethodsRef);
    }

    /**
     * Gets the <code>MethodDescriptor</code>s of the target listener interface.
     *
     * @return An array of <code>MethodDescriptor</code> objects for the target methods
     * within the target listener interface that will get called when
     * events are fired.
     */
    public synchronized MethodDescriptor[] getListenerMethodDescriptors() {
        return listenerMethodDescriptors;
    }

    /**
     * Gets the method used to add event listeners.
     *
     * @return The method used to register a listener at the event source.
     */
    public synchronized Method getAddListenerMethod() {
        return getMethod(this.addMethodDescriptor);
    }

    private synchronized void setAddListenerMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            setClass0(method.getDeclaringClass());
        }
        addMethodDescriptor = new MethodDescriptor(method);
    }

    /**
     * Gets the method used to remove event listeners.
     *
     * @return The method used to remove a listener at the event source.
     */
    public synchronized Method getRemoveListenerMethod() {
        return getMethod(this.removeMethodDescriptor);
    }

    private synchronized void setRemoveListenerMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            setClass0(method.getDeclaringClass());
        }
        removeMethodDescriptor = new MethodDescriptor(method);
    }

    /**
     * Gets the method used to access the registered event listeners.
     *
     * @return The method used to access the array of listeners at the event
     *         source or <code>null</code> if it doesn't exist.
     * @since 1.4
     */
    public synchronized Method getGetListenerMethod() {
        return getMethod(this.getMethodDescriptor);
    }

    private synchronized void setGetListenerMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            setClass0(method.getDeclaringClass());
        }
        getMethodDescriptor = new MethodDescriptor(method);
    }

    /**
     * Mark an event set as unicast (or not).
     *
     * @param unicast  True if the event set is unicast.
     */
    void setUnicast(boolean unicast) {
        this.unicast = unicast;
    }

    /**
     * Normally event sources are multicast.  However there are some
     * exceptions that are strictly unicast.
     *
     * @return <code>true</code> if the event set is unicast.
     * Defaults to <code>false</code>.
     */
    public boolean isUnicast() {
        return unicast;
    }

    /**
     * Reports if an event set is in the &quot;default&quot; set.
     *
     * @return <code>true</code> if the event set is in
     * the &quot;default&quot; set. Defaults to <code>true</code>.
     */
    public boolean isInDefaultEventSet() {
        return true;
    }

    // Package private methods for recreating the weak/soft referent

    private void setClass0(Class cls) {
        this.classRef = RefUtil.createWeakReference(cls);
    }

    Class getClass0() {
        return RefUtil.getObject(this.classRef);
    }

    private static Method getMethod(MethodDescriptor descriptor) {
        return (descriptor != null)
                ? descriptor.getMethod()
                : null;
    }

    public String getDisplayName() {
        return (descriptorData == null) ? null : descriptorData.getDisplayName();
    }

    public boolean isExpert() {
        return (descriptorData != null) && descriptorData.isExpert();
    }

    public boolean isHidden() {
        return (descriptorData != null) && descriptorData.isHidden();
    }

    public boolean isPreferred() {
        return (descriptorData != null) && descriptorData.isPreferred();
    }

    public String getShortDescription() {
        return (descriptorData == null) ? null : descriptorData.getShortDescription();
    }

    public boolean hasAttributes() {
        return (descriptorData != null) && descriptorData.hasAttributes();
    }

    public Object getValue(String attributeName) {
        return (descriptorData == null) ? null : descriptorData.getValue(attributeName);
    }

    public Enumeration<String> attributeNames() {
        return (descriptorData == null) ? DescriptorData.EMPTY_KEYS : descriptorData.attributeNames();
    }

    public DescriptorData getDescriptorData() {
        return (descriptorData == null) ? null : (DescriptorData) descriptorData.clone();
    }

    public boolean hasDescriptorData() {
        return (descriptorData != null);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     *
     * @since 1.7
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[name=").append(this.name);
        if (this.unicast) {
            sb.append("; ").append("unicast");
        }
        sb.append("; ").append("inDefaultEventSet");
        if (this.listenerTypeRef != null) {
            appendTo(sb, "listenerType", this.listenerTypeRef.get());
        }
        appendTo(sb, "getListenerMethod", getMethod(this.getMethodDescriptor));
        appendTo(sb, "addListenerMethod", getMethod(this.addMethodDescriptor));
        appendTo(sb, "removeListenerMethod", getMethod(this.removeMethodDescriptor));
        return sb.append("]").toString();
    }

    private static void appendTo(StringBuilder sb, String name, Object value) {
        if (value != null) {
            sb.append("; ").append(name).append('=').append(value);
        }
    }
}
