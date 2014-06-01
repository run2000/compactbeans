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
 * A bean implementor who wishes to provide explicit information about
 * their bean may provide a BeanInfo class that implements this BeanInfo
 * interface and provides explicit information about the methods,
 * properties, events, etc, of their  bean.
 * <p/>
 * A bean implementor doesn't need to provide a complete set of
 * explicit information.  You can pick and choose which information
 * you want to provide and the rest will be obtained by automatic
 * analysis using low-level reflection of the bean classes' methods
 * and applying standard design patterns.
 * <p/>
 * You get the opportunity to provide lots and lots of different
 * information as part of the various XyZDescriptor classes.  But
 * don't panic, you only really need to provide the minimal core
 * information required by the various constructors.
 * <p/>
 * See also the SimpleBeanInfo class which provides a convenient
 * "noop" base class for BeanInfo classes, which you can override
 * for those specific places where you want to return explicit info.
 * <p/>
 * To learn about all the behaviour of a bean see the Introspector class.
 */

public interface BeanInfo {

    /**
     * Gets the beans <code>BeanDescriptor</code>.
     *
     * @return  A BeanDescriptor providing overall information about
     * the bean, such as its displayName, its customizer, etc.  May
     * return null if the information should be obtained by automatic
     * analysis.
     */
    BeanDescriptor getBeanDescriptor();

    /**
     * Gets the beans <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    EventSetDescriptor[] getEventSetDescriptors();

    /**
     * Gets the beans <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p/>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    PropertyDescriptor[] getPropertyDescriptors();

    /**
     * Gets the beans <code>MethodDescriptor</code>s.
     *
     * @return An array of MethodDescriptors describing the externally
     * visible methods supported by this bean.  May return null if
     * the information should be obtained by automatic analysis.
     */
    MethodDescriptor[] getMethodDescriptors();

}
