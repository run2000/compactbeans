/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * A utility class which generates unique names for object instances.
 * The name will be a concatenation of the unqualified class name
 * and an instance number.
 * <p>
 * For example, if the first object instance javax.swing.JButton
 * is passed into <code>instanceName</code> then the returned
 * string identifier will be &quot;JButton0&quot;.</p>
 *
 * @author Philip Milne
 */
public final class NameGenerator {

    private NameGenerator() {
    }

    /**
     * Utility method to take a string and convert it to normal Java variable
     * name capitalization.  This normally means converting the first
     * character from upper case to lower case, but in the (unusual) special
     * case when there is more than one character and both the first and
     * second characters are upper case, we leave it alone.
     * <p>
     * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays
     * as "URL".</p>
     *
     * @param name The string to be decapitalized.
     * @return The decapitalized version of the string.
     */
    public static String decapitalize(String name) {
        if ((name == null) || (name.length() == 0)) {
            return name;
        }
        char c0 = name.charAt(0);
        if ((name.length() > 1) && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(c0)) {
            return name;
        }
        if(Character.isLowerCase(c0)) {
            return name;
        }
        return Character.toLowerCase(c0) + name.substring(1);
    }

    /**
     * Returns a String which capitalizes the first letter of the string.
     *
     * @param name The string to be capitalized.
     * @return The capitalized version of the string.
     */
    public static String capitalize(String name) {
        if ((name == null) || (name.length() == 0)) {
            return name;
        }
        char c0 = name.charAt(0);
        if(Character.isUpperCase(c0)) {
            return name;
        }
        return Character.toUpperCase(c0) + name.substring(1);
    }
}
