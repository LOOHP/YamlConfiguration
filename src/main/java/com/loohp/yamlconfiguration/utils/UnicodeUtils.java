/*
 * This file is part of YamlConfiguration.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.yamlconfiguration.utils;

import com.amihaiemil.eoyaml.Scalar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnicodeUtils {

    private static final Pattern SINGLE_QUOTED_PATTERN = Pattern.compile("'((?:[^']|'')*)'");

    private static Class<?> readPlainScalarClass;
    private static Field scalarField;
    private static Method getTrimmedMethod;

    static {
        try {
            readPlainScalarClass = Class.forName("com.amihaiemil.eoyaml.ReadPlainScalar");
            scalarField = readPlainScalarClass.getDeclaredField("scalar");
            scalarField.setAccessible(true);
            getTrimmedMethod = scalarField.getType().getDeclaredMethod("trimmed");
            getTrimmedMethod.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static String unescape(Scalar scalar) {
        String str = scalar.value();
        if (readPlainScalarClass.isInstance(scalar)) {
            try {
                String trimmed = (String) getTrimmedMethod.invoke(scalarField.get(scalar));
                Matcher matcher = SINGLE_QUOTED_PATTERN.matcher(trimmed);
                while (matcher.find()) {
                    if (matcher.group(1).equals(str)) {
                        str = str.replace("''", "'");
                        break;
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return unescape(str);
    }

    public static String unescape(String str) {
        if (str == null) {
            return null;
        }
        return StringEscapeUtils.unescapeJava(str);
    }

    public static String escape(String str) {
        if (str == null) {
            return null;
        }
        return StringEscapeUtils.escapeJava(str);
    }

}