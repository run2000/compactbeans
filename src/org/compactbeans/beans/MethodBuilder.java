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
 * Builder for MethodDescriptor classes. This allows controlled building
 * and mutation before creating an immutable MethodDescriptor class.
 *
 * @author NCull
 * @version 16/06/2014, 6:31 PM
 */
public final class MethodBuilder implements FeatureBuilder<MethodDescriptor> {
    private MethodDescriptor protoFeature;
    private DescriptorData protoDescriptor;

    /**
     * Build a <code>MethodDescriptor</code> from a
     * <code>Method</code>.
     *
     * @param method The low-level method information.
     */
    public MethodBuilder(Method method) {
        protoFeature = new MethodDescriptor(method);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Build a <code>MethodDescriptor</code> from a
     * <code>Method</code> providing descriptive information for each
     * of the method's parameters.
     *
     * @param method    The low-level method information.
     * @param parameterDescriptors  Descriptive information for each of the
     *                          method's parameters.
     */
    public MethodBuilder(Method method,
                            ParameterDescriptor parameterDescriptors[]) {
        protoFeature = new MethodDescriptor(method, parameterDescriptors);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Build a cloned <code>MethodDescriptor</code>. This isolates
     * the new object from any changes to the old object.
     *
     * @param old the method descriptor to be copied
     */
    public MethodBuilder(MethodDescriptor old) {
        protoFeature = new MethodDescriptor(old);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    /**
     * Build a cloned <code>MethodDescriptor</code> with new
     * <code>DescriptorData</code>. This isolates the new object from
     * any changes to the old object.
     *
     * @param old the method descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    public MethodBuilder(MethodDescriptor old, DescriptorData newData) {
        protoFeature = new MethodDescriptor(old, newData);
        protoDescriptor = protoFeature.getDescriptorData();
    }

    // Setters go here...
    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the method
     */
    public MethodBuilder setName(String name) {
        protoFeature.setName(name);
        return this;
    }

    public String getName() {
        return protoFeature.getName();
    }

    /**
     * Sets the localized display name of this feature.
     *
     * @param displayName  The localized display name for the method.
     */
    public MethodBuilder setDisplayName(String displayName) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setDisplayName(displayName);
        return this;
    }

    public String getDisplayName() {
        return (protoDescriptor == null) ? null : protoDescriptor.getDisplayName();
    }

    /**
     * The "expert" flag is used to distinguish between features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @param expert True if this feature is intended for use by experts only.
     */
    public MethodBuilder setExpert(boolean expert) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setExpert(expert);
        return this;
    }

    public boolean isExpert() {
        return (protoDescriptor != null) && protoDescriptor.isExpert();
    }

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @param hidden  True if this feature should be hidden from human users.
     */
    public MethodBuilder setHidden(boolean hidden) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setHidden(hidden);
        return this;
    }

    public boolean isHidden() {
        return (protoDescriptor != null) && protoDescriptor.isHidden();
    }

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @param preferred  True if this feature should be preferentially shown
     *                   to human users.
     */
    public MethodBuilder setPreferred(boolean preferred) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setPreferred(preferred);
        return this;
    }

    public boolean isPreferred() {
        return (protoDescriptor != null) && protoDescriptor.isPreferred();
    }

    /**
     * You can associate a short descriptive string with a feature.  Normally
     * these descriptive strings should be less than about 40 characters.
     *
     * @param text  A (localized) short description to be associated with
     * this method.
     */
    public MethodBuilder setShortDescription(String text) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setShortDescription(text);
        return this;
    }

    public String getShortDescription() {
        return (protoDescriptor == null) ? null : protoDescriptor.getShortDescription();
    }

    public boolean hasAttributes() {
        return (protoDescriptor != null) && protoDescriptor.hasAttributes();
    }

    /**
     * Associate a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @param value  The value.
     */
    public MethodBuilder setValue(String attributeName, Object value) {
        if(protoDescriptor == null) {
            protoDescriptor = new DescriptorData();
        }
        protoDescriptor.setValue(attributeName, value);
        return this;
    }

    public Object getValue(String attributeName) {
        return (protoDescriptor == null) ? null : protoDescriptor.getValue(attributeName);
    }

    public Iterable<String> getAttributeNames() {
        return (protoDescriptor == null) ? Collections.<String>emptyList() : protoDescriptor.getAttributeNames();
    }

    /**
     * Copies all values from the specified attribute table.
     * If any attribute already exists its value will be overridden.
     *
     * @param table  the attribute table with new values
     */
    public MethodBuilder addTable(Map<String, Object> table) {
        if ((table != null) && !table.isEmpty()) {
            if(protoDescriptor == null) {
                protoDescriptor = new DescriptorData();
            }
            protoDescriptor.addTable(table);
        }
        return this;
    }

    /**
     * Copies all descriptor data from this given object.
     *
     * @param descriptorData the descriptor data to be copied
     */
    public MethodBuilder setDescriptorData(DescriptorData descriptorData) {
        if(descriptorData != null) {
            protoDescriptor = (DescriptorData) descriptorData.clone();
        }
        return this;
    }

    public DescriptorData getDescriptorData() {
        return (protoDescriptor == null) ? null : (DescriptorData) protoDescriptor.clone();
    }

    /**
     * Build the immutable method descriptor. This method may be called
     * multiple times to build independent method descriptor objects.
     *
     * @return the newly built method descriptor
     */
    public MethodDescriptor build() {
        return new MethodDescriptor(protoFeature,
                (protoDescriptor == null) ? null : (DescriptorData) protoDescriptor.clone());
    }
}
