package com.loohp.yamlconfiguration.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnicodeUtilsTest {

    @Test
    void escapeTest() {
        String str = "Hey! \u2764\uFE0F";
        assertEquals("Hey! \\u2764\\uFE0F", UnicodeUtils.escape(str));
    }

    @Test
    void unescapeTest() {
        String str = "Hey! \\u2764\\uFE0F";
        assertEquals("Hey! \u2764\uFE0F", UnicodeUtils.unescape(str));
    }

}