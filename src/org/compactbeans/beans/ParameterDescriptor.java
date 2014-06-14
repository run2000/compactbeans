/*
 * Copyright (c) 1996, 1997, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Enumeration;

/**
 * The ParameterDescriptor class allows bean implementors to provide
 * additional information on each of their parameters, beyond the
 * low level type information provided by the
 * <code>java.lang.reflect.Method</code> class.
 * <p>
 * Currently all our state comes from the DescriptorData mixin class.</p>
 */

public final class ParameterDescriptor implements FeatureDescriptor {
    private final String name;
    private final DescriptorData descriptorData;

    /**
     * Public constructor that takes a name.
     *
     * @param name the name of the parameter
     */
    public ParameterDescriptor(String name) {
        this.name = name;
        this.descriptorData = null;
    }

    /**
     * Public constructor that takes a name and descriptor data.
     *
     * @param name the name of the parameter
     * @param descriptorData the descriptor data for this parameter descriptor,
     *                       possibly <code>null</code>
     */
    public ParameterDescriptor(String name, DescriptorData descriptorData) {
        this.name = name;
        this.descriptorData = descriptorData;
    }

    /**
     * Package private dup constructor.
     * This must isolate the new object from any changes to the old object.
     */
    ParameterDescriptor(ParameterDescriptor old) {
        name = old.name;
        descriptorData = old.getDescriptorData();
    }

    /**
     * Duplicate constructor, with new <code>DescriptorData</code>.
     * This isolates the new object from any changes to the old object.
     *
     * @param old the method descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    public ParameterDescriptor(ParameterDescriptor old, DescriptorData newData) {
        name = old.name;
        descriptorData = newData;
    }

    public String getName() {
        return name;
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.PARAMETER</code> to indicate this is a
     * ParameterDescriptor object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.PARAMETER;
    }

    public String getDisplayName() {
        String displayName = (descriptorData == null) ? null : descriptorData.getDisplayName();
        return (displayName == null) ? name : displayName;
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
        String description = (descriptorData == null) ? null : descriptorData.getShortDescription();
        return (description == null) ? getDisplayName() : description;
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
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[name=").append(this.name).append(']');
        return sb.toString();
    }
}
