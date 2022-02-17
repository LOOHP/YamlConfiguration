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