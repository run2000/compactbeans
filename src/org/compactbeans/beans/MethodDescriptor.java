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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * A MethodDescriptor describes a particular method that a Java Bean
 * supports for external access from other components.
 */

public final class MethodDescriptor implements FeatureDescriptor {

    private String name;
    private Reference<Method> methodRef;
    private String[] paramNames;
    private List<Reference<Class<?>>> params;
    private Reference<Class<?>> classRef;
    private ParameterDescriptor parameterDescriptors[];

    private final DescriptorData descriptorData;

    /**
     * Constructs a <code>MethodDescriptor</code> from a
     * <code>Method</code>.
     *
     * @param method The low-level method information.
     */
    public MethodDescriptor(Method method) {
        this.name = method.getName();
        setMethod(method);
        this.descriptorData = null;
    }

    /**
     * Constructs a <code>MethodDescriptor</code> from a
     * <code>Method</code> providing descriptive information for each
     * of the method's parameters.
     *
     * @param method    The low-level method information.
     * @param parameterDescriptors  Descriptive information for each of the
     *                          method's parameters.
     */
    public MethodDescriptor(Method method,
                ParameterDescriptor parameterDescriptors[]) {
        this.name = method.getName();
        setMethod(method);
        this.parameterDescriptors = parameterDescriptors;
        this.descriptorData = null;
    }

    /*
     * Package-private constructor
     * Merge two method descriptors.  Where they conflict, give the
     * second argument (y) priority over the first argument (x).
     * @param x  The first (lower priority) MethodDescriptor
     * @param y  The second (higher priority) MethodDescriptor
     */

    MethodDescriptor(MethodDescriptor x, MethodDescriptor y) {
        name = y.name;
        classRef = x.classRef;
        if (y.classRef != null) {
            classRef = y.classRef;
        }

        methodRef = x.methodRef;
        if (y.methodRef != null) {
            methodRef = y.methodRef;
        }
        params = x.params;
        if (y.params != null) {
            params = y.params;
        }
        paramNames = x.paramNames;
        if (y.paramNames != null) {
            paramNames = y.paramNames;
        }

        parameterDescriptors = x.parameterDescriptors;
        if (y.parameterDescriptors != null) {
            parameterDescriptors = y.parameterDescriptors;
        }

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

    /*
     * Package-private dup constructor
     * This must isolate the new object from any changes to the old object.
     */
    MethodDescriptor(MethodDescriptor old) {
        name = old.name;
        classRef = old.classRef;

        methodRef = old.methodRef;
        params = old.params;
        paramNames = old.paramNames;
        descriptorData = old.getDescriptorData();

        if (old.parameterDescriptors != null) {
            int len = old.parameterDescriptors.length;
            parameterDescriptors = new ParameterDescriptor[len];
            for (int i = 0; i < len ; i++) {
                parameterDescriptors[i] = new ParameterDescriptor(old.parameterDescriptors[i]);
            }
        }
    }

    /**
     * Duplicate constructor, with new <code>DescriptorData</code>.
     * This isolates the new object from any changes to the old object.
     *
     * @param old the method descriptor to be copied
     * @param newData the new DescriptorData to be composed in
     */
    MethodDescriptor(MethodDescriptor old, DescriptorData newData) {
        name = old.name;
        classRef = old.classRef;

        methodRef = old.methodRef;
        params = old.params;
        paramNames = old.paramNames;

        if (old.parameterDescriptors != null) {
            int len = old.parameterDescriptors.length;
            parameterDescriptors = new ParameterDescriptor[len];
            for (int i = 0; i < len ; i++) {
                parameterDescriptors[i] = new ParameterDescriptor(old.parameterDescriptors[i]);
            }
        }
        descriptorData = newData;
    }

    /**
     * Gets the method that this MethodDescriptor encapsulates.
     *
     * @return The low-level description of the method
     */
    public synchronized Method getMethod() {
        Method method = getMethod0();
        if (method == null) {
            Class<?> cls = getClass0();
            String name = getName();
            if ((cls != null) && (name != null)) {
                Class<?>[] params = getParams();
                if (params == null) {
                    for (int i = 0; i < 3; i++) {
                        // Find methods for up to 2 params. We are guessing here.
                        // This block should never execute unless the classloader
                        // that loaded the argument classes disappears.
                        method = IntrospectorSupport.findMethod(cls, name, i, null);
                        if (method != null) {
                            break;
                        }
                    }
                } else {
                    method = IntrospectorSupport.findMethod(cls, name, params.length, params);
                }
                setMethod(method);
            }
        }
        return method;
    }

    private synchronized void setMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            classRef = RefUtil.createWeakReference(method.getDeclaringClass());
        }
        setParams(IntrospectorSupport.getParameterTypes(getClass0(), method));
        this.methodRef = RefUtil.createSoftReference(method);
    }

    private synchronized void setParams(Class<?>[] param) {
        if (param == null) {
            return;
        }
        paramNames = new String[param.length];
        params = new ArrayList<Reference<Class<?>>>(param.length);
        for (int i = 0; i < param.length; i++) {
            paramNames[i] = param[i].getName();
            params.add(RefUtil.createWeakReference(param[i]));
        }
    }

    private Method getMethod0() {
        return RefUtil.getObject(methodRef);
    }

    // pp getParamNames used as an optimization to avoid method.getParameterTypes.
    String[] getParamNames() {
        return paramNames;
    }

    private synchronized Class<?>[] getParams() {
        Class<?>[] clss = new Class[params.size()];

        for (int i = 0; i < params.size(); i++) {
            Reference<Class<?>> ref = params.get(i);
            Class<?> cls = RefUtil.getObject(ref);
            if (cls == null) {
                return null;
            } else {
                clss[i] = cls;
            }
        }
        return clss;
    }

    /**
     * Gets the ParameterDescriptor for each of this MethodDescriptor's
     * method's parameters. The returned array is a defensive copy of the
     * MethodDescriptor's underlying storage.
     *
     * @return The locale-independent names of the parameters.  May return
     *          <code>null</code> if the parameter names aren't known.
     */
    public ParameterDescriptor[] getParameterDescriptors() {
        return (parameterDescriptors == null) ? null : parameterDescriptors.clone();
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the method
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the programmatic name of this feature.
     *
     * @param name The programmatic name of the method
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.METHOD</code> to indicate this is a
     * MethodDescriptor object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.METHOD;
    }

    private Class<?> getClass0() {
        return RefUtil.getObject(classRef);
    }

    /**
     * Gets the localized display name of this feature. By convention, this
     * will be the same as the programmatic name of the corresponding feature.
     *
     * @return The localized display name for the method.
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
     * @return  A localized short description associated with this method.
     *   This defaults to be the display name.
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
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[name=").append(this.name);
        if (this.methodRef != null) {
            Object value = this.methodRef.get();
            if (value != null) {
                sb.append("; method=").append(value);
            }
        }
        if (this.methodRef != null) {
            Object value = this.methodRef.get();
            if (value != null) {
                sb.append("; ").append("method").append('=').append(value);
            }
        }
        if (this.parameterDescriptors != null) {
            sb.append("; parameterDescriptors={");
            for (ParameterDescriptor pd : this.parameterDescriptors) {
                sb.append(pd).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append('}');
        }
        return sb.append(']').toString();
    }
}
