/*
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

package org.compactbeans.beans.test.x4520754;

/*
 * @test
 * @bug 4168475 4520754
 * @summary Tests for the removal of some of the exception based control flow
 * @author Mark Davidson
 */

import org.compactbeans.beans.BeanInfo;
import org.compactbeans.beans.IntrospectionException;
import org.compactbeans.beans.Introspector;

public class Test4520754 {
    /**
     * This is here to force the BeanInfo classes to be compiled
     */
    private static final Class[] COMPILE = {
            FooBarBeanInfo.class,
            WombatBeanInfo.class,
    };

    public static void main(String[] args) {
        // user defined classes
        test(Boolean.TRUE, Wombat.class, Foo.class, FooBar.class);
    }

    private static void test(Boolean mark, Class... types) {
        for (Class type : types) {
            BeanInfo info = getBeanInfo(mark, type);
            if (info == null) {
                throw new Error("could not find BeanInfo for " + type);
            }
            if (mark != info.getBeanDescriptor().getValue("test")) {
                throw new Error("could not find marked BeanInfo for " + type);
            }
        }
        Introspector.flushCaches();
    }

    private static BeanInfo getBeanInfo(Boolean mark, Class type) {
        System.out.println("test=" + mark + " for " + type);
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(type);
        } catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        }
        if (info == null) {
            throw new Error("could not find BeanInfo for " + type);
        }
        if (mark != info.getBeanDescriptor().getValue("test")) {
            throw new Error("could not find marked BeanInfo for " + type);
        }
        return info;
    }
}
