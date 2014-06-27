/*
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
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

/**
 * This is a separated-out implementation of the java.beans.Introspector and
 * associated Descriptor classes, targeted at JavaSE 1.8 Compact Profile 1.
 * <p>
 * The code is based on the openjdk8u20-dev source bundle, downloaded from the
 * Mercurial repository at 
 * <a href="http://hg.openjdk.java.net/jdk8u/jdk8u20-dev">http://hg.openjdk.java.net/jdk8u/jdk8u20-dev</a>.
 * </p>
 * <p>
 * The license is the same as for OpenJDK8 itself. That is, GPL version 2 only,
 * with the "Classpath" exception as described in the LICENSE file.</p>
 */
package org.compactbeans.beans;
