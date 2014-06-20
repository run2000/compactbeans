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

import java.util.Collections;
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
    private String name;
    private final DescriptorData descriptorData;

    /**
     * No-arg constructor.
     */
    ParameterDescriptor() {
        this.descriptorData = null;
    }

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
    ParameterDescriptor(ParameterDescriptor old, DescriptorData newData) {
        name = old.name;
        descriptorData = newData;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the parameter
     */
    void setName(String name) {
        this.name = name;
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

    /**
     * Gets the localized display name of this feature. By convention, this
     * will be the same as the programmatic name of the corresponding feature.
     *
     * @return The localized display name for the parameter.
     */
    public String getDisplayName() {
        String displayName = (descriptorData == null) ? null : descriptorData.getDisplayName();
        return (displayName == null) ? name : displayName;
    }

    /**
     * The "expert" flag is used to distinguish between those features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @return <code>True</code> if this feature is intended for use
     * by experts only.
     */
    public boolean isExpert() {
        return (descriptorData != null) && descriptorData.isExpert();
    }

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @return <code>True</code> if this feature should be hidden from
     * human users.
     */
    public boolean isHidden() {
        return (descriptorData != null) && descriptorData.isHidden();
    }

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @return <code>True</code> if this feature should be preferentially
     * shown to human users.
     */
    public boolean isPreferred() {
        return (descriptorData != null) && descriptorData.isPreferred();
    }

    /**
     * Gets the short description of this feature.
     *
     * @return  A localized short description associated with this
     *   property/method/event.  This defaults to be the display name.
     */
    public String getShortDescription() {
        String description = (descriptorData == null) ? null : descriptorData.getShortDescription();
        return (description == null) ? getDisplayName() : description;
    }

    /**
     * Determine whether this feature descriptor has any attributes.
     * Use this to short-circuit any internal table creation.
     *
     * @return <code>true</code> if there are any named attributes
     * in this feature, otherwise <code>false</code>
     */
    public boolean hasAttributes() {
        return (descriptorData != null) && descriptorData.hasAttributes();
    }

    /**
     * Retrieve a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @return  The value of the attribute.  May be <code>null</code> if
     *     the attribute is unknown.
     */
    public Object getValue(String attributeName) {
        return (descriptorData == null) ? null : descriptorData.getValue(attributeName);
    }

    /**
     * Gets an enumeration of the locale-independent names of this
     * feature. If there are no attributes, an empty enumeration
     * will be returned.
     *
     * @return  An enumeration of the locale-independent names of any
     *    attributes that have been registered.
     */
    public Enumeration<String> attributeNames() {
        return (descriptorData == null) ? Collections.<String>emptyEnumeration() : descriptorData.attributeNames();
    }

    /**
     * Return a copy (clone) of the descriptor data in this feature.
     * If the descriptor data has not been customized, for instance by a
     * suitable <code>BeanInfo</code> object, <code>null</code> will be
     * returned.
     *
     * @return a copy of the descriptor data, or <code>null</code>
     * if no descriptor data is present
     */
    public DescriptorData getDescriptorData() {
        return (descriptorData == null) ? null : (DescriptorData) descriptorData.clone();
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
