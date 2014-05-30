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
 * Bean introspection provide a BeanInfo class that implements this
 * BeanInfo interface and provides information about the methods,
 * properties, events, etc, of their bean.
 * <p>
 * To learn about all the behaviour of a bean see the Introspector class.
 * </p>
 */

public interface BeanInfo {

    /**
     * Gets the beans <code>BeanDescriptor</code>.
     *
     * @return  A BeanDescriptor providing overall information about
     * the bean.
     */
    BeanDescriptor getBeanDescriptor();

    /**
     * Gets the beans <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.
     */
    EventSetDescriptor[] getEventSetDescriptors();

    /**
     * Gets the beans <code>PropertyDescriptor</code>s.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use <code>getDescriptorType()</code>
     * to check if a given PropertyDescriptor is an IndexedPropertyDescriptor.</p>
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.
     */
    PropertyDescriptor[] getPropertyDescriptors();

    /**
     * Gets the beans <code>MethodDescriptor</code>s.
     *
     * @return An array of MethodDescriptors describing the externally
     * visible methods supported by this bean.
     */
    MethodDescriptor[] getMethodDescriptors();

}
