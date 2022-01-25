package com.loohp.yamlconfiguration;

import java.util.List;
import java.util.Map;

public interface IConfigurationSection {

    void set(String path, Object value);

    List<String> getKeys(boolean recursive);

    Map<String, Object> getValues(boolean recursive);

    boolean contains(String path);

    void setAboveComment(String path, String comment);

    void setInlineComment(String path, String comment);

    String getAboveComment(String path);

    String getInlineComment(String path);

    ConfigurationSection getConfigurationSection(String path);

    boolean isConfigurationSection(String path);

    Object get(String path, Object def);

    default Object get(String path) {
        return get(path, null);
    }

    default String getString(String path) {
        return getString(path, null);
    }

    String getString(String path, String def);

    boolean isString(String path);

    default int getInt(String path) {
        return getInt(path, 0);
    }

    int getInt(String path, int def);

    boolean isInt(String path);

    default boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    boolean getBoolean(String path, boolean def);

    boolean isBoolean(String path);

    default double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    double getDouble(String path, double def);

    boolean isDouble(String path);

    default long getLong(String path) {
        return getLong(path, 0L);
    }

    long getLong(String path, long def);

    boolean isLong(String path);

    boolean isList(String path);

    List<Object> getList(String path);

    List<String> getStringList(String path);

    List<Integer> getIntegerList(String path);

    List<Boolean> getBooleanList(String path);

    List<Double> getDoubleList(String path);

    List<Float> getFloatList(String path);

    List<Long> getLongList(String path);

    List<Byte> getByteList(String path);

    List<Character> getCharacterList(String path);

    List<Short> getShortList(String path);

}
