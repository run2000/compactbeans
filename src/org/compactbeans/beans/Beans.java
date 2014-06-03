package org.compactbeans.beans;

/*
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

import java.lang.reflect.Modifier;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This class provides some general purpose beans control methods.
 */

public final class Beans {

    private Beans() {
    }

    /**
     * <p>
     * Instantiate a JavaBean.
     * <p>
     * The bean is created based on a name relative to a class-loader.
     * This name should be a dot-separated name such as "a.b.c".
     * <p>
     * In Beans 1.0 the given name can indicate either a serialized object
     * or a class.  Other mechanisms may be added in the future.  In
     * beans 1.0 we first try to treat the beanName as a serialized object
     * name then as a class name.
     * <p>
     * When using the beanName as a serialized object name we convert the
     * given beanName to a resource pathname and add a trailing ".ser" suffix.
     * We then try to load a serialized object from that resource.
     * <p>
     * For example, given a beanName of "x.y", Beans.instantiate would first
     * try to read a serialized object from the resource "x/y.ser" and if
     * that failed it would try to load the class "x.y" and create an
     * instance of that class.
     *
     * @param     cls         the class-loader from which we should create
     *                        the bean.  If this is <code>null</code>,
     *                        then the system class-loader is used
     * @param     beanName    the name of the bean within the class-loader.
     *                        For example "sun.beanbox.foobah"
     *
     * @exception ClassNotFoundException if the class of a serialized
     *              object could not be found
     * @exception IOException if an I/O error occurs
     */

    public static Object instantiate(ClassLoader cls, String beanName) throws IOException, ClassNotFoundException {
        ClassLoader cls1 = cls;

        InputStream ins;
        ObjectInputStream oins = null;
        Object result = null;
        boolean serialized = false;
        IOException serex = null;

        // If the given classloader is null, we check if an
        // system classloader is available and (if so)
        // use that instead.
        // Note that calls on the system class loader will
        // look in the bootstrap class loader first.
        if (cls1 == null) {
            try {
                cls1 = ClassLoader.getSystemClassLoader();
            } catch (SecurityException ex) {
                // We're not allowed to access the system class loader.
                // Drop through.
            }
        }

        // Try to find a serialized object with this name
        final String serName = beanName.replace('.', '/').concat(".ser");
        final ClassLoader loader = cls1;
        ins = (InputStream)AccessController.doPrivileged
            (new PrivilegedAction() {
                public Object run() {
                    if (loader == null)
                        return ClassLoader.getSystemResourceAsStream(serName);
                    else
                        return loader.getResourceAsStream(serName);
                }
        });
        if (ins != null) {
            try {
                if (cls1 == null) {
                    oins = new ObjectInputStream(ins);
                } else {
                    oins = new ObjectInputStreamWithLoader(ins, cls1);
                }
                result = oins.readObject();
                serialized = true;
                oins.close();
            } catch (IOException ex) {
                ins.close();
                // Drop through and try opening the class.  But remember
                // the exception in case we can't find the class either.
                serex = ex;
            } catch (ClassNotFoundException ex) {
                ins.close();
                throw ex;
            }
        }

        if (result == null) {
            // No serialized object, try just instantiating the class
            Class cl;

            try {
                cl = ClassFinder.findClass(beanName, cls1);
            } catch (ClassNotFoundException ex) {
                // There is no appropriate class.  If we earlier tried to
                // deserialize an object and got an IO exception, throw that,
                // otherwise rethrow the ClassNotFoundException.
                if (serex != null) {
                    throw serex;
                }
                throw ex;
            }

            if (!Modifier.isPublic(cl.getModifiers())) {
                throw new ClassNotFoundException("" + cl + " : no public access");
            }

            /*
             * Try to instantiate the class.
             */

            try {
                result = cl.newInstance();
            } catch (Exception ex) {
                // We have to remap the exception to one in our signature.
                // But we pass extra information in the detail message.
                throw new ClassNotFoundException("" + cl + " : " + ex, ex);
            }
        }

        return result;
    }

    /**
     * From a given bean, obtain an object representing a specified
     * type view of that source object.
     * <p>
     * The result may be the same object or a different object.  If
     * the requested target view isn't available then the given
     * bean is returned.
     * <p>
     * This method is provided in Beans 1.0 as a hook to allow the
     * addition of more flexible bean behaviour in the future.
     *
     * @param bean <code>Object</code> from which we want to obtain a view
     * @param targetType The type of view we'd like to get
     *
     */
    public static Object getInstanceOf(Object bean, Class<?> targetType) {
        return bean;
    }

    /**
     * Check if a bean can be viewed as a given target type.
     * The result will be true if the Beans.getInstanceof method
     * can be used on the given bean to obtain an object that
     * represents the specified targetType type view.
     *
     * @param bean  Bean from which we want to obtain a view.
     * @param targetType  The type of view we'd like to get.
     * @return "true" if the given bean supports the given targetType.
     *
     */
    public static boolean isInstanceOf(Object bean, Class<?> targetType) {
        return IntrospectorSupport.isSubclass(bean.getClass(), targetType);
    }

    /**
     * This subclass of ObjectInputStream delegates loading of classes to
     * an existing ClassLoader.
     */

    private static class ObjectInputStreamWithLoader extends ObjectInputStream
    {
        private final ClassLoader loader;

        /**
         * Loader must be non-null;
         */

        public ObjectInputStreamWithLoader(InputStream in, ClassLoader loader)
                throws IOException, StreamCorruptedException {

            super(in);
            if (loader == null) {
                throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
            }
            this.loader = loader;
        }

        /**
         * Use the given ClassLoader rather than using the system class
         */
        protected Class resolveClass(ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException {

            String cname = classDesc.getName();
            return ClassFinder.resolveClass(cname, this.loader);
        }
    }
}
