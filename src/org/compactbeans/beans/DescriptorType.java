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
    /** This is a BeanDescriptor object */
    BEAN,
    /** This is a MethodDescriptor object */
    METHOD,
    /** This is an EventSetDescriptor object */
    EVENT_SET,
    /** This is a PropertyDescriptor object */
    PROPERTY,
    /** This is an IndexedPropertyDescriptor object */
    INDEXED_PROPERTY
}
