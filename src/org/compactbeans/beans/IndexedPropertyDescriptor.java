/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * An IndexedPropertyDescriptor describes a property that acts like an
 * array and has an indexed read and/or indexed write method to access
 * specific elements of the array.
 * <p>
 * An indexed property may also provide simple non-indexed read and write
 * methods.  If these are present, they read and write arrays of the type
 * returned by the indexed read method.</p>
 */

public final class IndexedPropertyDescriptor extends PropertyDescriptor {

    private Reference<Class> indexedPropertyTypeRef;
    private Reference<Method> indexedReadMethodRef;
    private Reference<Method> indexedWriteMethodRef;

    private String indexedReadMethodName;
    private String indexedWriteMethodName;

    /**
     * This constructor takes the name of a simple property, and Method
     * objects for reading and writing the property.
     *
     * @param propertyName The programmatic name of the property.
     * @param readMethod The method used for reading the property values as an array.
     *          May be null if the property is write-only or must be indexed.
     * @param writeMethod The method used for writing the property values as an array.
     *          May be null if the property is read-only or must be indexed.
     * @param indexedReadMethod The method used for reading an indexed property value.
     *          May be null if the property is write-only.
     * @param indexedWriteMethod The method used for writing an indexed property value.
     *          May be null if the property is read-only.
     * @throws IntrospectionException if an exception occurs during
     *              introspection.
     */
    public IndexedPropertyDescriptor(String propertyName, Method readMethod, Method writeMethod,
                                     Method indexedReadMethod, Method indexedWriteMethod)
            throws IntrospectionException {
        super(propertyName, readMethod, writeMethod);

        setIndexedReadMethod0(indexedReadMethod);
        setIndexedWriteMethod0(indexedWriteMethod);

        // Type checking
        setIndexedPropertyType(findIndexedPropertyType(indexedReadMethod, indexedWriteMethod));
    }

    /**
     * Gets the method that should be used to read an indexed
     * property value.
     *
     * @return The method that should be used to read an indexed
     * property value.
     * May return null if the property isn't indexed or is write-only.
     */
    public synchronized Method getIndexedReadMethod() {
        Method indexedReadMethod = getIndexedReadMethod0();
        if (indexedReadMethod == null) {
            Class cls = getClass0();
            if ((cls == null) ||
                    ((indexedReadMethodName == null) && (indexedReadMethodRef == null))) {
                // the Indexed readMethod was explicitly set to null.
                return null;
            }
            String nextMethodName = IntrospectorSupport.GET_PREFIX + getBaseName();
            if (indexedReadMethodName == null) {
                Class type = getIndexedPropertyType0();
                if ((type == boolean.class) || (type == null)) {
                    indexedReadMethodName = IntrospectorSupport.IS_PREFIX + getBaseName();
                } else {
                    indexedReadMethodName = nextMethodName;
                }
            }

            Class[] args = {int.class};
            indexedReadMethod = IntrospectorSupport.findMethod(cls, indexedReadMethodName, 1, args);
            if ((indexedReadMethod == null) && !indexedReadMethodName.equals(nextMethodName)) {
                // no "is" method, so look for a "get" method.
                indexedReadMethodName = nextMethodName;
                indexedReadMethod = IntrospectorSupport.findMethod(cls, indexedReadMethodName, 1, args);
            }
            setIndexedReadMethod0(indexedReadMethod);
        }
        return indexedReadMethod;
    }

    /**
     * Sets the method that should be used to read an indexed property value.
     *
     * @param readMethod The new indexed read method.
     */
    private synchronized void setIndexedReadMethod(Method readMethod)
            throws IntrospectionException {

        // the indexed property type is set by the reader.
        setIndexedPropertyType(findIndexedPropertyType(readMethod,
                getIndexedWriteMethod0()));
        setIndexedReadMethod0(readMethod);
    }

    private void setIndexedReadMethod0(Method readMethod) {
        if (readMethod == null) {
            indexedReadMethodName = null;
            indexedReadMethodRef = null;
        } else {
            setClass0(readMethod.getDeclaringClass());

            indexedReadMethodName = readMethod.getName();
            indexedReadMethodRef = RefUtil.createSoftReference(readMethod);
        }
    }


    /**
     * Gets the method that should be used to write an indexed property value.
     *
     * @return The method that should be used to write an indexed
     * property value.
     * May return null if the property isn't indexed or is read-only.
     */
    public synchronized Method getIndexedWriteMethod() {
        Method indexedWriteMethod = getIndexedWriteMethod0();
        if (indexedWriteMethod == null) {
            Class cls = getClass0();
            if ((cls == null) ||
                    ((indexedWriteMethodName == null) && (indexedWriteMethodRef == null))) {
                // the Indexed writeMethod was explicitly set to null.
                return null;
            }

            // We need the indexed type to ensure that we get the correct method.
            // Cannot use the getIndexedPropertyType method since that could
            // result in an infinite loop.
            Class type = getIndexedPropertyType0();
            if (type == null) {
                try {
                    type = findIndexedPropertyType(getIndexedReadMethod(), null);
                    setIndexedPropertyType(type);
                } catch (IntrospectionException ex) {
                    // Set iprop type to be the classic type
                    Class propType = getPropertyType();
                    if (propType.isArray()) {
                        type = propType.getComponentType();
                    }
                }
            }

            if (indexedWriteMethodName == null) {
                indexedWriteMethodName = IntrospectorSupport.SET_PREFIX + getBaseName();
            }

            Class[] args = (type == null) ? null : new Class[] { int.class, type };
            indexedWriteMethod = IntrospectorSupport.findMethod(cls, indexedWriteMethodName, 2, args);
            if (indexedWriteMethod != null) {
                if (!indexedWriteMethod.getReturnType().equals(void.class)) {
                    indexedWriteMethod = null;
                }
            }
            setIndexedWriteMethod0(indexedWriteMethod);
        }
        return indexedWriteMethod;
    }

    /**
     * Sets the method that should be used to write an indexed property value.
     *
     * @param writeMethod The new indexed write method.
     */
    private synchronized void setIndexedWriteMethod(Method writeMethod)
            throws IntrospectionException {

        // If the indexed property type has not been set, then set it.
        Class type = findIndexedPropertyType(getIndexedReadMethod(),
                writeMethod);
        setIndexedPropertyType(type);
        setIndexedWriteMethod0(writeMethod);
    }

    private void setIndexedWriteMethod0(Method writeMethod) {
        if (writeMethod == null) {
            indexedWriteMethodName = null;
            indexedWriteMethodRef = null;
        } else {
            setClass0(writeMethod.getDeclaringClass());
            indexedWriteMethodName = writeMethod.getName();
            indexedWriteMethodRef = RefUtil.createSoftReference(writeMethod);
        }
    }

    /**
     * Gets the <code>Class</code> object of the indexed properties' type.
     * The returned <code>Class</code> may describe a primitive type such as <code>int</code>.
     *
     * @return The <code>Class</code> for the indexed properties' type; may return <code>null</code>
     * if the type cannot be determined.
     */
    public synchronized Class<?> getIndexedPropertyType() {
        Class type = getIndexedPropertyType0();
        if (type == null) {
            try {
                type = findIndexedPropertyType(getIndexedReadMethod(),
                        getIndexedWriteMethod());
                setIndexedPropertyType(type);
            } catch (IntrospectionException ex) {
                // fall
            }
        }
        return type;
    }

    // Private methods which set get/set the Reference objects

    private void setIndexedPropertyType(Class type) {
        indexedPropertyTypeRef = RefUtil.createWeakReference(type);
    }

    private Class getIndexedPropertyType0() {
        return RefUtil.getObject(indexedPropertyTypeRef);
    }

    private Method getIndexedReadMethod0() {
        return RefUtil.getObject(indexedReadMethodRef);
    }

    private Method getIndexedWriteMethod0() {
        return RefUtil.getObject(indexedWriteMethodRef);
    }

    private Class findIndexedPropertyType(Method indexedReadMethod,
                                          Method indexedWriteMethod)
            throws IntrospectionException {
        Class indexedPropertyType = null;

        if (indexedReadMethod != null) {
            Class params[] = IntrospectorSupport.getParameterTypes(getClass0(), indexedReadMethod);
            if (params.length != 1) {
                throw new IntrospectionException("bad indexed read method arg count");
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("non int index to indexed read method");
            }
            indexedPropertyType = indexedReadMethod.getReturnType();
            if (indexedPropertyType == Void.TYPE) {
                throw new IntrospectionException("indexed read method returns void");
            }
        }
        if (indexedWriteMethod != null) {
            Class params[] = indexedWriteMethod.getParameterTypes();
            if (params.length != 2) {
                throw new IntrospectionException("bad indexed write method arg count");
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("non int index to indexed write method");
            }
            if (indexedPropertyType != null && indexedPropertyType != params[1]) {
                throw new IntrospectionException(
                        "type mismatch between indexed read and indexed write methods: "
                                + getName());
            }
            indexedPropertyType = params[1];
        }
        Class propertyType = getPropertyType();
        if ((propertyType != null) && (!propertyType.isArray() ||
                (propertyType.getComponentType() != indexedPropertyType))) {
            throw new IntrospectionException("type mismatch between indexed and non-indexed methods: "
                    + getName());
        }
        return indexedPropertyType;
    }

    /**
     * Compares this <code>PropertyDescriptor</code> against the specified object.
     * Returns true if the objects are the same. Two <code>PropertyDescriptor</code>s
     * are the same if the read, write, property types, property editor and
     * flags  are equivalent.
     *
     * @since 1.4
     */
    public boolean equals(Object obj) {
        // Note: This would be identical to PropertyDescriptor but they don't
        // share the same fields.
        if (this == obj) {
            return true;
        }

        if ((obj != null) && (obj instanceof IndexedPropertyDescriptor)) {
            IndexedPropertyDescriptor other = (IndexedPropertyDescriptor) obj;
            Method otherIndexedReadMethod = other.getIndexedReadMethod();
            Method otherIndexedWriteMethod = other.getIndexedWriteMethod();

            if (!compareMethods(getIndexedReadMethod(), otherIndexedReadMethod)) {
                return false;
            }

            if (!compareMethods(getIndexedWriteMethod(), otherIndexedWriteMethod)) {
                return false;
            }

            return (getIndexedPropertyType() == other.getIndexedPropertyType()) && super.equals(obj);
        }
        return false;
    }

    /**
     * Package-private constructor.
     * Merge two property descriptors.  Where they conflict, give the
     * second argument (y) priority over the first argumnnt (x).
     *
     * @param x The first (lower priority) PropertyDescriptor
     * @param y The second (higher priority) PropertyDescriptor
     */

    IndexedPropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
        super(x, y);
        if (x instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor ix = (IndexedPropertyDescriptor) x;
            try {
                Method xr = ix.getIndexedReadMethod();
                if (xr != null) {
                    setIndexedReadMethod(xr);
                }

                Method xw = ix.getIndexedWriteMethod();
                if (xw != null) {
                    setIndexedWriteMethod(xw);
                }
            } catch (IntrospectionException ex) {
                // Should not happen
                throw new AssertionError(ex);
            }
        }
        if (y instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor iy = (IndexedPropertyDescriptor) y;
            try {
                Method yr = iy.getIndexedReadMethod();
                if ((yr != null) && (yr.getDeclaringClass() == getClass0())) {
                    setIndexedReadMethod(yr);
                }

                Method yw = iy.getIndexedWriteMethod();
                if ((yw != null) && (yw.getDeclaringClass() == getClass0())) {
                    setIndexedWriteMethod(yw);
                }
            } catch (IntrospectionException ex) {
                // Should not happen
                throw new AssertionError(ex);
            }
        }
    }

    /**
     * Returns a hash code value for the object.
     * See {@link java.lang.Object#hashCode} for a complete description.
     *
     * @return a hash code value for this object.
     * @since 1.5
     */
    public int hashCode() {
        int result = super.hashCode();

        result = 37 * result + ((indexedWriteMethodName == null) ? 0 :
                indexedWriteMethodName.hashCode());
        result = 37 * result + ((indexedReadMethodName == null) ? 0 :
                indexedReadMethodName.hashCode());
        result = 37 * result + ((getIndexedPropertyType() == null) ? 0 :
                getIndexedPropertyType().hashCode());

        return result;
    }
}