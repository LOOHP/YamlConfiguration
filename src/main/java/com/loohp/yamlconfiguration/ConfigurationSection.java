package com.loohp.yamlconfiguration;

import com.amihaiemil.eoyaml.Comment;
import com.amihaiemil.eoyaml.Scalar;
import com.amihaiemil.eoyaml.ScalarComment;
import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationSection implements IConfigurationSection {

    public static ConfigurationSection newConfigurationSection() {
        return new RootConfigurationSection(Yaml.createYamlMappingBuilder().build());
    }

    protected RootConfigurationSection root;
    protected String currentPath;
    protected YamlMapping currentMapping;

    protected ConfigurationSection(RootConfigurationSection root, String currentPath, YamlMapping currentMapping) {
        this.root = root;
        this.currentPath = currentPath;
        this.currentMapping = currentMapping;
    }

    public boolean isRoot() {
        return root == null || root == this;
    }

    @Override
    public void set(String path, Object value) {
        root.set(toFullPath(path), value);
        currentMapping = root.getConfigurationSection(currentPath).currentMapping;
    }

    @Override
    public List<String> getKeys(boolean recursive) {
        if (recursive) {
            List<String> keys = new ArrayList<>();
            for (YamlNode node : currentMapping.keys()) {
                String key = node.asScalar().value();
                keys.add(key);
                if (isConfigurationSection(key)) {
                    keys.addAll(getConfigurationSection(key).getKeys(true).stream().map(each -> key + "." + each).collect(Collectors.toList()));
                }
            }
            return keys;
        } else {
            return currentMapping.keys().stream().map(each -> each.asScalar().value()).collect(Collectors.toList());
        }
    }

    @Override
    public Map<String, Object> getValues(boolean recursive) {
        Map<String, Object> mapping = new LinkedHashMap<>();
        for (String key : getKeys(recursive)) {
            if (isConfigurationSection(key)) {
                mapping.put(key, getConfigurationSection(key));
            } else if (isList(key)) {
                mapping.put(key, getList(key));
            } else {
                mapping.put(key, get(key));
            }
        }
        return mapping;
    }

    @Override
    public boolean contains(String path) {
        return getNode(path) != null;
    }

    @Override
    public void setAboveComment(String path, String comment) {
        root.setAboveComment(toFullPath(path), comment);
        currentMapping = root.getConfigurationSection(currentPath).currentMapping;
    }

    @Override
    public void setInlineComment(String path, String comment) {
        root.setInlineComment(toFullPath(path), comment);
        currentMapping = root.getConfigurationSection(currentPath).currentMapping;
    }

    @Override
    public String getAboveComment(String path) {
        YamlNode node = getNode(path);
        if (node == null) {
            return null;
        }
        return node.comment().value();
    }

    @Override
    public String getInlineComment(String path) {
        YamlNode node = getNode(path);
        if (node == null) {
            return null;
        }
        Comment comment = node.comment();
        if (comment instanceof ScalarComment) {
            return ((ScalarComment) comment).inline().value();
        }
        return "";
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        YamlMapping mapping = getMapping(path);
        if (mapping == null) {
            return null;
        }
        return new ConfigurationSection(root, toFullPath(path), mapping);
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return getMapping(path) != null;
    }

    @Override
    public Object get(String path, Object def) {
        Scalar scalar = getScalar(path);
        return scalar == null ? def : scalar.value();
    }

    @Override
    public String getString(String path, String def) {
        Scalar scalar = getScalar(path);
        return scalar == null ? def : scalar.value();
    }

    @Override
    public boolean isString(String path) {
        return getScalar(path) != null;
    }

    @Override
    public int getInt(String path, int def) {
        Scalar scalar = getScalar(path);
        return scalar == null ? def : Integer.parseInt(scalar.value());
    }

    @Override
    public boolean isInt(String path) {
        Scalar scalar = getScalar(path);
        if (scalar == null) {
            return false;
        }
        try {
            Integer.parseInt(scalar.value());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Scalar scalar = getScalar(path);
        if (scalar == null) {
            return def;
        }
        if (scalar.value().equalsIgnoreCase("true")) {
            return true;
        }
        if (scalar.value().equalsIgnoreCase("false")) {
            return false;
        }
        return def;
    }

    @Override
    public boolean isBoolean(String path) {
        Scalar scalar = getScalar(path);
        if (scalar == null) {
            return false;
        }
        return scalar.value().equalsIgnoreCase("true") || scalar.value().equalsIgnoreCase("false");
    }

    @Override
    public double getDouble(String path, double def) {
        Scalar scalar = getScalar(path);
        return scalar == null ? def : Double.parseDouble(scalar.value());
    }

    @Override
    public boolean isDouble(String path) {
        Scalar scalar = getScalar(path);
        if (scalar == null) {
            return false;
        }
        try {
            Double.parseDouble(scalar.value());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public long getLong(String path, long def) {
        Scalar scalar = getScalar(path);
        return scalar == null ? def : Long.parseLong(scalar.value());
    }

    @Override
    public boolean isLong(String path) {
        Scalar scalar = getScalar(path);
        if (scalar == null) {
            return false;
        }
        try {
            Long.parseLong(scalar.value());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean isList(String path) {
        return getSequence(path) != null;
    }

    @Override
    public List<Object> getList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return extractList(sequence);
    }

    @Override
    public List<String> getStringList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> each.asScalar().value()).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getIntegerList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> Integer.parseInt(each.asScalar().value())).collect(Collectors.toList());
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> {
            String value = each.asScalar().value();
            if (value.equalsIgnoreCase("true")) {
                return true;
            } else if (value.equalsIgnoreCase("false")) {
                return false;
            }
            throw new IllegalArgumentException(value + " is not a valid boolean!");
        }).collect(Collectors.toList());
    }

    @Override
    public List<Double> getDoubleList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> Double.parseDouble(each.asScalar().value())).collect(Collectors.toList());
    }

    @Override
    public List<Float> getFloatList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> Float.parseFloat(each.asScalar().value())).collect(Collectors.toList());
    }

    @Override
    public List<Long> getLongList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> Long.parseLong(each.asScalar().value())).collect(Collectors.toList());
    }

    @Override
    public List<Byte> getByteList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> Byte.parseByte(each.asScalar().value())).collect(Collectors.toList());
    }

    @Override
    public List<Character> getCharacterList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> each.asScalar().value().toCharArray()[0]).collect(Collectors.toList());
    }

    @Override
    public List<Short> getShortList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> Short.parseShort(each.asScalar().value())).collect(Collectors.toList());
    }

    protected YamlNode getNode(String path) {
        String[] paths = toPathArray(path);
        YamlNode node = currentMapping;
        for (String p : paths) {
            if (node == null) {
                return null;
            }
            node = node.asMapping().value(p);
        }
        return node;
    }

    protected Scalar getScalar(String path) {
        YamlNode node = getNode(path);
        if (node == null) {
            return null;
        }
        if (!(node instanceof Scalar)) {
            return null;
        }
        return node.asScalar();
    }

    protected YamlSequence getSequence(String path) {
        YamlNode node = getNode(path);
        if (node == null) {
            return null;
        }
        if (!(node instanceof YamlSequence)) {
            return null;
        }
        return node.asSequence();
    }

    protected YamlMapping getMapping(String path) {
        YamlNode node = getNode(path);
        if (node == null) {
            return null;
        }
        if (!(node instanceof YamlMapping)) {
            return null;
        }
        return node.asMapping();
    }

    protected List<Object> extractList(YamlSequence sequence) {
        List<Object> list = new LinkedList<>();
        for (YamlNode node : sequence.values()) {
            if (node instanceof Scalar) {
                list.add(((Scalar) node).value());
            } else if (node instanceof YamlSequence) {
                list.add(extractList((YamlSequence) node));
            } else if (node instanceof YamlMapping) {
                list.add(new RootConfigurationSection((YamlMapping) node));
            } else {
                list.add(node);
            }
        }
        return list;
    }

    protected YamlSequenceBuilder createSequence(Collection<?> list) {
        YamlSequenceBuilder builder = Yaml.createYamlSequenceBuilder();
        for (Object obj : list) {
            if (obj instanceof Collection) {
                builder = builder.add(createSequence((Collection<?>) obj).build());
            } else if (obj instanceof RootConfigurationSection && ((RootConfigurationSection) obj).isRoot()) {
                builder = builder.add(((RootConfigurationSection) obj).currentMapping);
            } else {
                builder = builder.add(obj.toString());
            }
        }
        return builder;
    }

    protected String toFullPath(String relativePath) {
        if (currentPath.isEmpty()) {
            return relativePath;
        }
        return currentPath + "." + relativePath;
    }

    protected String[] toPathArray(String path) {
        return path.split("\\.");
    }

    protected String fromPathArray(String[] paths) {
        return String.join(".", paths);
    }

}
