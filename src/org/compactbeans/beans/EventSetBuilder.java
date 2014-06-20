/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Collections;
import java.util.Map;

/**
 * Builder for EventSetDescriptor classes. This allows controlled building
 * and mutation before creating an immutable EventSetDescriptor class.
 *
 * @author NCull
 * @version 16/06/2014, 2:08 PM
 */
public final class EventSetBuilder implements FeatureBuilder<EventSetDescriptor> {
    private final EventSetDescriptor protoFeature;
    private DescriptorData protoDescriptor;

    /**
     * Build an <code>EventSetDescriptor</code> assuming that you are
     * following the most simple standard design pattern where a named
     * event &quot;fred&quot; is
     * <ol>
     * <li> delivered as a call on the single method of
     * interface FredListener,</li>
     * <li>has a single argument of type FredEvent,
     * and </li>
     * <li>where the FredListener may be registered with a call on an
     * addFredListener method of the source component and removed with a
     * call on a removeFredListener method.</li>
     * </ol>
     *
     * @param sourceClass  The class firing the event.
     * @param eventSetName  The programmatic name of the event.  E.g. &quot;fred&quot;.
     *          Note that this should normally start with a lower-case character.
     * @param listenerType  The target interface that events
     *          will get delivered to.
     * @param listenerMethodName  The method that will get called when the event gets
     *          delivered to its target listener interface.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public EventSetBuilder(Class<?> sourceClass, String eventSetName,
                              Class<?> listenerType, String listenerMethodName)
            throws IntrospectionException {
        protoFeature = new EventSetDescriptor(sourceClass, eventSetName, listenerType, listenerMethodName);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Build an <code>EventSetDescriptor</code> from scratch using
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
    public EventSetBuilder(Class<?> sourceClass,
                              String eventSetName,
                              Class<?> listenerType,
                              String listenerMethodNames[],
                              String addListenerMethodName,
                              String removeListenerMethodName)
            throws IntrospectionException {
        protoFeature = new EventSetDescriptor(sourceClass, eventSetName, listenerType,
                listenerMethodNames, addListenerMethodName,
                removeListenerMethodName);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * This constructor builds an <code>EventSetDescriptor</code> from scratch
     * using string names.
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
    public EventSetBuilder(Class<?> sourceClass,
                              String eventSetName,
                              Class<?> listenerType,
                              String listenerMethodNames[],
                              String addListenerMethodName,
                              String removeListenerMethodName,
                              String getListenerMethodName)
            throws IntrospectionException {
        protoFeature = new EventSetDescriptor(sourceClass, eventSetName, listenerType,
                listenerMethodNames, addListenerMethodName, removeListenerMethodName,
                getListenerMethodName);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds an <code>EventSetDescriptor</code> from scratch using
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
    public EventSetBuilder(String eventSetName,
                              Class<?> listenerType,
                              Method listenerMethods[],
                              Method addListenerMethod,
                              Method removeListenerMethod)
            throws IntrospectionException {
        protoFeature = new EventSetDescriptor(eventSetName, listenerType, listenerMethods,
                addListenerMethod, removeListenerMethod);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * This constructor builds an EventSetDescriptor from scratch using
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
     * @param getListenerMethod The method on the event source
     *          that can be used to access the array of event listener objects.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     * @since 1.4
     */
    public EventSetBuilder(String eventSetName,
                              Class<?> listenerType,
                              Method listenerMethods[],
                              Method addListenerMethod,
                              Method removeListenerMethod,
                              Method getListenerMethod)
            throws IntrospectionException {
        protoFeature = new EventSetDescriptor(eventSetName, listenerType, listenerMethods,
                addListenerMethod, removeListenerMethod, getListenerMethod);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds an <code>EventSetDescriptor</code> from scratch using
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
    public EventSetBuilder(String eventSetName,
                Class<?> listenerType,
                MethodDescriptor listenerMethodDescriptors[],
                Method addListenerMethod,
                Method removeListenerMethod)
                throws IntrospectionException {
        protoFeature = new EventSetDescriptor(eventSetName, listenerType, listenerMethodDescriptors,
                addListenerMethod, removeListenerMethod);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds a cloned <code>EventSetDescriptor</code>. This isolates
     * the new object from any changes to the old object.
     *
     * @param old the event set descriptor to be copied
     */
    public EventSetBuilder(EventSetDescriptor old) {
        protoFeature = new EventSetDescriptor(old);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds a cloned <code>EventSetDescriptor</code> with new
     * <code>DescriptorData</code>. This isolates the new object from any
     * changes to the old object.
     *
     * @param old the event set descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    public EventSetBuilder(EventSetDescriptor old, DescriptorData newData) {
        protoFeature = new EventSetDescriptor(old, newData);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    // Setters go here...
    /**
     * Mark an event set as unicast (or not).
     *
     * @param unicast <code>True</code> if the event set is unicast.
     * @return this builder, for chaining
     */
    public EventSetBuilder setUnicast(boolean unicast) {
        protoFeature.setUnicast(unicast);
        return this;
    }

    /**
     * Normally event sources are multicast.  However there are some
     * exceptions that are strictly unicast.
     *
     * @return <code>true</code> if the event set is unicast.
     * Defaults to <code>false</code>.
     */
    public boolean isUnicast() {
        return protoFeature.isUnicast();
    }

    /**
     * Marks an event set as being in the &quot;default&quot; set (or not).
     * By default this is <code>true</code>.
     *
     * @param inDefaultEventSet <code>true</code> if the event set is in
     * the &quot;default&quot; set, <code>false</code> if not
     * @return this builder, for chaining
     */
    public EventSetBuilder setInDefaultEventSet(boolean inDefaultEventSet) {
        protoFeature.setInDefaultEventSet(inDefaultEventSet);
        return this;
    }

    /**
     * Reports if an event set is in the &quot;default&quot; set.
     *
     * @return <code>true</code> if the event set is in the
     * &quot;default&quot; set.  Defaults to <code>true</code>.
     */
    public boolean isInDefaultEventSet() {
        return protoFeature.isInDefaultEventSet();
    }

    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the event
     */
    public EventSetBuilder setName(String name) {
        protoFeature.setName(name);
        return this;
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the event
     */
    public String getName() {
        return protoFeature.getName();
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.EVENT_SET</code> to indicate this is an
     * EventSetBuilder object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.EVENT_SET;
    }

    /**
     * Sets the localized display name of this feature.
     *
     * @param displayName  The localized display name for the event.
     */
    public EventSetBuilder setDisplayName(String displayName) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setDisplayName(displayName);
        return this;
    }

    /**
     * Gets the localized display name of this feature. By convention, this
     * will be the same as the programmatic name of the corresponding feature.
     *
     * @return The localized display name for the event.
     */
    public String getDisplayName() {
        return (protoDescriptor == null) ? null : protoDescriptor.getDisplayName();
    }

    /**
     * The "expert" flag is used to distinguish between features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @param expert <code>True</code> if this feature is intended for use
     * by experts only.
     */
    public EventSetBuilder setExpert(boolean expert) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setExpert(expert);
        return this;
    }

    /**
     * The "expert" flag is used to distinguish between those features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @return <code>True</code> if this feature is intended for use
     * by experts only.
     */
    public boolean isExpert() {
        return (protoDescriptor != null) && protoDescriptor.isExpert();
    }

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @param hidden <code>True</code> if this feature should be hidden
     * from human users.
     */
    public EventSetBuilder setHidden(boolean hidden) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setHidden(hidden);
        return this;
    }

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @return <code>True</code> if this feature should be hidden from
     * human users.
     */
    public boolean isHidden() {
        return (protoDescriptor != null) && protoDescriptor.isHidden();
    }

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @param preferred <code>True</code> if this feature should be
     * preferentially shown to human users.
     */
    public EventSetBuilder setPreferred(boolean preferred) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setPreferred(preferred);
        return this;
    }

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @return <code>True</code> if this feature should be preferentially
     * shown to human users.
     */
    public boolean isPreferred() {
        return (protoDescriptor != null) && protoDescriptor.isPreferred();
    }

    /**
     * You can associate a short descriptive string with a feature.  Normally
     * these descriptive strings should be less than about 40 characters.
     *
     * @param text  A (localized) short description to be associated with
     * this event.
     */
    public EventSetBuilder setShortDescription(String text) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setShortDescription(text);
        return this;
    }

    /**
     * Gets the short description of this feature. When the descriptor
     * is built, this will default to be the display name.
     *
     * @return  A localized short description associated with this event.
     */
    public String getShortDescription() {
        return (protoDescriptor == null) ? null : protoDescriptor.getShortDescription();
    }

    /**
     * Determine whether this feature builder has any attributes.
     * Use this to short-circuit any internal table creation.
     *
     * @return <code>true</code> if there are any named attributes
     * in the feature, otherwise <code>false</code>
     */
    public boolean hasAttributes() {
        return (protoDescriptor != null) && protoDescriptor.hasAttributes();
    }

    /**
     * Associate a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @param value  The value.
     */
    public EventSetBuilder setValue(String attributeName, Object value) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setValue(attributeName, value);
        return this;
    }

    /**
     * Retrieve a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @return  The value of the attribute.  May be <code>null</code> if
     *     the attribute is unknown.
     */
    public Object getValue(String attributeName) {
        return (protoDescriptor == null) ? null : protoDescriptor.getValue(attributeName);
    }

    /**
     * Gets an Iterable&lt;String&gt; of the locale-independent names of the
     * feature. If there are no attributes, an empty iterable will be returned.
     *
     * @return  An Iterable&lt;String&gt; of the locale-independent names
     * of any attributes that have been registered.
     */
    public Iterable<String> getAttributeNames() {
        return (protoDescriptor == null) ? Collections.<String>emptyList() : protoDescriptor.getAttributeNames();
    }

    /**
     * Copies all values from the specified attribute table.
     * If any attribute already exists its value will be overridden.
     *
     * @param table  the attribute table with new values
     */
    public EventSetBuilder addTable(Map<String, Object> table) {
        if ((table != null) && !table.isEmpty()) {
            if(protoDescriptor == null) {
                protoDescriptor = new DescriptorData();
            }
            protoDescriptor.addTable(table);
        }
        return this;
    }

    /**
     * Copies all descriptor data from this given object, replacing
     * any existing data already in the builder. This replaces <em>all</em>
     * data in the attribute table with the new table in the supplied
     * descriptor data.
     *
     * @param descriptorData the descriptor data to be copied
     */
    public EventSetBuilder setDescriptorData(DescriptorData descriptorData) {
        if(descriptorData != null) {
            protoDescriptor = (DescriptorData) descriptorData.clone();
        }
        return this;
    }

    /**
     * Return a copy (clone) of the descriptor data in this feature builder.
     * If the descriptor data has not been customized, for instance by
     * suitable setters being called, <code>null</code> will be returned.
     *
     * @return a copy of the descriptor data, or <code>null</code>
     * if no descriptor data is present
     */
    public DescriptorData getDescriptorData() {
        return (protoDescriptor == null) ? null : (DescriptorData) protoDescriptor.clone();
    }

    /**
     * Build the immutable event set descriptor. This method may be called
     * multiple times to build independent event set descriptor objects.
     *
     * @return the newly built event set descriptor
     */
    public EventSetDescriptor build() {
        return new EventSetDescriptor(protoFeature,
                (protoDescriptor == null) ? null : (DescriptorData) protoDescriptor.clone());
    }
}
