/*
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
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

/**
 * This is utility class that provides functionality
 * to find a {@link BeanInfo} for a JavaBean specified by its type.
 *
 * @since 1.7
 *
 * @author Sergey A. Malenkov
 * @jdksource com.sun.beans.finder.BeanInfoFinder
 */
final class BeanInfoFinder {

    private static final String DEFAULT = "sun.beans.infos";
    private static final String DEFAULT_NEW = "com.sun.beans.infos";

    protected static final String[] EMPTY = { };

    private volatile String[] packages;

    public BeanInfoFinder() {
        this.packages = new String[] { DEFAULT };
    }

    private BeanInfo instantiate(Class<?> type, String prefix, String name) {
        if (DEFAULT.equals(prefix)) {
            prefix = DEFAULT_NEW;
        }

        // this optimization will only use the BeanInfo search path
        // if is has changed from the original
        // or trying to get the ComponentBeanInfo
        BeanInfo info;

        if (!DEFAULT_NEW.equals(prefix) || "ComponentBeanInfo".equals(name)) {
            info =  instantiate(type, prefix + '.' + name);
        } else {
            info = null;
        }

        if (info != null) {
            // make sure that the returned BeanInfo matches the class
            BeanDescriptor bd = info.getBeanDescriptor();
            if (bd != null) {
                if (type.equals(bd.getBeanClass())) {
                    return info;
                }
            }
            else {
                PropertyDescriptor[] pds = info.getPropertyDescriptors();
                if (pds != null) {
                    for (PropertyDescriptor pd : pds) {
                        Method method = pd.getReadMethod();
                        if (method == null) {
                            method = pd.getWriteMethod();
                        }
                        if (isValid(type, method)) {
                            return info;
                        }
                    }
                }
                else {
                    MethodDescriptor[] mds = info.getMethodDescriptors();
                    if (mds != null) {
                        for (MethodDescriptor md : mds) {
                            if (isValid(type, md.getMethod())) {
                                return info;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isValid(Class<?> type, Method method) {
        return (method != null) && method.getDeclaringClass().isAssignableFrom(type);
    }

    public String[] getPackages() {
        return this.packages.clone();
    }

    public void setPackages(String... packages) {
        this.packages = (packages != null) && (packages.length > 0)
                ? packages.clone()
                : EMPTY;
    }

    public BeanInfo find(Class<?> type) {
        if (type == null) {
            return null;
        }
        String name = type.getName() + "BeanInfo";
        BeanInfo object = instantiate(type, name);
        if (object != null) {
            return object;
        }
        object = instantiate(type);
        if (object != null) {
            return object;
        }
        int index = name.lastIndexOf('.') + 1;
        if (index > 0) {
            name = name.substring(index);
        }
        for (String prefix : this.packages) {
            object = instantiate(type, prefix, name);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    private BeanInfo instantiate(Class<?> type, String name) {
        try {
            Class<?> infoType = ClassFinder.findClass(name, type.getClassLoader());
            if (BeanInfo.class.isAssignableFrom(infoType)) {
                return (BeanInfo) infoType.newInstance();
            }
        }
        catch (Exception exception) {
            // ignore any exceptions
        }
        return null;
    }

    private BeanInfo instantiate(Class<?> type) {
        try {
            if (BeanInfo.class.isAssignableFrom(type)) {
                return (BeanInfo) type.newInstance();
            }
        }
        catch (Exception exception) {
            // ignore any exceptions
        }
        return null;
    }
}
