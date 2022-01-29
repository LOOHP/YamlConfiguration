package com.loohp.yamlconfiguration;

import com.amihaiemil.eoyaml.Comment;
import com.amihaiemil.eoyaml.Scalar;
import com.amihaiemil.eoyaml.ScalarComment;
import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;
import com.loohp.yamlconfiguration.utils.UnicodeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurationSection implements IConfigurationSection {

    public static ConfigurationSection newConfigurationSection() {
        return new RootConfigurationSection(Yaml.createYamlMappingBuilder().build());
    }

    public static ConfigurationSection newConfigurationSection(ConfigurationSection section) {
        return new RootConfigurationSection(section.currentMapping);
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
        return false;
    }

    public boolean fromExistingYaml() {
        if (root == this) {
            return false;
        }
        return root.fromExistingYaml();
    }

    @Override
    public void set(String path, Object value) {
        root.set(toFullPath(path), value);
    }

    @Override
    public List<String> getKeys(boolean recursive) {
        if (recursive) {
            Set<YamlNode> keys = currentMapping.keys();
            List<String> recursiveKeys = new ArrayList<>(keys.size());
            for (YamlNode node : keys) {
                String key = node.asScalar().value();
                recursiveKeys.add(key);
                if (isConfigurationSection(key)) {
                    recursiveKeys.addAll(getConfigurationSection(key).getKeys(true).stream().map(each -> key + "." + each).collect(Collectors.toList()));
                }
            }
            return recursiveKeys;
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
    }

    @Override
    public void setInlineComment(String path, String comment) {
        root.setInlineComment(toFullPath(path), comment);
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
        return root.createOrGetSubsection(toFullPath(path), mapping);
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return getMapping(path) != null;
    }

    @Override
    public Object get(String path, Object def) {
        YamlNode node = getNode(path);
        if (node instanceof Scalar) {
            return UnicodeUtils.unescape((Scalar) node);
        } else if (node instanceof YamlSequence) {
            return getList(path);
        } else if (node instanceof YamlMapping) {
            return getConfigurationSection(path);
        }
        return def;
    }

    @Override
    public String getString(String path, String def) {
        Scalar scalar = getScalar(path);
        return scalar == null ? def : UnicodeUtils.unescape(scalar);
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
        return sequence.values().stream().map(each -> UnicodeUtils.unescape(each.asScalar())).collect(Collectors.toList());
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
        return sequence.values().stream().map(each -> UnicodeUtils.unescape(each.asScalar()).toCharArray()[0]).collect(Collectors.toList());
    }

    @Override
    public List<Short> getShortList(String path) {
        YamlSequence sequence = getSequence(path);
        if (sequence == null) {
            return null;
        }
        return sequence.values().stream().map(each -> Short.parseShort(each.asScalar().value())).collect(Collectors.toList());
    }

    protected void toSubsection(RootConfigurationSection root, String currentPath) {
        //do nothing
    }

    @Override
    public String toString() {
        return currentMapping.toString();
    }

    protected YamlNode getNode(String path) {
        String[] paths = toPathArray(path);
        YamlNode node = currentMapping;
        for (String p : paths) {
            if (!(node instanceof YamlMapping)) {
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
        Collection<YamlNode> values = sequence.values();
        List<Object> list = new ArrayList<>(values.size());
        for (YamlNode node : values) {
            if (node instanceof Scalar) {
                list.add(UnicodeUtils.unescape((Scalar) node));
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
            } else if (obj instanceof String) {
                builder = builder.add(UnicodeUtils.escape((String) obj));
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
        if (path.isEmpty()) {
            return new String[0];
        }
        return path.split("\\.");
    }

    protected String fromPathArray(String[] paths) {
        return String.join(".", paths);
    }

    protected String fromPathList(List<String> paths) {
        return String.join(".", paths);
    }

}
