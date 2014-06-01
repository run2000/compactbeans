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

/**
 * A BeanDescriptor provides global information about a "bean",
 * including its Java class, its programmatic name, etc.
 * <p>
 * This is one of the kinds of descriptor returned by a <code>BeanInfo</code>
 * object, which also returns descriptors for properties, method, and events.</p>
 */

public final class BeanDescriptor implements FeatureDescriptor {

    private final Reference<Class> beanClassRef;
    private final String name;

    /**
     * Create a BeanDescriptor for a bean that doesn't have a customizer.
     *
     * @param beanClass the <code>Class</code> object of the Java class
     *          that implements the bean.  For example sun.beans.OurButton.class
     */
    public BeanDescriptor(Class<?> beanClass) {
        this.beanClassRef = RefUtil.createWeakReference((Class)beanClass);

        String name = beanClass.getName();
        if(name.lastIndexOf('.') >= 0) {
            name = name.substring(name.lastIndexOf('.') + 1);
        }
        this.name = name;
    }

    /**
     * Gets the bean's <code>Class</code> object.
     *
     * @return The <code>Class</code> object for the bean
     */
    public Class getBeanClass() {
        return RefUtil.getObject(this.beanClassRef);
    }

    /**
     * Gets the programmatic name of this feature.
     *
     * @return The programmatic name of the bean
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the descriptor type for this object.
     *
     * @return <code>DescriptorType.BEAN</code> to indicate this is a
     * BeanDescriptor object
     */
    public DescriptorType getDescriptorType() {
        return DescriptorType.BEAN;
    }
}
