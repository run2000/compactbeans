/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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
package org.compactbeans.beans.test;

import org.compactbeans.beans.DescriptorData;

/**
 * Test of properties and attributes on the DescriptorData class.
 * This tests that the boolean values work independently, and that
 * the cloning of the attributes table creates an independent copy.
 */
public final class DescriptorDataTest {

    private DescriptorDataTest() {
    }

    public static void main(String args[]) {
        DescriptorData data1 = new DescriptorData();

        if(data1.isExpert()) {
            throw new Error("Expert should be false");
        }
        if(data1.isHidden()) {
            throw new Error("Hidden should be false");
        }
        if(data1.isPreferred()) {
            throw new Error("Preferred should be false");
        }

        Iterable<String> it1 = data1.getAttributeNames();
        if(data1.hasAttributes()) {
            throw new Error("There should be no attributes");
        }
        if(it1.iterator().hasNext()) {
            throw new Error("Attribute names should be empty");
        }

        data1.setValue("test1", "value1");

        // Unfortunate, have to get a fresh iterable once we add the first attribute
        it1 = data1.getAttributeNames();
        if(!data1.hasAttributes()) {
            throw new Error("There should be attributes");
        }
        if(!it1.iterator().hasNext()) {
            throw new Error("Attribute names should contain an item");
        }
        if(! "value1".equals(data1.getValue("test1"))) {
            throw new Error("Attribute test1 should be value1");
        }

        DescriptorData data2 = (DescriptorData)data1.clone();

        if(!data2.hasAttributes()) {
            throw new Error("There should be attributes");
        }

        Iterable<String> it2 = data2.getAttributeNames();

        if(!it2.iterator().hasNext()) {
            throw new Error("Attribute names should contain an item");
        }

        if( data1.getValue("test2") != null ) {
            throw new Error("Value should not exist yet");
        }
        if( data2.getValue("test2") != null ) {
            throw new Error("Value should not exist yet");
        }

        data1.setValue("test2", "value2");

        if( data1.getValue("test2") == null ) {
            throw new Error("Value should have a value");
        }
        if( data2.getValue("test2") != null ) {
            throw new Error("Value should not exist yet");
        }

        data2.setValue("test3", "value3");

        if( data1.getValue("test3") != null ) {
            throw new Error("Value should not exist yet");
        }
        if( data2.getValue("test3") == null ) {
            throw new Error("Value should have a value");
        }

        data1.setExpert(true);

        if(!data1.isExpert()) {
            throw new Error("Expert should be true");
        }
        if(data1.isHidden()) {
            throw new Error("Hidden should be false");
        }
        if(data1.isPreferred()) {
            throw new Error("Preferred should be false");
        }

        if(data2.isExpert()) {
            throw new Error("Expert should be false");
        }
        if(data2.isHidden()) {
            throw new Error("Hidden should be false");
        }
        if(data2.isPreferred()) {
            throw new Error("Preferred should be false");
        }

        data1.setHidden(true);
        if(!data1.isExpert()) {
            throw new Error("Expert should be true");
        }
        if(!data1.isHidden()) {
            throw new Error("Hidden should be true");
        }
        if(data1.isPreferred()) {
            throw new Error("Preferred should be false");
        }

        if(data2.isExpert()) {
            throw new Error("Expert should be false");
        }
        if(data2.isHidden()) {
            throw new Error("Hidden should be false");
        }
        if(data2.isPreferred()) {
            throw new Error("Preferred should be false");
        }

        data1.setExpert(false);

        if(data1.isExpert()) {
            throw new Error("Expert should be false");
        }
        if(!data1.isHidden()) {
            throw new Error("Hidden should be true");
        }
        if(data1.isPreferred()) {
            throw new Error("Preferred should be false");
        }

        data1.setPreferred(true);

        if(data1.isExpert()) {
            throw new Error("Expert should be false");
        }
        if(!data1.isHidden()) {
            throw new Error("Hidden should be true");
        }
        if(!data1.isPreferred()) {
            throw new Error("Preferred should be true");
        }

        data1.setHidden(false);
        data1.setExpert(true);

        if(!data1.isExpert()) {
            throw new Error("Expert should be true");
        }
        if(data1.isHidden()) {
            throw new Error("Hidden should be false");
        }
        if(!data1.isPreferred()) {
            throw new Error("Preferred should be true");
        }

        data1.setHidden(true);

        if(!data1.isExpert()) {
            throw new Error("Expert should be true");
        }
        if(!data1.isHidden()) {
            throw new Error("Hidden should be true");
        }
        if(!data1.isPreferred()) {
            throw new Error("Preferred should be true");
        }
    }
}
