package com.loohp.yamlconfiguration;

import com.amihaiemil.eoyaml.Yaml;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationSectionTest {

    @Test
    void newEmptyConfigurationSectionTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        assertEquals(0, section.getKeys(true).size());
    }

    @Test
    void newConfigurationSectionFromExisting() {
        ConfigurationSection section1 = ConfigurationSection.newConfigurationSection();
        section1.set("Key", "value");
        ConfigurationSection section2 = ConfigurationSection.newConfigurationSection(section1);
        assertEquals(1, section2.getKeys(true).size());
        assertEquals("value", section2.get("Key"));
    }

    @Test
    void getAndSetTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2.Key", "value2");
        section.set("Key3.Key.Key", Arrays.asList(1, 2, 3));

        ConfigurationSection section2 = ConfigurationSection.newConfigurationSection();
        section2.set("SubKey", "sub-value");
        section.set("Key4.Key.Key.Key", section2);

        assertEquals("value1", section.get("Key"));
        assertEquals("value2", section.get("Key2.Key"));
        assertEquals(Arrays.asList(1, 2, 3), section.getIntegerList("Key3.Key.Key"));
        assertEquals("sub-value", section.get("Key4.Key.Key.Key.SubKey"));

        section.set("Key", null);
        assertNull(section.get("Key"));
    }

    @Test
    void getKeysTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2.Key", "value2");
        section.set("Key3.Key.Key", Arrays.asList(1, 2, 3));

        assertEquals(Arrays.asList("Key", "Key2", "Key3"), section.getKeys(false));
        assertEquals(Arrays.asList("Key", "Key2", "Key2.Key", "Key3", "Key3.Key", "Key3.Key.Key"), section.getKeys(true));
    }

    @Test
    void getValuesTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2.Key", "value2");
        section.set("Key3.Key.Key", Arrays.asList(1, 2, 3));

        assertEquals("{Key=value1, Key2={Key=value2}, Key3={Key={Key=[1, 2, 3]}, Key.Key=[1, 2, 3]}}", section.getValues(false).toString());
        assertEquals("{Key=value1, Key2={Key=value2}, Key2.Key=value2, Key3={Key={Key=[1, 2, 3]}, Key.Key=[1, 2, 3]}, Key3.Key={Key=[1, 2, 3]}, Key3.Key.Key=[1, 2, 3]}", section.getValues(true).toString());
    }

    @Test
    void containsTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2.Key", "value2");
        section.set("Key3.Key.Key", Arrays.asList(1, 2, 3));

        assertTrue(section.contains("Key"));
        assertTrue(section.contains("Key2.Key"));
        assertTrue(section.contains("Key3.Key.Key"));
        assertFalse(section.contains("NonExist"));
        assertFalse(section.contains("NonExist.Key"));
    }

    @Test
    void aboveCommentTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2.Key", "value2");
        section.set("Key3.Key.Key", Arrays.asList(1, 2, 3));

        section.setAboveComment("Key2", "above comment");
        section.setAboveComment("Key3.Key.Key", "above list comment");

        assertEquals("above comment", section.getAboveComment("Key2"));
        assertEquals("above list comment", section.getAboveComment("Key3.Key.Key"));
        assertEquals("", section.getAboveComment("Key2.Key"));
        assertNull(section.getAboveComment("Key4"));
    }

    @Test
    void inlineCommentTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2.Key", "value2");
        section.set("Key3.Key.Key", Arrays.asList(1, 2, 3));

        section.setInlineComment("Key", "inline comment");
        section.setInlineComment("Key2", "another inline comment");

        assertEquals("inline comment", section.getInlineComment("Key"));
        assertEquals("", section.getInlineComment("Key2"));
        assertEquals("", section.getInlineComment("Key2.Key"));
        assertNull(section.getInlineComment("Key4"));
    }

    @Test
    void isAndGetConfigurationSectionTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2.Key", "value2");

        assertFalse(section.isConfigurationSection("Key"));
        assertTrue(section.isConfigurationSection("Key2"));

        ConfigurationSection subsection = section.getConfigurationSection("Key2");
        assertEquals("value2", subsection.get("Key"));
    }

    @Test
    void isAndGetStringTest() {
        ConfigurationSection section = new RootConfigurationSection(Yaml.createYamlMappingBuilder().add("Key", "\\u2764\\uFE0F").build());
        assertTrue(section.isString("Key"));
        assertEquals("\u2764\uFE0F", section.getString("Key"));
    }

    @Test
    void isAndGetIntTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2", 2);

        assertFalse(section.isInt("Key"));
        assertTrue(section.isInt("Key2"));

        assertEquals(2, section.getInt("Key2"));
    }

    @Test
    void isAndGetBooleanTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2", false);

        assertFalse(section.isBoolean("Key"));
        assertTrue(section.isBoolean("Key2"));

        assertFalse(section.getBoolean("Key2"));
    }

    @Test
    void isAndGetDoubleTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2", 2.456);

        assertFalse(section.isDouble("Key"));
        assertTrue(section.isDouble("Key2"));

        assertEquals(2.456, section.getDouble("Key2"));
    }

    @Test
    void isAndGetLongTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2", Long.MAX_VALUE);

        assertFalse(section.isLong("Key"));
        assertTrue(section.isLong("Key2"));

        assertEquals(Long.MAX_VALUE, section.getLong("Key2"));
    }

    @Test
    void isAndGetListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2", Arrays.asList("1", "2", "3", "4"));

        assertFalse(section.isList("Key"));
        assertTrue(section.isList("Key2"));

        assertEquals(Arrays.asList("1", "2", "3", "4"), section.getList("Key2"));
    }

    @Test
    void isAndGetStringListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", "value1");
        section.set("Key2", Arrays.asList("1", "2", "3", "4"));

        assertFalse(section.isList("Key"));
        assertTrue(section.isList("Key2"));

        assertEquals(Arrays.asList("1", "2", "3", "4"), section.getList("Key2"));
    }

    @Test
    void getIntegerListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", Arrays.asList(1, 2, 3, 4));
        section.set("Key2", Arrays.asList("Hello", "World"));

        assertEquals(Arrays.asList(1, 2, 3, 4), section.getIntegerList("Key"));
        assertThrows(IllegalArgumentException.class, () -> section.getIntegerList("Key2"));
    }

    @Test
    void getBooleanListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", Arrays.asList(true, false));
        section.set("Key2", Arrays.asList("Hello", "World"));

        assertEquals(Arrays.asList(true, false), section.getBooleanList("Key"));
        assertThrows(IllegalArgumentException.class, () -> section.getBooleanList("Key2"));
    }

    @Test
    void getDoubleListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", Arrays.asList(1.2, 2.2, 3.2, 4.2));
        section.set("Key2", Arrays.asList("Hello", "World"));

        assertEquals(Arrays.asList(1.2, 2.2, 3.2, 4.2), section.getDoubleList("Key"));
        assertThrows(IllegalArgumentException.class, () -> section.getDoubleList("Key2"));
    }

    @Test
    void getFloatListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", Arrays.asList(1.2f, 2.2f, 3.2f, 4.2f));
        section.set("Key2", Arrays.asList("Hello", "World"));

        assertEquals(Arrays.asList(1.2f, 2.2f, 3.2f, 4.2f), section.getFloatList("Key"));
        assertThrows(IllegalArgumentException.class, () -> section.getFloatList("Key2"));
    }

    @Test
    void getLongListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", Arrays.asList(Long.MAX_VALUE, Long.MIN_VALUE));
        section.set("Key2", Arrays.asList("Hello", "World"));

        assertEquals(Arrays.asList(Long.MAX_VALUE, Long.MIN_VALUE), section.getLongList("Key"));
        assertThrows(IllegalArgumentException.class, () -> section.getLongList("Key2"));
    }

    @Test
    void getByteListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", Arrays.asList((byte) 1, (byte) 2, (byte) 3, (byte) 4));
        section.set("Key2", Arrays.asList("Hello", "World"));

        assertEquals(Arrays.asList((byte) 1, (byte) 2, (byte) 3, (byte) 4), section.getByteList("Key"));
        assertThrows(IllegalArgumentException.class, () -> section.getByteList("Key2"));
    }

    @Test
    void getCharacterList() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", Arrays.asList('A', 'B', 'C', 'D'));
        section.set("Key2", Arrays.asList("Hello", "World"));

        assertEquals(Arrays.asList('A', 'B', 'C', 'D'), section.getCharacterList("Key"));
        assertThrows(IllegalArgumentException.class, () -> section.getCharacterList("Key2"));
    }

    @Test
    void getShortListTest() {
        ConfigurationSection section = ConfigurationSection.newConfigurationSection();
        section.set("Key", Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4));
        section.set("Key2", Arrays.asList("Hello", "World"));

        assertEquals(Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4), section.getShortList("Key"));
        assertThrows(IllegalArgumentException.class, () -> section.getShortList("Key2"));
    }

}