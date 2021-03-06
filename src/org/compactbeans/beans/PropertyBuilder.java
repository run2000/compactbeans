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
 * Builder for PropertyDescriptor classes. This allows controlled building
 * and mutation before creating an immutable PropertyDescriptor class.
 *
 * @author NCull
 * @version 16/06/2014, 6:50 PM
 */
public final class PropertyBuilder implements FeatureBuilder<PropertyDescriptor> {
    private final PropertyDescriptor protoFeature;
    private DescriptorData protoDescriptor;

    /**
     * Builds a PropertyDescriptor for a property that follows
     * the standard Java convention by having getFoo and setFoo
     * accessor methods.  Thus if the argument name is "fred", it will
     * assume that the writer method is "setFred" and the reader method
     * is "getFred" (or "isFred" for a boolean property).  Note that the
     * property name should start with a lower case character, which will
     * be capitalized in the method names.
     *
     * @param propertyName The programmatic name of the property.
     * @param beanClass    The Class object for the target bean.  For
     *                     example sun.beans.OurButton.class.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public PropertyBuilder(String propertyName, Class<?> beanClass)
            throws IntrospectionException {
        protoFeature = new PropertyDescriptor(propertyName, beanClass);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * This builds by taking the name of a simple property, and
     * <code>Method</code> objects for reading and writing the property.
     *
     * @param propertyName The programmatic name of the property.
     * @param readMethod   The method used for reading the property value.
     *                     May be <code>null</code> if the property is write-only.
     * @param writeMethod  The method used for writing the property value.
     *                     May be <code>null</code> if the property is read-only.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public PropertyBuilder(String propertyName, Method readMethod, Method writeMethod)
            throws IntrospectionException {
        protoFeature = new PropertyDescriptor(propertyName, readMethod, writeMethod);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * This builds by taking the name of a simple property, and method
     * names for reading and writing the property.
     *
     * @param propertyName    The programmatic name of the property.
     * @param beanClass       The Class object for the target bean.  For
     *                        example sun.beans.OurButton.class.
     * @param readMethodName  The name of the method used for reading the property
     *                        value.  May be <code>null</code> if the property is write-only.
     * @param writeMethodName The name of the method used for writing the property
     *                        value.  May be <code>null</code> if the property is read-only.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public PropertyBuilder(String propertyName, Class<?> beanClass,
                           String readMethodName, String writeMethodName)
            throws IntrospectionException {
        protoFeature = new PropertyDescriptor(propertyName, beanClass, readMethodName, writeMethodName);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds a cloned <code>PropertyDescriptor</code>. This isolates
     * the new object from any changes to the old object.
     *
     * @param old     the property descriptor to be copied
     */
    public PropertyBuilder(PropertyDescriptor old) {
        protoFeature = new PropertyDescriptor(old);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds a cloned <code>PropertyDescriptor</code> with new
     * <code>DescriptorData</code>. This isolates the new object from
     * any changes to the old object.
     *
     * @param old     the property descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    public PropertyBuilder(PropertyDescriptor old, DescriptorData newData) {
        protoFeature = new PropertyDescriptor(old, newData);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    // Setters go here...

    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the property
     */
    public PropertyBuilder setName(String name) {
        protoFeature.setName(name);
        return this;
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the property
     */
    public String getName() {
        return protoFeature.getName();
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.PROPERTY</code> to indicate this is a
     * PropertyBuilder object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.PROPERTY;
    }

    /**
     * Sets the localized display name of this feature.
     *
     * @param displayName The localized display name for the
     *                    property.
     */
    public PropertyBuilder setDisplayName(String displayName) {
        if (protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setDisplayName(displayName);
        return this;
    }

    /**
     * Gets the localized display name of this feature. By convention, this
     * will be the same as the programmatic name of the corresponding feature.
     *
     * @return The localized display name for the property.
     */
    public String getDisplayName() {
        return (protoDescriptor == null) ? null : protoDescriptor.getDisplayName();
    }

    /**
     * Updates to "bound" properties will cause a "PropertyChange" event to
     * get fired when the property is changed.
     *
     * @param bound <code>True</code> if this is a bound property.
     * @return this builder, for chaining
     */
    public PropertyBuilder setBound(boolean bound) {
        protoFeature.setBound(bound);
        return this;
    }

    /**
     * Updates to "bound" properties will cause a "PropertyChange" event to
     * get fired when the property is changed.
     *
     * @return <code>true</code> if this is a bound property.
     */
    public boolean isBound() {
        return protoFeature.isBound();
    }

    /**
     * Attempted updates to "Constrained" properties will cause a "VetoableChange"
     * event to get fired when the property is changed.
     *
     * @param constrained <code>True</code> if this is a constrained property.
     * @return this builder, for chaining
     */
    public PropertyBuilder setConstrained(boolean constrained) {
        protoFeature.setConstrained(constrained);
        return this;
    }

    /**
     * Attempted updates to "Constrained" properties will cause a "VetoableChange"
     * event to get fired when the property is changed.
     *
     * @return <code>True</code> if this is a constrained property.
     */
    public boolean isConstrained() {
        return protoFeature.isConstrained();
    }

    /**
     * Sets the method that should be used to read the property value.
     *
     * @param readMethod The new read method.
     * @return this builder, for chaining
     * @throws IntrospectionException if the read method is invalid
     */
    public PropertyBuilder setReadMethod(Method readMethod) throws IntrospectionException {
        protoFeature.setReadMethod(readMethod);
        return this;
    }

    /**
     * Gets the method that should be used to read the property value.
     *
     * @return The method that should be used to read the property value.
     * May return <code>null</code> if the property can't be read.
     */
    public Method getReadMethod() {
        return protoFeature.getReadMethod();
    }

    /**
     * Sets the method that should be used to write the property value.
     *
     * @param writeMethod The new write method.
     * @return this builder, for chaining
     * @throws IntrospectionException if the write method is invalid
     */
    public PropertyBuilder setWriteMethod(Method writeMethod) throws IntrospectionException {
        protoFeature.setWriteMethod(writeMethod);
        return this;
    }

    /**
     * Gets the method that should be used to write the property value.
     *
     * @return The method that should be used to write the property value.
     * May return <code>null</code> if the property can't be written.
     */
    public Method getWriteMethod() {
        return protoFeature.getWriteMethod();
    }

    /**
     * The "expert" flag is used to distinguish between features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @param expert <code>True</code> if this feature is intended for use
     * by experts only.
     */
    public PropertyBuilder setExpert(boolean expert) {
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
    public PropertyBuilder setHidden(boolean hidden) {
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
     * @param preferred  <code>True</code> if this feature should be
     * preferentially shown to human users.
     */
    public PropertyBuilder setPreferred(boolean preferred) {
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
     * this property.
     */
    public PropertyBuilder setShortDescription(String text) {
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
     * @return  A localized short description associated with this property.
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
    public PropertyBuilder setValue(String attributeName, Object value) {
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
    public PropertyBuilder addTable(Map<String, Object> table) {
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
    public PropertyBuilder setDescriptorData(DescriptorData descriptorData) {
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
     * Build the immutable property descriptor. This method may be called
     * multiple times to build independent property descriptor objects.
     *
     * @return the newly built property descriptor
     */
    public PropertyDescriptor build() {
        return new PropertyDescriptor(protoFeature,
                (protoDescriptor == null) ? null : (DescriptorData) protoDescriptor.clone());
    }
}
