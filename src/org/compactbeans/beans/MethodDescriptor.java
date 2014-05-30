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
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A MethodDescriptor describes a particular method that a Java Bean
 * supports for external access from other components.
 */

public final class MethodDescriptor implements FeatureDescriptor {

    private final String name;
    private Reference<Method> methodRef;
    private String[] paramNames;
    private List<Reference<Class>> params;
    private Reference<Class> classRef;

    /**
     * Constructs a <code>MethodDescriptor</code> from a
     * <code>Method</code>.
     *
     * @param method The low-level method information.
     */
    public MethodDescriptor(Method method) {
        this.name = method.getName();
        setMethod(method);
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
    }

    /**
     * Gets the method that this MethodDescriptor encapsulates.
     *
     * @return The low-level description of the method
     */
    public synchronized Method getMethod() {
        Method method = getMethod0();
        if (method == null) {
            Class cls = getClass0();
            String name = getName();
            if ((cls != null) && (name != null)) {
                Class[] params = getParams();
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
            classRef = RefUtil.createWeakReference((Class)method.getDeclaringClass());
        }
        setParams(IntrospectorSupport.getParameterTypes(getClass0(), method));
        this.methodRef = RefUtil.createSoftReference(method);
    }

    private synchronized void setParams(Class[] param) {
        if (param == null) {
            return;
        }
        paramNames = new String[param.length];
        params = new ArrayList<Reference<Class>>(param.length);
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

    private synchronized Class[] getParams() {
        Class[] clss = new Class[params.size()];

        for (int i = 0; i < params.size(); i++) {
            Reference<Class> ref = params.get(i);
            Class cls = ref.get();
            if (cls == null) {
                return null;
            } else {
                clss[i] = cls;
            }
        }
        return clss;
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the property/method/event
     */
    public String getName() {
        return name;
    }

    private Class getClass0() {
        return RefUtil.getObject(classRef);
    }
}
