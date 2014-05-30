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

/**
 * A PropertyDescriptor describes one property that a Java Bean
 * exports via a pair of accessor methods.
 */
public class PropertyDescriptor implements FeatureDescriptor {

    private final String name;
    private Reference<Class> propertyTypeRef;
    private Reference<Method> readMethodRef;
    private Reference<Method> writeMethodRef;

    // The base name of the method name which will be prefixed with the
    // read and write method. If name == "foo" then the baseName is "Foo"
    private String baseName;

    private String writeMethodName;
    private String readMethodName;
    private Reference<Class> classRef;

    /**
     * This constructor takes the name of a simple property, and Method
     * objects for reading and writing the property.
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
        readMethodRef = old.readMethodRef;
        writeMethodRef = old.writeMethodRef;
        readMethodName = old.readMethodName;
        writeMethodName = old.writeMethodName;
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
        Class type = getPropertyType0();
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

    private void setPropertyType(Class type) {
        this.propertyTypeRef = RefUtil.createWeakReference(type);
    }

    private Class getPropertyType0() {
        return RefUtil.getObject(propertyTypeRef);
    }

    /**
     * Gets the method that should be used to read the property value.
     *
     * @return The method that should be used to read the property value.
     * May return <code>null</code> if the property can't be read.
     */
    public synchronized Method getReadMethod() {
        Method readMethod = getReadMethod0();
        if (readMethod == null) {
            Class cls = getClass0();
            if ((cls == null) || ((readMethodName == null) && (readMethodRef == null))) {
                // The read method was explicitly set to null.
                return null;
            }
            String nextMethodName = IntrospectorSupport.GET_PREFIX + getBaseName();
            if (readMethodName == null) {
                Class type = getPropertyType0();
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
     */
    synchronized void setReadMethod(Method readMethod)
            throws IntrospectionException {
        if (readMethod == null) {
            readMethodName = null;
            readMethodRef = null;
        } else {
            // The property type is determined by the read method.
            setPropertyType(findPropertyType(readMethod, getWriteMethod0()));
            setClass0(readMethod.getDeclaringClass());

            readMethodName = readMethod.getName();
            readMethodRef = RefUtil.createSoftReference(readMethod);
        }
    }

    /**
     * Gets the method that should be used to write the property value.
     *
     * @return The method that should be used to write the property value.
     * May return <code>null</code> if the property can't be written.
     */
    public synchronized Method getWriteMethod() {
        Method writeMethod = getWriteMethod0();
        if (writeMethod == null) {
            Class cls = getClass0();
            if ((cls == null) || ((writeMethodName == null) && (writeMethodRef == null))) {
                // The write method was explicitly set to null.
                return null;
            }

            // We need the type to fetch the correct method.
            Class type = getPropertyType0();
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

            Class[] args = (type == null) ? null : new Class[] { type };
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
     */
    synchronized void setWriteMethod(Method writeMethod)
            throws IntrospectionException {
        if (writeMethod == null) {
            writeMethodName = null;
            writeMethodRef = null;
        } else {
            // Set the property type - which validates the method
            setPropertyType(findPropertyType(getReadMethod(), writeMethod));
            setClass0(writeMethod.getDeclaringClass());

            writeMethodName = writeMethod.getName();
            writeMethodRef = RefUtil.createSoftReference(writeMethod);
        }
    }

    private Method getReadMethod0() {
        return RefUtil.getObject(readMethodRef);
    }

    private Method getWriteMethod0() {
        return RefUtil.getObject(writeMethodRef);
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
    protected void setClass0(Class clz) {
        Class currClass = getClass0();

        // don't replace a subclass with a superclass
        if ((currClass == null) || !clz.isAssignableFrom(currClass)) {
            classRef = RefUtil.createWeakReference(clz);
        }
    }

    Class getClass0() {
        return RefUtil.getObject(classRef);
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
                    (writeMethodName == other.writeMethodName) &&
                    (readMethodName == other.readMethodName);
        }
        return false;
    }

    /**
     * Returns the property type that corresponds to the read and write method.
     * The type precedence is given to the readMethod.
     *
     * @return the type of the property descriptor or null if both
     * read and write methods are null.
     * @throws IntrospectionException if the read or write method is invalid
     */
    private Class findPropertyType(Method readMethod, Method writeMethod)
            throws IntrospectionException {
        Class propertyType = null;
        try {
            if (readMethod != null) {
                Class[] params = IntrospectorSupport.getParameterTypes(getClass0(), readMethod);
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
                Class params[] = IntrospectorSupport.getParameterTypes(getClass0(), writeMethod);
                if (params.length != 1) {
                    throw new IntrospectionException("bad write method arg count: "
                            + writeMethod);
                }
                if (propertyType != null && !params[0].isAssignableFrom(propertyType)) {
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

        return result;
    }

    private boolean isAssignable(Method m1, Method m2) {
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
