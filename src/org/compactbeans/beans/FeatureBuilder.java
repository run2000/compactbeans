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

import java.util.Map;

/**
 * Builder interface for FeatureDescriptor classes. This allows controlled
 * building and mutation before creating an immutable FeatureDescriptor class.
 *
 * @author NCull
 * @version 16/06/2014, 8:26 PM
 */
public interface FeatureBuilder <T extends FeatureDescriptor> {

    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the bean/property/method/event
     * @return this builder, for chaining
     */
    FeatureBuilder<T> setName(String name);

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the bean/property/method/event
     */
    String getName();

    /**
     * Gets the descriptor type for this object.
     *
     * @return one of the <code>DescriptorType</code> types to indicate
     * the object type of the implementation class
     */
    DescriptorType getDescriptorType();

    /**
     * Sets the localized display name of this feature.
     *
     * @param displayName The localized display name for the
     *                    bean/property/method/event.
     * @return this builder, for chaining
     */
    FeatureBuilder<T> setDisplayName(String displayName);

    /**
     * Gets the localized display name of this feature. By convention, this
     * will be the same as the programmatic name of the corresponding feature.
     *
     * @return The localized display name for the bean/property/method/event.
     */
    String getDisplayName();

    /**
     * The "expert" flag is used to distinguish between features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @param expert <code>True</code> if this feature is intended for use
     * by experts only.
     * @return this builder, for chaining
     */
    FeatureBuilder<T> setExpert(boolean expert);

    /**
     * The "expert" flag is used to distinguish between those features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @return <code>True</code> if this feature is intended for use
     * by experts only.
     */
    boolean isExpert();

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @param hidden  <code>True</code> if this feature should be hidden
     * from human users.
     * @return this builder, for chaining
     */
    FeatureBuilder<T> setHidden(boolean hidden);

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @return <code>True</code> if this feature should be hidden from
     * human users.
     */
    boolean isHidden();

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @param preferred  <code>True</code> if this feature should be
     * preferentially shown to human users.
     * @return this builder, for chaining
     */
    FeatureBuilder<T> setPreferred(boolean preferred);

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @return <code>True</code> if this feature should be preferentially
     * shown to human users.
     */
    boolean isPreferred();

    /**
     * You can associate a short descriptive string with a feature.  Normally
     * these descriptive strings should be less than about 40 characters.
     *
     * @param text  A (localized) short description to be associated with
     * this property/method/event.
     * @return this builder, for chaining
     */
    FeatureBuilder<T> setShortDescription(String text);

    /**
     * Gets the short description of this feature. When the descriptor
     * is built, this will default to be the display name.
     *
     * @return  A localized short description associated with this
     * bean/property/method/event.
     */
    String getShortDescription();

    /**
     * Determine whether this feature builder has any attributes.
     * Use this to short-circuit any internal table creation.
     *
     * @return <code>true</code> if there are any named attributes
     * in the feature, otherwise <code>false</code>
     */
    public boolean hasAttributes();

    /**
     * Associate a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @param value  The value.
     * @return this builder, for chaining
     */
    FeatureBuilder<T> setValue(String attributeName, Object value);

    /**
     * Retrieve a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @return  The value of the attribute.  May be <code>null</code> if
     *     the attribute is unknown.
     */
    Object getValue(String attributeName);

    /**
     * Gets an Iterable&lt;String&gt; of the locale-independent names of the
     * feature. If there are no attributes, an empty iterable will be returned.
     *
     * @return  An Iterable&lt;String&gt; of the locale-independent names
     * of any attributes that have been registered.
     */
    Iterable<String> getAttributeNames();

    /**
     * Copies all values from the specified attribute table.
     * If any attribute already exists its value will be overridden.
     *
     * @param table  the attribute table with new values
     * @return this builder, for chaining
     */
    FeatureBuilder<T> addTable(Map<String, Object> table);

    /**
     * Copies all descriptor data from this given object, replacing
     * any existing data already in the builder. This replaces <em>all</em>
     * data in the attribute table with the new table in the supplied
     * descriptor data.
     *
     * @param descriptorData the descriptor data to be copied
     * @return this builder, for chaining
     */
    FeatureBuilder<T> setDescriptorData(DescriptorData descriptorData);

    /**
     * Return a copy (clone) of the descriptor data in this feature builder.
     * If the descriptor data has not been customized, for instance by
     * suitable setters being called, <code>null</code> will be returned.
     *
     * @return a copy of the descriptor data, or <code>null</code>
     * if no descriptor data is present
     */
    DescriptorData getDescriptorData();

    /**
     * Build the immutable feature. This method may be called multiple times
     * to build independent feature objects.
     *
     * @return the newly built feature
     */
    T build();

    /**
     * Generate an array of event set descriptor objects from a parameter array
     * of builders.
     * <p>
     * This is a convenience method to batch-convert event set builders
     * into an array of event set descriptors suitable for returning in a
     * <code>BeanInfo</code> object.</p>
     *
     * @param builders the builders to build into the resulting array of
     * event set descriptors
     * @return the resulting array of event set descriptors
     */
    @SafeVarargs
    public static EventSetDescriptor[] toEventSetArray(FeatureBuilder<? extends EventSetDescriptor>... builders) {
        EventSetDescriptor[] result = new EventSetDescriptor[builders.length];
        for(int i = 0; i < builders.length; i++) {
            FeatureBuilder<? extends EventSetDescriptor> builder = builders[i];
            if(builder != null) {
                result[i] = builder.build();
            }
        }
        return result;
    }

    /**
     * Generate an array of method descriptor objects from a parameter array
     * of builders.
     * <p>
     * This is a convenience method to batch-convert method builders
     * into an array of method descriptors suitable for returning in a
     * <code>BeanInfo</code> object.</p>
     *
     * @param builders the builders to build into the resulting array of
     * method descriptors
     * @return the resulting array of method descriptors
     */
    @SafeVarargs
    static MethodDescriptor[] toMethodArray(FeatureBuilder<? extends MethodDescriptor>... builders) {
        MethodDescriptor[] result = new MethodDescriptor[builders.length];
        for(int i = 0; i < builders.length; i++) {
            FeatureBuilder<? extends MethodDescriptor> builder = builders[i];
            if(builder != null) {
                result[i] = builder.build();
            }
        }
        return result;
    }

    /**
     * Generate an array of parameter descriptor objects from a parameter array
     * of builders.
     * <p>
     * This is a convenience method to batch-convert parameter builders
     * into an array of parameter descriptors suitable for passing into a
     * <code>MethodBuilder</code> or <code>MethodDescriptor</code> object.</p>
     *
     * @param builders the builders to build into the resulting array of
     * parameter descriptors
     * @return the resulting array of parameter descriptors
     */
    @SafeVarargs
    static ParameterDescriptor[] toParameterArray(FeatureBuilder<? extends ParameterDescriptor>... builders) {
        ParameterDescriptor[] result = new ParameterDescriptor[builders.length];
        for(int i = 0; i < builders.length; i++) {
            FeatureBuilder<? extends ParameterDescriptor> builder = builders[i];
            if(builder != null) {
                result[i] = builder.build();
            }
        }
        return result;
    }

    /**
     * Generate an array of property descriptor objects from a parameter array
     * of builders.
     * <p>
     * This is a convenience method to batch-convert property builders
     * (including indexed property builders) into an array of property
     * descriptors suitable for returning in a <code>BeanInfo</code> object.
     * </p>
     *
     * @param builders the builders to build into the resulting array of
     * property descriptors
     * @return the resulting array of property descriptors
     */
    @SafeVarargs
    static PropertyDescriptor[] toPropertyArray(FeatureBuilder<? extends PropertyDescriptor>... builders) {
        PropertyDescriptor[] result = new PropertyDescriptor[builders.length];
        for(int i = 0; i < builders.length; i++) {
            FeatureBuilder<? extends PropertyDescriptor> builder = builders[i];
            if(builder != null) {
                result[i] = builder.build();
            }
        }
        return result;
    }
}
