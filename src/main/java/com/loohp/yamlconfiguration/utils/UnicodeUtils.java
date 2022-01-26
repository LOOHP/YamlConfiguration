package com.loohp.yamlconfiguration.utils;

import com.amihaiemil.eoyaml.Scalar;

import java.io.StringWriter;
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
        StringWriter out = new StringWriter();
        if (str == null) {
            return null;
        }
        int sz = str.length();
        StringBuilder unicode = new StringBuilder(4);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == 4) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try {
                        int value = Integer.parseInt(unicode.toString(), 16);
                        out.write((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw new RuntimeException("Unable to parse unicode value: " + unicode, nfe);
                    }
                }
                continue;
            }
            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
                    case '\\':
                        out.write('\\');
                        break;
                    case '\'':
                        out.write('\'');
                        break;
                    case '\"':
                        out.write('"');
                        break;
                    case 'r':
                        out.write('\r');
                        break;
                    case 'f':
                        out.write('\f');
                        break;
                    case 't':
                        out.write('\t');
                        break;
                    case 'n':
                        out.write('\n');
                        break;
                    case 'b':
                        out.write('\b');
                        break;
                    case 'u': {
                        // uh-oh, we're in unicode country....
                        inUnicode = true;
                        break;
                    }
                    default:
                        out.write(ch);
                        break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            out.write(ch);
        }
        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.write('\\');
        }
        return out.toString();
    }

}
