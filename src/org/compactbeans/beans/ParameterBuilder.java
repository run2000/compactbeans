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

import java.util.Collections;
import java.util.Map;

/**
 * Builder for ParameterDescriptor classes. This allows controlled building
 * and mutation before creating an immutable ParameterDescriptor class.
 *
 * @author NCull
 * @version 16/06/2014, 6:45 PM
 */
public final class ParameterBuilder implements FeatureBuilder<ParameterDescriptor> {
    private final ParameterDescriptor protoFeature;
    private DescriptorData protoDescriptor;

    /**
     * Public no-argument constructor.
     */
    public ParameterBuilder() {
        protoFeature = new ParameterDescriptor();
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Public constructor that takes a name.
     *
     * @param name the name of the parameter
     */
    public ParameterBuilder(String name) {
        protoFeature = new ParameterDescriptor(name);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds a cloned <code>ParameterDescriptor</code>. This isolates
     * the new object from any changes to the old object.
     *
     * @param old the parameter descriptor to be copied
     */
    public ParameterBuilder(ParameterDescriptor old) {
        protoFeature = new ParameterDescriptor(old);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Builds a cloned <code>ParameterDescriptor</code> with new
     * <code>DescriptorData</code>. This isolates the new object from
     * any changes to the old object.
     *
     * @param old the parameter descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    public ParameterBuilder(ParameterDescriptor old, DescriptorData newData) {
        protoFeature = new ParameterDescriptor(old, newData);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    // Setters go here...
    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the parameter
     */
    public ParameterBuilder setName(String name) {
        protoFeature.setName(name);
        return this;
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the parameter
     */
    public String getName() {
        return protoFeature.getName();
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.PARAMETER</code> to indicate this is a
     * ParameterBuilder object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.PARAMETER;
    }

    /**
     * Sets the localized display name of this feature.
     *
     * @param displayName  The localized display name for the parameter.
     */
    public ParameterBuilder setDisplayName(String displayName) {
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
     * @return The localized display name for the parameter.
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
    public ParameterBuilder setExpert(boolean expert) {
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
     * @param hidden <code>True</code> if this feature should be hidden from
     * human users.
     */
    public ParameterBuilder setHidden(boolean hidden) {
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
    public ParameterBuilder setPreferred(boolean preferred) {
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
     * this parameter.
     */
    public ParameterBuilder setShortDescription(String text) {
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
     * @return  A localized short description associated with this parameter.
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
    public ParameterBuilder setValue(String attributeName, Object value) {
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
    public ParameterBuilder addTable(Map<String, Object> table) {
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
    public ParameterBuilder setDescriptorData(DescriptorData descriptorData) {
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
     * Build the immutable parameter descriptor. This method may be called
     * multiple times to build independent parameter descriptor objects.
     *
     * @return the newly built parameter descriptor
     */
    public ParameterDescriptor build() {
        return new ParameterDescriptor(protoFeature,
                (protoDescriptor == null) ? null : (DescriptorData) protoDescriptor.clone());
    }
}
