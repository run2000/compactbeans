/*
 * Copyright (c) 1996, 2010, Oracle and/or its affiliates. All rights reserved.
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
 * The FeatureDescriptor interface is the common interface for
 * <code>PropertyDescriptor</code>, <code>EventSetDescriptor</code>,
 * and <code>MethodDescriptor</code>, etc.
 * <p>
 * It supports some common information that can be retrieved for any of the
 * introspection descriptors.</p>
 */
public interface FeatureDescriptor {

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
     * Gets the localized display name of this feature. By convention, this
     * will be the same as the programmatic name of the corresponding feature.
     *
     * @return The localized display name for the property/method/event.
     */
    public String getDisplayName();

    /**
     * The "expert" flag is used to distinguish between those features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @return <code>True</code> if this feature is intended for use
     * by experts only.
     */
    public boolean isExpert();

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @return <code>True</code> if this feature should be hidden from
     * human users.
     */
    public boolean isHidden();

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @return <code>True</code> if this feature should be preferentially
     * shown to human users.
     */
    public boolean isPreferred();

    /**
     * Gets the short description of this feature.
     *
     * @return  A localized short description associated with this
     *   property/method/event.  This defaults to be the display name.
     */
    public String getShortDescription();

    /**
     * Determine whether this feature descriptor has any attributes.
     * Use this to short-circuit any internal table creation.
     *
     * @return <code>true</code> if there are any named attributes
     * in this feature, otherwise <code>false</code>
     */
    public boolean hasAttributes();

    /**
     * Retrieve a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @return  The value of the attribute.  May be <code>null</code> if
     *     the attribute is unknown.
     */
    public Object getValue(String attributeName);

    /**
     * Gets an enumeration of the locale-independent names of this
     * feature. If there are no attributes, an empty enumeration
     * will be returned.
     *
     * @return  An enumeration of the locale-independent names of any
     *    attributes that have been registered.
     */
    public Enumeration<String> attributeNames();

}
