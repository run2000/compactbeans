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
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Wrapping and unwrapping of <code>Reference</code> objects.
 * Factored out from <code>Descriptor</code> classes.
 */
final class RefUtil {

    private RefUtil() {
    }

    /**
     * Create a Reference wrapper for the object.
     *
     * @param obj  object that will be wrapped
     * @return a Reference or null if obj is null.
     */
    static <T> Reference<T> createSoftReference(T obj) {
        if (obj != null) {
            return new SoftReference<T>(obj);
        } else {
            return null;
        }
    }

    /**
     * Create a WeakReference wrapper for the object.
     *
     * @param obj object that will be wrapped
     * @return a WeakReference or null if obj is null.
     */
    static <T> Reference<T> createWeakReference(T obj) {
        if (obj != null) {
            return new WeakReference<T>(obj);
        } else {
            return null;
        }
    }

    /**
     * Returns an object from a Reference wrapper.
     *
     * @return the Object in a wrapper or null.
     */
    static <T> T getObject(Reference<T> ref) {
        return (ref == null) ? null : ref.get();
    }
}
