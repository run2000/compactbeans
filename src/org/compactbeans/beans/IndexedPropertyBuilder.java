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
 * Builder for IndexedPropertyDescriptor classes. This allows controlled building
 * and mutation before creating an immutable IndexedPropertyDescriptor class.
 *
 * @author NCull
 * @version 16/06/2014, 6:50 PM
 */
public final class IndexedPropertyBuilder implements FeatureBuilder<IndexedPropertyDescriptor> {
    private final IndexedPropertyDescriptor protoFeature;
    private DescriptorData protoDescriptor;

    /**
     * This builds an IndexedPropertyDescriptor for a property
     * that follows the standard Java conventions by having getFoo and setFoo
     * accessor methods, for both indexed access and array access.
     * <p>
     * Thus if the argument name is "fred", it will assume that there
     * is an indexed reader method "getFred", a non-indexed (array) reader
     * method also called "getFred", an indexed writer method "setFred",
     * and finally a non-indexed writer method "setFred".</p>
     *
     * @param propertyName The programmatic name of the property.
     * @param beanClass    The Class object for the target bean.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public IndexedPropertyBuilder(String propertyName, Class<?> beanClass)
            throws IntrospectionException {
        protoFeature = new IndexedPropertyDescriptor(propertyName, beanClass);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * This builds by taking the name of a simple property, and
     * <code>Method</code> objects for reading and writing the property.
     *
     * @param propertyName       The programmatic name of the property.
     * @param readMethod         The method used for reading the property values as an array.
     *                           May be <code>null</code> if the property is write-only or must be indexed.
     * @param writeMethod        The method used for writing the property values as an array.
     *                           May be <code>null</code> if the property is read-only or must be indexed.
     * @param indexedReadMethod  The method used for reading an indexed property value.
     *                           May be <code>null</code> if the property is write-only.
     * @param indexedWriteMethod The method used for writing an indexed property value.
     *                           May be <code>null</code> if the property is read-only.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public IndexedPropertyBuilder(String propertyName, Method readMethod, Method writeMethod,
                                  Method indexedReadMethod, Method indexedWriteMethod)
            throws IntrospectionException {
        protoFeature = new IndexedPropertyDescriptor(propertyName, readMethod, writeMethod, indexedReadMethod, indexedWriteMethod);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * This builds by taking the name of a simple property, and method
     * names for reading and writing the property, both indexed
     * and non-indexed.
     *
     * @param propertyName           The programmatic name of the property.
     * @param beanClass              The Class object for the target bean.
     * @param readMethodName         The name of the method used for reading the property
     *                               values as an array.  May be <code>null</code> if the property
     *                               is write-only or must be indexed.
     * @param writeMethodName        The name of the method used for writing the property
     *                               values as an array.  May be <code>null</code> if the property
     *                               is read-only or must be indexed.
     * @param indexedReadMethodName  The name of the method used for reading
     *                               an indexed property value.
     *                               May be <code>null</code> if the property is write-only.
     * @param indexedWriteMethodName The name of the method used for writing
     *                               an indexed property value.
     *                               May be <code>null</code> if the property is read-only.
     * @throws IntrospectionException if an exception occurs during
     *                                introspection.
     */
    public IndexedPropertyBuilder(String propertyName, Class<?> beanClass,
                                  String readMethodName, String writeMethodName,
                                  String indexedReadMethodName, String indexedWriteMethodName)
            throws IntrospectionException {
        protoFeature = new IndexedPropertyDescriptor(propertyName, beanClass, readMethodName, writeMethodName,
                indexedReadMethodName, indexedWriteMethodName);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds a cloned <code>IndexedPropertyDescriptor</code>. This isolates
     * the new object from any changes to the old object.
     *
     * @param old     the indexed property descriptor to be copied
     */
    public IndexedPropertyBuilder(IndexedPropertyDescriptor old) {
        protoFeature = new IndexedPropertyDescriptor(old);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds a cloned <code>IndexedPropertyDescriptor</code> with new
     * <code>DescriptorData</code>. This isolates the new object from
     * any changes to the old object.
     *
     * @param old     the indexed property descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    public IndexedPropertyBuilder(IndexedPropertyDescriptor old, DescriptorData newData) {
        protoFeature = new IndexedPropertyDescriptor(old, newData);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    // Setters go here...

    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the indexed property
     */
    public IndexedPropertyBuilder setName(String name) {
        protoFeature.setName(name);
        return this;
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the indexed property
     */
    public String getName() {
        return protoFeature.getName();
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.INDEXED_PROPERTY</code> to indicate
     * this is an IndexedPropertyBuilder object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.INDEXED_PROPERTY;
    }

    /**
     * Sets the localized display name of this feature.
     *
     * @param displayName The localized display name for the indexed property.
     */
    public IndexedPropertyBuilder setDisplayName(String displayName) {
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
     * @return The localized display name for the indexed property.
     */
    public String getDisplayName() {
        return (protoDescriptor == null) ? null : protoDescriptor.getDisplayName();
    }

    /**
     * Updates to "bound" properties will cause an "IndexedPropertyChange"
     * event to be fired when the property is changed.
     *
     * @param bound True if this is a bound property.
     * @return this builder, for chaining
     */
    public IndexedPropertyBuilder setBound(boolean bound) {
        protoFeature.setBound(bound);
        return this;
    }

    /**
     * Updates to "bound" properties will cause an "IndexedPropertyChange"
     * event to get fired when the property is changed.
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
     * @param constrained True if this is a constrained property.
     * @return this builder, for chaining
     */
    public IndexedPropertyBuilder setConstrained(boolean constrained) {
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
    public IndexedPropertyBuilder setReadMethod(Method readMethod) throws IntrospectionException {
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
    public IndexedPropertyBuilder setWriteMethod(Method writeMethod) throws IntrospectionException {
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
     * Sets the method that should be used to read an indexed property value.
     *
     * @param readMethod The new indexed read method.
     * @return this builder, for chaining
     * @throws IntrospectionException if the indexed read method is invalid
     */
    public IndexedPropertyBuilder setIndexedReadMethod(Method readMethod)
            throws IntrospectionException {
        protoFeature.setIndexedReadMethod(readMethod);
        return this;
    }

    /**
     * Gets the method that should be used to read an indexed
     * property value. May return <code>null</code> if the property isn't
     * indexed or is write-only.
     *
     * @return The method that should be used to read an indexed
     * property value.
     */
    public Method getIndexedReadMethod() {
        return protoFeature.getIndexedReadMethod();
    }

    /**
     * Sets the method that should be used to write an indexed property value.
     *
     * @param writeMethod The new indexed write method.
     * @return this builder, for chaining
     * @throws IntrospectionException if the indexed write method is invalid
     */
    public IndexedPropertyBuilder setIndexedWriteMethod(Method writeMethod)
            throws IntrospectionException {
        protoFeature.setIndexedWriteMethod(writeMethod);
        return this;
    }

    /**
     * Gets the method that should be used to write an indexed property value.
     * May return <code>null</code> if the property isn't indexed
     * or is read-only.
     *
     * @return The method that should be used to write an indexed
     * property value.
     */
    public Method getIndexedWriteMethod() {
        return protoFeature.getIndexedWriteMethod();
    }

    /**
     * The "expert" flag is used to distinguish between features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @param expert True if this feature is intended for use by experts only.
     */
    public IndexedPropertyBuilder setExpert(boolean expert) {
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
     * @param hidden  True if this feature should be hidden from human users.
     */
    public IndexedPropertyBuilder setHidden(boolean hidden) {
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
    public IndexedPropertyBuilder setPreferred(boolean preferred) {
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
     * this indexed property.
     */
    public IndexedPropertyBuilder setShortDescription(String text) {
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
     * @return  A localized short description associated with this
     * indexed property.
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
    public IndexedPropertyBuilder setValue(String attributeName, Object value) {
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
    public IndexedPropertyBuilder addTable(Map<String, Object> table) {
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
    public IndexedPropertyBuilder setDescriptorData(DescriptorData descriptorData) {
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
     * Build the immutable indexed property descriptor. This method may be
     * called multiple times to build independent indexed property descriptor
     * objects.
     *
     * @return the newly built indexed property descriptor
     */
    public IndexedPropertyDescriptor build() {
        return new IndexedPropertyDescriptor(protoFeature,
                (protoDescriptor == null) ? null : (DescriptorData) protoDescriptor.clone());
    }
}
