/*
 * Copyright (c) 1996, 2014. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  As a special exception, the
 * copyright holders of this library designate this particular file as
 * subject to the "Classpath" exception as provided in the LICENSE file
 * that accompanied this code.
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
 */

package org.compactbeans.beans;

/**
 * Simple enum for identifying feature descriptor types without requiring
 * <code>instanceof</code> tests.
 */
public enum DescriptorType {
    /** This is a <code>BeanDescriptor</code> or <code>BeanBuilder</code> object */
    BEAN,
    /** This is a <code>MethodDescriptor</code> or <code>MethodBuilder</code> object */
    METHOD,
    /** This is an <code>EventSetDescriptor</code> or <code>EventSetBuilder</code> object */
    EVENT_SET,
    /** This is a <code>PropertyDescriptor</code> or <code>PropertyBuilder</code> object */
    PROPERTY,
    /** This is an <code>IndexedPropertyDescriptor</code> or <code>IndexedPropertyBuilder</code> object */
    INDEXED_PROPERTY,
    /** This is a <code>ParameterDescriptor</code> or <code>ParameterBuilder</code> object */
    PARAMETER
}
