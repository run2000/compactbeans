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

/**
 * Package private implementation support class for Introspector's
 * internal use.
 * <p>
 * Mostly this is used as a placeholder for the descriptors.</p>
 */
final class GenericBeanInfo implements BeanInfo {

    private final BeanDescriptor beanDescriptor;
    private final EventSetDescriptor[] events;
    private final PropertyDescriptor[] properties;
    private final MethodDescriptor[] methods;

    public GenericBeanInfo(BeanDescriptor beanDescriptor,
                           EventSetDescriptor[] events,
                           PropertyDescriptor[] properties,
                           MethodDescriptor[] methods) {
        this.beanDescriptor = beanDescriptor;
        this.events = events;
        this.properties = properties;
        this.methods = methods;
    }

    public BeanDescriptor getBeanDescriptor() {
        return beanDescriptor;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        return events;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    public MethodDescriptor[] getMethodDescriptors() {
        return methods;
    }
}
