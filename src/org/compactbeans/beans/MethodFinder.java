/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.compactbeans.beans.ReflectUtil.isPackageAccessible;

/**
 * This utility class provides {@code static} methods
 * to find a public method with specified name and parameter types
 * in specified class.
 *
 * @since 1.7
 *
 * @author Sergey A. Malenkov
 * @jdksource com.sun.beans.finder.MethodFinder
 * @jdksource com.sun.beans.finder.InstanceFinder
 */
public final class MethodFinder {
    private static final MethodCache CACHE = new MethodCache(MethodCache.Kind.SOFT, MethodCache.Kind.SOFT);

    /**
     * Finds public method (static or non-static)
     * that is accessible from public class.
     *
     * @param type  the class that can have method
     * @param name  the name of method to find
     * @param args  parameter types that is used to find method
     * @return object that represents found method
     * @throws NoSuchMethodException if method could not be found
     *                               or some methods are found
     */
    public static Method findMethod(Class<?> type, String name, Class<?>... args) throws NoSuchMethodException {
        if (name == null) {
            throw new IllegalArgumentException("Method name is not set");
        }
        PrimitiveWrapperMap.replacePrimitivesWithWrappers(args);
        Signature signature = new Signature(type, name, args);

        try {
            Method method = CACHE.get(signature);
            return (method == null) || isPackageAccessible(method.getDeclaringClass()) ? method : CACHE.create(signature);
        }
        catch (SignatureException exception) {
            throw exception.toNoSuchMethodException("Method '" + name + "' is not found");
        }
    }

    /**
     * Finds public non-static method
     * that is accessible from public class.
     *
     * @param type  the class that can have method
     * @param name  the name of method to find
     * @param args  parameter types that is used to find method
     * @return object that represents found method
     * @throws NoSuchMethodException if method could not be found
     *                               or some methods are found
     */
    public static Method findInstanceMethod(Class<?> type, String name, Class<?>... args) throws NoSuchMethodException {
        Method method = findMethod(type, name, args);
        if (Modifier.isStatic(method.getModifiers())) {
            throw new NoSuchMethodException("Method '" + name + "' is static");
        }
        return method;
    }

    /**
     * Finds public static method
     * that is accessible from public class.
     *
     * @param type  the class that can have method
     * @param name  the name of method to find
     * @param args  parameter types that is used to find method
     * @return object that represents found method
     * @throws NoSuchMethodException if method could not be found
     *                               or some methods are found
     */
    public static Method findStaticMethod(Class<?> type, String name, Class<?>... args) throws NoSuchMethodException {
        Method method = findMethod(type, name, args);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new NoSuchMethodException("Method '" + name + "' is not static");
        }
        return method;
    }

    /**
     * Finds method that is accessible from public class or interface through class hierarchy.
     *
     * @param method  object that represents found method
     * @return object that represents accessible method
     * @throws NoSuchMethodException if method is not accessible or is not found
     *                               in specified superclass or interface
     */
    public static Method findAccessibleMethod(Method method) throws NoSuchMethodException {
        Class<?> type = method.getDeclaringClass();
        if (Modifier.isPublic(type.getModifiers()) && isPackageAccessible(type)) {
            return method;
        }
        if (Modifier.isStatic(method.getModifiers())) {
            throw new NoSuchMethodException("Method '" + method.getName() + "' is not accessible");
        }
        for (Type generic : type.getGenericInterfaces()) {
            try {
                return findAccessibleMethod(method, generic);
            }
            catch (NoSuchMethodException exception) {
                // try to find in superclass or another interface
            }
        }
        return findAccessibleMethod(method, type.getGenericSuperclass());
    }

    /**
     * Finds method that accessible from specified class.
     *
     * @param method  object that represents found method
     * @param generic generic type that is used to find accessible method
     * @return object that represents accessible method
     * @throws NoSuchMethodException if method is not accessible or is not found
     *                               in specified superclass or interface
     */
    private static Method findAccessibleMethod(Method method, Type generic) throws NoSuchMethodException {
        String name = method.getName();
        Class<?>[] params = method.getParameterTypes();
        if (generic instanceof Class) {
            Class<?> type = (Class<?>) generic;
            return findAccessibleMethod(type.getMethod(name, params));
        }
        if (generic instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) generic;
            Class<?> type = (Class<?>) pt.getRawType();
            for (Method m : type.getMethods()) {
                if (m.getName().equals(name)) {
                    Class<?>[] pts = m.getParameterTypes();
                    if (pts.length == params.length) {
                        if (Arrays.equals(params, pts)) {
                            return findAccessibleMethod(m);
                        }
                        Type[] gpts = m.getGenericParameterTypes();
                        if (params.length == gpts.length) {
                            if (Arrays.equals(params, TypeResolver.erase(TypeResolver.resolve(pt, gpts)))) {
                                return findAccessibleMethod(m);
                            }
                        }
                    }
                }
            }
        }
        throw new NoSuchMethodException("Method '" + name + "' is not accessible");
    }


    private final String name;
    private final Class<?>[] args;

    /**
     * Creates method finder with specified array of parameter types.
     *
     * @param name  the name of method to find
     * @param args  the array of parameter types
     */
    MethodFinder(String name, Class<?>[] args) {
        this.args = args;
        this.name = name;
    }

    /**
     * Checks validness of the method.
     * The valid method should be public and
     * should have the specified name.
     *
     * @param method  the object that represents method
     * @return {@code true} if the method is valid,
     *         {@code false} otherwise
     */
    protected boolean isValid(Method method) {
        return Modifier.isPublic(method.getModifiers()) && method.getName().equals(this.name);
    }

    /**
     * Performs a search in the {@code methods} array.
     * The one method is selected from the array of the valid methods.
     * The list of parameters of the selected method shows
     * the best correlation with the list of arguments
     * specified at class initialization.
     * If more than one method is both accessible and applicable
     * to a method invocation, it is necessary to choose one
     * to provide the descriptor for the run-time method dispatch.
     * The most specific method should be chosen.
     *
     * @param methods  the array of methods to search within
     * @return the object that represents found method
     * @throws NoSuchMethodException if no method was found or several
     *                               methods meet the search criteria
     * @see #isAssignable
     */
    final Method find(Method[] methods) throws NoSuchMethodException {
        Map<Method, Class<?>[]> map = new HashMap<Method, Class<?>[]>();

        Method oldMethod = null;
        Class<?>[] oldParams = null;
        boolean ambiguous = false;

        for (Method newMethod : methods) {
            if (isValid(newMethod)) {
                Class<?>[] newParams = newMethod.getParameterTypes();
                if (newParams.length == this.args.length) {
                    PrimitiveWrapperMap.replacePrimitivesWithWrappers(newParams);
                    if (isAssignable(newParams, this.args)) {
                        if (oldMethod == null) {
                            oldMethod = newMethod;
                            oldParams = newParams;
                        } else {
                            boolean useNew = isAssignable(oldParams, newParams);
                            boolean useOld = isAssignable(newParams, oldParams);

                            if (useOld && useNew) {
                                // only if parameters are equal
                                useNew = !newMethod.isSynthetic();
                                useOld = !oldMethod.isSynthetic();
                            }
                            if (useOld == useNew) {
                                ambiguous = true;
                            } else if (useNew) {
                                oldMethod = newMethod;
                                oldParams = newParams;
                                ambiguous = false;
                            }
                        }
                    }
                }
                if (newMethod.isVarArgs()) {
                    int length = newParams.length - 1;
                    if (length <= this.args.length) {
                        Class<?>[] array = new Class<?>[this.args.length];
                        System.arraycopy(newParams, 0, array, 0, length);
                        if (length < this.args.length) {
                            Class<?> type = newParams[length].getComponentType();
                            if (type.isPrimitive()) {
                                type = PrimitiveWrapperMap.getType(type.getName());
                            }
                            for (int i = length; i < this.args.length; i++) {
                                array[i] = type;
                            }
                        }
                        map.put(newMethod, array);
                    }
                }
            }
        }
        for (Method newMethod : methods) {
            Class<?>[] newParams = map.get(newMethod);
            if (newParams != null) {
                if (isAssignable(newParams, this.args)) {
                    if (oldMethod == null) {
                        oldMethod = newMethod;
                        oldParams = newParams;
                    } else {
                        boolean useNew = isAssignable(oldParams, newParams);
                        boolean useOld = isAssignable(newParams, oldParams);

                        if (useOld && useNew) {
                            // only if parameters are equal
                            useNew = !newMethod.isSynthetic();
                            useOld = !oldMethod.isSynthetic();
                        }
                        if (useOld == useNew) {
                            if (oldParams == map.get(oldMethod)) {
                                ambiguous = true;
                            }
                        } else if (useNew) {
                            oldMethod = newMethod;
                            oldParams = newParams;
                            ambiguous = false;
                        }
                    }
                }
            }
        }

        if (ambiguous) {
            throw new NoSuchMethodException("Ambiguous methods are found");
        }
        if (oldMethod == null) {
            throw new NoSuchMethodException("Method is not found");
        }
        return oldMethod;
    }

    /**
     * Determines if every class in {@code min} array is either the same as,
     * or is a superclass of, the corresponding class in {@code max} array.
     * The length of every array must equal the number of arguments.
     * This comparison is performed in the {@link #find} method
     * before the first call of the isAssignable method.
     * If an argument equals {@code null}
     * the appropriate pair of classes does not take into consideration.
     *
     * @param min  the array of classes to be checked
     * @param max  the array of classes that is used to check
     * @return {@code true} if all classes in {@code min} array
     *         are assignable from corresponding classes in {@code max} array,
     *         {@code false} otherwise
     *
     * @see Class#isAssignableFrom
     */
    private boolean isAssignable(Class<?>[] min, Class<?>[] max) {
        for (int i = 0; i < this.args.length; i++) {
            if (null != this.args[i]) {
                if (!min[i].isAssignableFrom(max[i])) {
                    return false;
                }
            }
        }
        return true;
    }

}
