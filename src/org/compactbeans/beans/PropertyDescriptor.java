/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.ref.Reference;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;

/**
 * A PropertyDescriptor describes one property that a Java Bean
 * exports via a pair of accessor methods.
 */
public class PropertyDescriptor implements FeatureDescriptor {

    private String name;
    private Reference<Class<?>> propertyTypeRef;
    private final MethodRef readMethodRef = new MethodRef();
    private final MethodRef writeMethodRef = new MethodRef();

    private boolean bound;
    private boolean constrained;

    // The base name of the method name which will be prefixed with the
    // read and write method. If name == "foo" then the baseName is "Foo"
    private String baseName;

    private String writeMethodName;
    private String readMethodName;
    private Reference<Class<?>> classRef;

    private final DescriptorData descriptorData;

    /**
     * Constructs a PropertyDescriptor for a property that follows
     * the standard Java convention by having getFoo and setFoo
     * accessor methods.  Thus if the argument name is "fred", it will
     * assume that the writer method is "setFred" and the reader method
     * is "getFred" (or "isFred" for a boolean property).  Note that the
     * property name should start with a lower case character, which will
     * be capitalized in the method names.
     *
     * @param propertyName The programmatic name of the property.
     * @param beanClass The Class object for the target bean.  For
     *          example sun.beans.OurButton.class.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public PropertyDescriptor(String propertyName, Class<?> beanClass)
            throws IntrospectionException {
        this(propertyName, beanClass,
                IntrospectorSupport.IS_PREFIX + NameGenerator.capitalize(propertyName),
                IntrospectorSupport.SET_PREFIX + NameGenerator.capitalize(propertyName));
    }

    /**
     * This constructor takes the name of a simple property, and
     * <code>Method</code> objects for reading and writing the property.
     *
     * @param propertyName The programmatic name of the property.
     * @param readMethod The method used for reading the property value.
     *          May be <code>null</code> if the property is write-only.
     * @param writeMethod The method used for writing the property value.
     *          May be <code>null</code> if the property is read-only.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public PropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
            throws IntrospectionException {
        if ((propertyName == null) || (propertyName.length() == 0)) {
            throw new IntrospectionException("bad property name");
        }
        name = propertyName;
        setReadMethod(readMethod);
        setWriteMethod(writeMethod);
        this.descriptorData = null;
    }

    /**
     * Package-private constructor.
     * Merge two property descriptors.  Where they conflict, give the
     * second argument (y) priority over the first argument (x).
     *
     * @param x The first (lower priority) PropertyDescriptor
     * @param y The second (higher priority) PropertyDescriptor
     */
    PropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
        name = y.name;
        classRef = x.classRef;
        if (y.classRef != null) {
            classRef = y.classRef;
        }

        if (y.baseName != null) {
            baseName = y.baseName;
        } else {
            baseName = x.baseName;
        }

        if (y.readMethodName != null) {
            readMethodName = y.readMethodName;
        } else {
            readMethodName = x.readMethodName;
        }

        if (y.writeMethodName != null) {
            writeMethodName = y.writeMethodName;
        } else {
            writeMethodName = x.writeMethodName;
        }

        if (y.propertyTypeRef != null) {
            propertyTypeRef = y.propertyTypeRef;
        } else {
            propertyTypeRef = x.propertyTypeRef;
        }

        // Figure out the merged read method.
        Method xr = x.getReadMethod();
        Method yr = y.getReadMethod();

        // Normally give priority to y's readMethod.
        try {
            if (isAssignable(xr, yr)) {
                setReadMethod(yr);
            } else {
                setReadMethod(xr);
            }
        } catch (IntrospectionException ex) {
            // fall through
        }

        // However, if both x and y reference read methods in the same class,
        // give priority to a boolean "is" method over a boolean "get" method.
        if ((xr != null) && (yr != null) &&
                (xr.getDeclaringClass() == yr.getDeclaringClass()) &&
                IntrospectorSupport.getReturnType(getClass0(), xr) == boolean.class &&
                IntrospectorSupport.getReturnType(getClass0(), yr) == boolean.class &&
                (xr.getName().indexOf(IntrospectorSupport.IS_PREFIX) == 0) &&
                (yr.getName().indexOf(IntrospectorSupport.GET_PREFIX) == 0)) {
            try {
                setReadMethod(xr);
            } catch (IntrospectionException ex) {
                // fall through
            }
        }

        Method xw = x.getWriteMethod();
        Method yw = y.getWriteMethod();

        try {
            if (yw != null) {
                setWriteMethod(yw);
            } else {
                setWriteMethod(xw);
            }
        } catch (IntrospectionException ex) {
            // Fall through
        }

        bound = x.bound | y.bound;
        constrained = x.constrained | y.constrained;

        if(x.descriptorData == null) {
            if(y.descriptorData == null) {
                descriptorData = null;
            } else {
                descriptorData = (DescriptorData) y.descriptorData.clone();
            }
        } else if(y.descriptorData == null) {
            descriptorData = (DescriptorData) x.descriptorData.clone();
        } else {
            descriptorData = new DescriptorData(x.descriptorData, y.descriptorData);
        }
    }

    /**
     * Creates <code>PropertyDescriptor</code> for the specified bean
     * with the specified name and methods to read/write the property value.
     *
     * @param bean the type of the target bean
     * @param base the base name of the property (the rest of the method name)
     * @param read the method used for reading the property value
     * @param write the method used for writing the property value
     * @throws IntrospectionException if an exception occurs during introspection
     *
     * @since 1.7
     */
    PropertyDescriptor(Class<?> bean, String base, Method read, Method write) throws IntrospectionException {
        if (bean == null) {
            throw new IntrospectionException("Target Bean class is null");
        }
        setClass0(bean);
        name = NameGenerator.decapitalize(base);
        setReadMethod(read);
        setWriteMethod(write);
        this.baseName = base;
        this.descriptorData = null;
    }

    /**
     * This constructor takes the name of a simple property, and method
     * names for reading and writing the property.
     *
     * @param propertyName The programmatic name of the property.
     * @param beanClass The Class object for the target bean.  For
     *          example sun.beans.OurButton.class.
     * @param readMethodName The name of the method used for reading the property
     *           value.  May be <code>null</code> if the property is write-only.
     * @param writeMethodName The name of the method used for writing the property
     *           value.  May be <code>null</code> if the property is read-only.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public PropertyDescriptor(String propertyName, Class<?> beanClass,
                String readMethodName, String writeMethodName)
                throws IntrospectionException {
        if (beanClass == null) {
            throw new IntrospectionException("Target Bean class is null");
        }
        if ((propertyName == null) || (propertyName.length() == 0)) {
            throw new IntrospectionException("bad property name");
        }
        if ("".equals(readMethodName) || "".equals(writeMethodName)) {
            throw new IntrospectionException("read or write method name should not be the empty string");
        }
        name = propertyName;
        setClass0(beanClass);

        this.readMethodName = readMethodName;
        if ((readMethodName != null) && (getReadMethod() == null)) {
            throw new IntrospectionException("Method not found: " + readMethodName);
        }
        this.writeMethodName = writeMethodName;
        if ((writeMethodName != null) && (getWriteMethod() == null)) {
            throw new IntrospectionException("Method not found: " + writeMethodName);
        }
        // If this class or one of its base classes allow PropertyChangeListener,
        // then we assume that any properties we discover are "bound".
        // See Introspector.getTargetPropertyInfo() method.
        Class<?>[] args = { PropertyChangeListener.class };
        this.bound = (null != IntrospectorSupport.findMethod(beanClass, "addPropertyChangeListener", args.length, args));
        this.descriptorData = null;
    }

    /*
     * Package-private copy constructor.
     * This must isolate the new object from any changes to the old object.
     */
    PropertyDescriptor(PropertyDescriptor old) {
        name = old.name;
        baseName = old.baseName;
        classRef = old.classRef;
        propertyTypeRef = old.propertyTypeRef;
        this.readMethodRef.set(old.readMethodRef.get());
        this.writeMethodRef.set(old.writeMethodRef.get());
        readMethodName = old.readMethodName;
        writeMethodName = old.writeMethodName;
        bound = old.bound;
        constrained = old.constrained;
        descriptorData = old.getDescriptorData();
    }

    /**
     * Duplicate constructor, with new <code>DescriptorData</code>.
     * This isolates the new object from any changes to the old object.
     *
     * @param old the property descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    PropertyDescriptor(PropertyDescriptor old, DescriptorData newData) {
        name = old.name;
        baseName = old.baseName;
        classRef = old.classRef;
        propertyTypeRef = old.propertyTypeRef;
        this.readMethodRef.set(old.readMethodRef.get());
        this.writeMethodRef.set(old.writeMethodRef.get());
        readMethodName = old.readMethodName;
        writeMethodName = old.writeMethodName;
        bound = old.bound;
        constrained = old.constrained;
        descriptorData = newData;
    }

    void updateGenericsFor(Class<?> type) {
        setClass0(type);
        try {
            setPropertyType(findPropertyType(this.readMethodRef.get(), this.writeMethodRef.get()));
        }
        catch (IntrospectionException exception) {
            setPropertyType(null);
        }
    }

    /**
     * Returns the Java type info for the property.
     * Note that the {@code Class} object may describe
     * primitive Java types such as {@code int}.
     * This type is returned by the read method
     * or is used as the parameter type of the write method.
     * Returns {@code null} if the type is an indexed property
     * that does not support non-indexed access.
     *
     * @return the {@code Class} object that represents the Java type info,
     *         or {@code null} if the type cannot be determined
     */
    public synchronized Class<?> getPropertyType() {
        Class<?> type = getPropertyType0();
        if (type == null) {
            try {
                type = findPropertyType(getReadMethod(), getWriteMethod());
                setPropertyType(type);
            } catch (IntrospectionException ex) {
                // Fall
            }
        }
        return type;
    }

    private void setPropertyType(Class<?> type) {
        this.propertyTypeRef = RefUtil.createWeakReference(type);
    }

    private Class<?> getPropertyType0() {
        return RefUtil.getObject(propertyTypeRef);
    }

    /**
     * Gets the method that should be used to read the property value.
     *
     * @return The method that should be used to read the property value.
     * May return <code>null</code> if the property can't be read.
     */
    public synchronized Method getReadMethod() {
        Method readMethod = this.readMethodRef.get();
        if (readMethod == null) {
            Class<?> cls = getClass0();
            if ((cls == null) || ((readMethodName == null) && !this.readMethodRef.isSet())) {
                // The read method was explicitly set to null.
                return null;
            }
            String nextMethodName = IntrospectorSupport.GET_PREFIX + getBaseName();
            if (readMethodName == null) {
                Class<?> type = getPropertyType0();
                if ((type == boolean.class) || (type == null)) {
                    readMethodName = IntrospectorSupport.IS_PREFIX + getBaseName();
                } else {
                    readMethodName = nextMethodName;
                }
            }

            // Since there can be multiple write methods but only one getter
            // method, find the getter method first so that you know what the
            // property type is.  For booleans, there can be "is" and "get"
            // methods.  If an "is" method exists, this is the official
            // reader method so look for this one first.
            readMethod = IntrospectorSupport.findMethod(cls, readMethodName, 0);
            if ((readMethod == null) && !readMethodName.equals(nextMethodName)) {
                readMethodName = nextMethodName;
                readMethod = IntrospectorSupport.findMethod(cls, readMethodName, 0);
            }
            try {
                setReadMethod(readMethod);
            } catch (IntrospectionException ex) {
                // fall
            }
        }
        return readMethod;
    }

    /**
     * Sets the method that should be used to read the property value.
     *
     * @param readMethod The new read method.
     * @throws IntrospectionException if the read method is invalid
     */
    synchronized void setReadMethod(Method readMethod)
            throws IntrospectionException {
        this.readMethodRef.set(readMethod);
        if (readMethod == null) {
            readMethodName = null;
            return;
        }
        // The property type is determined by the read method.
        setPropertyType(findPropertyType(readMethod, this.writeMethodRef.get()));
        setClass0(readMethod.getDeclaringClass());

        readMethodName = readMethod.getName();
    }

    /**
     * Gets the method that should be used to write the property value.
     *
     * @return The method that should be used to write the property value.
     * May return <code>null</code> if the property can't be written.
     */
    public synchronized Method getWriteMethod() {
        Method writeMethod = this.writeMethodRef.get();
        if (writeMethod == null) {
            Class<?> cls = getClass0();
            if ((cls == null) || ((writeMethodName == null) && !this.writeMethodRef.isSet())) {
                // The write method was explicitly set to null.
                return null;
            }

            // We need the type to fetch the correct method.
            Class<?> type = getPropertyType0();
            if (type == null) {
                try {
                    // Can't use getPropertyType since it will lead to recursive loop.
                    type = findPropertyType(getReadMethod(), null);
                    setPropertyType(type);
                } catch (IntrospectionException ex) {
                    // Without the correct property type we can't be guaranteed
                    // to find the correct method.
                    return null;
                }
            }

            if (writeMethodName == null) {
                writeMethodName = IntrospectorSupport.SET_PREFIX + getBaseName();
            }

            Class<?>[] args = (type == null) ? null : new Class<?>[] { type };
            writeMethod = IntrospectorSupport.findMethod(cls, writeMethodName, 1, args);
            if (writeMethod != null) {
                if (!writeMethod.getReturnType().equals(void.class)) {
                    writeMethod = null;
                }
            }
            try {
                setWriteMethod(writeMethod);
            } catch (IntrospectionException ex) {
                // fall through
            }
        }
        return writeMethod;
    }

    /**
     * Sets the method that should be used to write the property value.
     *
     * @param writeMethod The new write method.
     * @throws IntrospectionException if the write method is invalid
     */
    synchronized void setWriteMethod(Method writeMethod)
            throws IntrospectionException {
        this.writeMethodRef.set(writeMethod);
        if (writeMethod == null) {
            writeMethodName = null;
            return;
        }
        // Set the property type - which validates the method
        setPropertyType(findPropertyType(getReadMethod(), writeMethod));
        setClass0(writeMethod.getDeclaringClass());

        writeMethodName = writeMethod.getName();
    }

    /**
     * Updates to "bound" properties will cause a "PropertyChange" event to
     * get fired when the property is changed.
     *
     * @return <code>true</code> if this is a bound property.
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * Updates to "bound" properties will cause a "PropertyChange" event to
     * get fired when the property is changed.
     *
     * @param bound <code>true</code> if this is a bound property.
     */
    void setBound(boolean bound) {
        this.bound = bound;
    }

    /**
     * Attempted updates to "Constrained" properties will cause a "VetoableChange"
     * event to get fired when the property is changed.
     *
     * @return <code>True</code> if this is a constrained property.
     */
    public boolean isConstrained() {
        return constrained;
    }

    /**
     * Attempted updates to "Constrained" properties will cause a "VetoableChange"
     * event to get fired when the property is changed.
     *
     * @param constrained <code>True</code> if this is a constrained property.
     */
    void setConstrained(boolean constrained) {
        this.constrained = constrained;
    }

    // Calculate once since capitalize() is expensive.
    String getBaseName() {
        if (baseName == null) {
            baseName = NameGenerator.capitalize(getName());
        }
        return baseName;
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the property
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.PROPERTY</code> to indicate this is a
     * PropertyDescriptor object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.PROPERTY;
    }

    /**
     * Overridden to ensure that a super class doesn't take precedent
     */
    void setClass0(Class<?> clz) {
        Class<?> currClass = getClass0();

        // don't replace a subclass with a superclass
        if ((currClass == null) || !clz.isAssignableFrom(currClass)) {
            classRef = RefUtil.createWeakReference(clz);
        }
    }

    Class<?> getClass0() {
        return RefUtil.getObject(classRef);
    }

    /**
     * Gets any explicit PropertyEditor <code>Class</code> that has been registered
     * for this property.
     *
     * @return this will return <code>null</code>,
     *          indicating that no special editor has been registered
     */
    public Class<?> getPropertyEditorClass() {
        // We don't ever look for property editors in the introspection code
        return null;
    }

    /**
     * Returns the property type that corresponds to the read and write method.
     * The type precedence is given to the readMethod.
     *
     * @return the type of the property descriptor or <code>null</code> if both
     * read and write methods are <code>null</code>.
     * @throws IntrospectionException if the read or write method is invalid
     */
    private Class<?> findPropertyType(Method readMethod, Method writeMethod)
            throws IntrospectionException {
        Class<?> propertyType = null;
        try {
            if (readMethod != null) {
                Class<?>[] params = IntrospectorSupport.getParameterTypes(getClass0(), readMethod);
                if (params.length != 0) {
                    throw new IntrospectionException("bad read method arg count: "
                            + readMethod);
                }
                propertyType = IntrospectorSupport.getReturnType(getClass0(), readMethod);
                if (propertyType == Void.TYPE) {
                    throw new IntrospectionException("read method " +
                            readMethod.getName() + " returns void");
                }
            }
            if (writeMethod != null) {
                Class<?> params[] = IntrospectorSupport.getParameterTypes(getClass0(), writeMethod);
                if (params.length != 1) {
                    throw new IntrospectionException("bad write method arg count: "
                            + writeMethod);
                }
                if ((propertyType != null) && !params[0].isAssignableFrom(propertyType)) {
                    throw new IntrospectionException("type mismatch between read and write methods");
                }
                propertyType = params[0];
            }
        } catch (IntrospectionException ex) {
            throw ex;
        }
        return propertyType;
    }

    /**
     * Gets the localized display name of this feature. By convention, this
     * will be the same as the programmatic name of the corresponding feature.
     *
     * @return The localized display name for the property.
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
     * @return  A localized short description associated with this property.
     * This defaults to be the display name.
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
     * Compares this <code>PropertyDescriptor</code> against the specified object.
     * Returns true if the objects are the same. Two <code>PropertyDescriptor</code>s
     * are the same if the read, write, property types, property editor and
     * flags  are equivalent.
     *
     * @since 1.4
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj != null) && (obj instanceof PropertyDescriptor)) {
            PropertyDescriptor other = (PropertyDescriptor) obj;
            Method otherReadMethod = other.getReadMethod();
            Method otherWriteMethod = other.getWriteMethod();

            if (!IntrospectorSupport.compareMethods(getReadMethod(), otherReadMethod)) {
                return false;
            }

            if (!IntrospectorSupport.compareMethods(getWriteMethod(), otherWriteMethod)) {
                return false;
            }

            return (getPropertyType() == other.getPropertyType()) &&
                    (bound == other.isBound()) &&
                    (constrained == other.isConstrained()) &&
                    (writeMethodName == other.writeMethodName) &&
                    (readMethodName == other.readMethodName);
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     * See {@link java.lang.Object#hashCode} for a complete description.
     *
     * @return a hash code value for this object.
     * @since 1.5
     */
    @Override
    public int hashCode() {
        int result = 7;

        result = 37 * result + ((getPropertyType() == null) ? 0 :
                getPropertyType().hashCode());
        result = 37 * result + ((getReadMethod() == null) ? 0 :
                getReadMethod().hashCode());
        result = 37 * result + ((getWriteMethod() == null) ? 0 :
                getWriteMethod().hashCode());
        result = 37 * result + ((writeMethodName == null) ? 0 :
                writeMethodName.hashCode());
        result = 37 * result + ((readMethodName == null) ? 0 :
                readMethodName.hashCode());
        result = 37 * result + getName().hashCode();
        result = 37 * result + ((bound) ? 1 : 0);
        result = 37 * result + ((constrained) ? 1 : 0);

        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     *
     * @since 1.7
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[name=").append(this.name);
        if (this.bound) {
            sb.append("; bound");
        }
        if (this.constrained) {
            sb.append("; constrained");
        }
        appendTo(sb, "propertyType", RefUtil.getObject(this.propertyTypeRef));
        appendTo(sb, "readMethod", this.readMethodRef.get());
        appendTo(sb, "writeMethod", this.writeMethodRef.get());
        appendTo(sb);
        return sb.append("]").toString();
    }

    void appendTo(StringBuilder sb) {
    }

    static void appendTo(StringBuilder sb, String name, Object value) {
        if (value != null) {
            sb.append("; ").append(name).append('=').append(value);
        }
    }

    boolean isAssignable(Method m1, Method m2) {
        if (m1 == null) {
            return true; // choose second method
        }
        if (m2 == null) {
            return false; // choose first method
        }
        if (!m1.getName().equals(m2.getName())) {
            return true; // choose second method by default
        }
        Class<?> type1 = m1.getDeclaringClass();
        Class<?> type2 = m2.getDeclaringClass();
        if (!type1.isAssignableFrom(type2)) {
            return false; // choose first method: it declared later
        }
        type1 = IntrospectorSupport.getReturnType(getClass0(), m1);
        type2 = IntrospectorSupport.getReturnType(getClass0(), m2);
        if (!type1.isAssignableFrom(type2)) {
            return false; // choose first method: it overrides return type
        }
        Class<?>[] args1 = IntrospectorSupport.getParameterTypes(getClass0(), m1);
        Class<?>[] args2 = IntrospectorSupport.getParameterTypes(getClass0(), m2);
        if (args1.length != args2.length) {
            return true; // choose second method by default
        }
        for (int i = 0; i < args1.length; i++) {
            if (!args1[i].isAssignableFrom(args2[i])) {
                return false; // choose first method: it overrides parameter
            }
        }
        return true; // choose second method
    }
}
