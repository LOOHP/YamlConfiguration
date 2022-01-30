package com.loohp.yamlconfiguration.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixerUtilsTest {

    @Test
    void fixYamlTest() {
        assertEquals(" Hello: \"test\"", FixerUtils.fixYaml(" Hello: test"));
        assertEquals(" Hello: \"true\"", FixerUtils.fixYaml(" Hello: true"));
        assertEquals(" Hello: ", FixerUtils.fixYaml(" Hello: "));
        assertEquals(" Hello: \"\"", FixerUtils.fixYaml(" Hello: \"\""));
        assertEquals(" Hello: \"test\"", FixerUtils.fixYaml(" Hello: \"test\""));
        assertEquals(" Hello: \"true\"", FixerUtils.fixYaml(" Hello: \"true\""));
        assertEquals("    - \"elements\"", FixerUtils.fixYaml("    - elements"));
        assertEquals("    - \"elements\"", FixerUtils.fixYaml("    - \"elements\""));
        assertEquals("    - [elements, test]", FixerUtils.fixYaml("    - [elements, test]"));
    }

}