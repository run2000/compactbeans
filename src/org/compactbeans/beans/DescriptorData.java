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

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * The DescriptorData class provides some common information that can be
 * set and retrieved for any of the feature descriptors.
 * <p>
 * In addition it provides an extension mechanism so that arbitrary
 * attribute/value pairs can be associated with a design feature.</p>
 * <p>
 * Finally, this class may be subclassed to provide additional information.</p>
 * <p>
 * The most common way to use this class is to get or set descriptor data
 * using one of the {@link FeatureBuilder} objects. This will then be copied
 * into the resulting {@link FeatureDescriptor} objects.
 * </p>
 */
public class DescriptorData implements Serializable, Cloneable {
    private static final long serialVersionUID = -6556452834342672815L;

    private boolean expert;
    private boolean hidden;
    private boolean preferred;
    private String shortDescription;
    private String displayName;
    private Hashtable<String, Object> table;

    /**
     * Constructs an empty <code>DescriptorData</code> object.
     */
    public DescriptorData() {
    }

    /**
     * Package-private constructor,
     * Merge information from two FeatureDescriptors.
     * The merged hidden and expert flags are formed by or-ing the values.
     * In the event of other conflicts, the second argument (y) is
     * given priority over the first argument (x).
     *
     * @param x  The first (lower priority) MethodDescriptor
     * @param y  The second (higher priority) MethodDescriptor
     */
    DescriptorData(DescriptorData x, DescriptorData y) {
        expert = x.expert | y.expert;
        hidden = x.hidden | y.hidden;
        preferred = x.preferred | y.preferred;
        shortDescription = x.shortDescription;
        if (y.shortDescription != null) {
            shortDescription = y.shortDescription;
        }
        displayName = x.displayName;
        if (y.displayName != null) {
            displayName = y.displayName;
        }
        addTable(x.table);
        addTable(y.table);
    }

    /*
     * Package-private dup constructor
     * This must isolate the new object from any changes to the old object.
     */
    DescriptorData(DescriptorData old) {
        expert = old.expert;
        hidden = old.hidden;
        preferred = old.preferred;
        shortDescription = old.shortDescription;
        displayName = old.displayName;

        addTable(old.table);
    }

    /**
     * Clone this descriptor data. This performs a clone of the table
     * data, and pointer copy of the rest.
     *
     * @return the cloned descriptor data
     */
    @Override
    public Object clone() {
        try {
            DescriptorData newData = (DescriptorData) super.clone();
            newData.addTable(this.table);
            return newData;
        } catch(CloneNotSupportedException ex) {
            throw new RuntimeException("Not cloneable");
        }
    }

    /**
     * Gets the localized display name of this feature. By convention, this
     * will be the same as the programmatic name of the corresponding feature.
     *
     * @return The localized display name for the property/method/event.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the localized display name of this feature.
     *
     * @param displayName  The localized display name for the
     *          property/method/event.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * The "expert" flag is used to distinguish between those features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @return <code>True</code> if this feature is intended for use by
     * experts only.
     */
    public boolean isExpert() {
        return expert;
    }

    /**
     * The "expert" flag is used to distinguish between features that are
     * intended for expert users from those that are intended for normal users.
     *
     * @param expert <code>True</code> if this feature is intended for use
     * by experts only.
     */
    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @return <code>True</code> if this feature should be hidden from
     * human users.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * The "hidden" flag is used to identify features that are intended only
     * for tool use, and which should not be exposed to humans.
     *
     * @param hidden <code>True</code> if this feature should be hidden from
     * human users.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @return <code>True</code> if this feature should be preferentially
     * shown to human users.
     */
    public boolean isPreferred() {
        return preferred;
    }

    /**
     * The "preferred" flag is used to identify features that are particularly
     * important for presenting to humans.
     *
     * @param preferred  <code>True</code> if this feature should be
     * preferentially shown to human users.
     */
    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    /**
     * Gets the short description of this feature.
     *
     * @return  A localized short description associated with this
     *   property/method/event.  This defaults to be the display name.
     */
    public String getShortDescription() {
        if (shortDescription == null) {
            return getDisplayName();
        }
        return shortDescription;
    }

    /**
     * You can associate a short descriptive string with a feature.  Normally
     * these descriptive strings should be less than about 40 characters.
     * @param text  A (localized) short description to be associated with
     * this property/method/event.
     */
    public void setShortDescription(String text) {
        shortDescription = text;
    }

    /**
     * Associate a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @param value  The value.
     */
    public void setValue(String attributeName, Object value) {
        getTable().put(attributeName, value);
    }

    /**
     * Retrieve a named attribute with this feature.
     *
     * @param attributeName  The locale-independent name of the attribute
     * @return  The value of the attribute.  May be <code>null</code> if
     *     the attribute is unknown.
     */
    public Object getValue(String attributeName) {
        return (this.table != null)
                ? this.table.get(attributeName)
                : null;
    }

    /**
     * Determine whether this descriptor data has any attributes.
     * Use this to short-circuit any internal table creation.
     *
     * @return <code>true</code> if there are any named attributes
     * in this descriptor data, otherwise <code>false</code>
     */
    public boolean hasAttributes() {
        return (this.table != null) && (!this.table.isEmpty());
    }

    /**
     * Gets an enumeration of the locale-independent names of this
     * feature. This is for compatibility with the JavaBean
     * {@link FeatureDescriptor} classes.
     *
     * @return  An enumeration of the locale-independent names of any
     *    attributes that have been registered with setValue.
     */
    public Enumeration<String> attributeNames() {
        if(this.table == null) {
            return Collections.<String>emptyEnumeration();
        }
        return getTable().keys();
    }

    /**
     * Gets an Iterable&lt;String&gt; of the locale-independent names of this
     * feature. This allows Java foreach loops to operate over attribute
     * names.
     *
     * @return  An Iterable&lt;String&gt; of the locale-independent names
     * of any attributes that have been registered with setValue.
     */
    public Iterable<String> getAttributeNames() {
        if(this.table == null) {
            return Collections.<String>emptyList();
        } else {
            return Collections.unmodifiableMap(this.table).keySet();
        }
    }

    /**
     * Copies all values from the specified attribute table.
     * If any attribute already exists its value will be overridden.
     *
     * @param table  the attribute table with new values
     */
    public void addTable(Map<String, Object> table) {
        if ((table != null) && !table.isEmpty()) {
            getTable().putAll(table);
        }
    }

    /**
     * Returns the initialized attribute table.
     *
     * @return the initialized attribute table
     */
    private Hashtable<String, Object> getTable() {
        if (this.table == null) {
            this.table = new Hashtable<String, Object>();
        }
        return this.table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DescriptorData that = (DescriptorData) o;

        if (expert != that.expert) {
            return false;
        }
        if (hidden != that.hidden) {
            return false;
        }
        if (preferred != that.preferred) {
            return false;
        }
        if ((displayName != null) ? !displayName.equals(that.displayName) : (that.displayName != null)) {
            return false;
        }
        if ((shortDescription != null) ? !shortDescription.equals(that.shortDescription) : (that.shortDescription != null)) {
            return false;
        }
        return (table != null ? table.equals(that.table) : that.table == null);
    }

    @Override
    public int hashCode() {
        int result = (table != null ? table.hashCode() : 0);
        result = 31 * result + (shortDescription != null ? shortDescription.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (preferred ? 1 : 0);
        result = 31 * result + (expert ? 1 : 0);
        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append('[');
        appendTo(sb, "displayName", this.displayName);
        appendTo(sb, "shortDescription", this.shortDescription);
        appendTo(sb, "preferred", this.preferred);
        appendTo(sb, "hidden", this.hidden);
        appendTo(sb, "expert", this.expert);
        if ((this.table != null) && !this.table.isEmpty()) {
            sb.append("values={");
            for (Entry<String, Object> entry : this.table.entrySet()) {
                sb.append(entry.getKey()).append('=').append(entry.getValue()).append("; ");
            }
            sb.setLength(sb.length() - 2);
            sb.append('}');
        }
        appendTo(sb);
        return sb.append(']').toString();
    }

    /**
     * Hook for subclasses wanting to append additional information to
     * toString().
     *
     * @param sb the string builder to which to append
     */
    protected void appendTo(StringBuilder sb) {
    }

    static void appendTo(StringBuilder sb, String name, Object value) {
        if (value != null) {
            sb.append(name).append('=').append(value).append("; ");
        }
    }

    static void appendTo(StringBuilder sb, String name, boolean value) {
        if (value) {
            sb.append(name).append("; ");
        }
    }
}
