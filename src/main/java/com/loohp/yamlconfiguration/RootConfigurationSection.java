package com.loohp.yamlconfiguration;

import com.amihaiemil.eoyaml.Scalar;
import com.amihaiemil.eoyaml.ScalarComment;
import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;
import com.amihaiemil.eoyaml.extensions.MergedYamlMapping;

import java.util.Collection;

public class RootConfigurationSection extends ConfigurationSection {

    protected RootConfigurationSection(YamlMapping rootMapping) {
        super(null, "", rootMapping);
    }

    @Override
    public void set(String path, Object value) {
        if (!isRoot()) {
            super.set(path, value);
            return;
        }
        String comment = "";
        YamlNode currentNode = getNode(path);
        if (currentNode != null) {
            comment = currentNode.comment().value();
        }
        YamlNode newNode;
        if (value instanceof Collection) {
            newNode = createSequence((Collection<?>) value).build(comment);
        } else if (value instanceof RootConfigurationSection && ((RootConfigurationSection) value).isRoot()) {
            if (value == this) {
                throw new IllegalStateException("RootConfigurationSection cannot be nested into self");
            }
            YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
            RootConfigurationSection section = (RootConfigurationSection) value;
            YamlMapping mapping = section.currentMapping;
            for (YamlNode key : mapping.keys()) {
                builder = builder.add(key, mapping.value(key));
            }
            newNode = builder.build(comment);
            section.root = this;
            section.currentPath = path;
        } else {
            String inlineComment = "";
            if (currentNode != null && currentNode.comment() instanceof ScalarComment) {
                inlineComment = ((ScalarComment) currentNode.comment()).inline().value();
            }
            newNode = Yaml.createYamlScalarBuilder().addLine(value.toString()).buildPlainScalar(comment, inlineComment);
        }
        String[] paths = toPathArray(path);
        YamlMapping mapping = Yaml.createYamlMappingBuilder().add(paths[paths.length - 1], newNode).build();
        for (int i = paths.length - 2; i >= 0; i--) {
            mapping = Yaml.createYamlMappingBuilder().add(paths[i], mapping).build();
        }
        currentMapping = new MergedYamlMapping(currentMapping, mapping, true);
    }

    @Override
    public void setAboveComment(String path, String comment) {
        if (!isRoot()) {
            super.setAboveComment(path, comment);
            return;
        }
        if (comment == null) {
            comment = "";
        }
        YamlNode currentNode = getNode(path);
        if (currentNode == null) {
            return;
        }
        YamlNode newNode;
        if (currentNode instanceof Scalar) {
            Scalar scalar = (Scalar) currentNode;
            newNode = Yaml.createYamlScalarBuilder().addLine(scalar.value()).buildPlainScalar(comment, ((ScalarComment) scalar.comment()).inline().value());
        } else if (currentNode instanceof YamlSequence) {
            YamlSequenceBuilder builder = Yaml.createYamlSequenceBuilder();
            for (YamlNode each : ((YamlSequence) currentNode).values()) {
                builder = builder.add(each);
            }
            newNode = builder.build(comment);
        } else if (currentNode instanceof YamlMapping) {
            YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
            YamlMapping mapping = (YamlMapping) currentNode;
            for (YamlNode key : mapping.keys()) {
                builder = builder.add(key, mapping.value(key));
            }
            newNode = builder.build(comment);
        } else {
            return;
        }
        String[] paths = toPathArray(path);
        YamlMapping mapping = Yaml.createYamlMappingBuilder().add(paths[paths.length - 1], newNode).build();
        for (int i = paths.length - 2; i >= 0; i--) {
            mapping = Yaml.createYamlMappingBuilder().add(paths[i], mapping).build();
        }
        currentMapping = new MergedYamlMapping(currentMapping, mapping, true);
    }

    @Override
    public void setInlineComment(String path, String comment) {
        if (!isRoot()) {
            super.setInlineComment(path, comment);
            return;
        }
        if (comment == null) {
            comment = "";
        }
        Scalar currentNode = getScalar(path);
        if (currentNode == null) {
            return;
        }
        String[] paths = toPathArray(path);
        YamlMapping mapping = Yaml.createYamlMappingBuilder().add(paths[paths.length - 1], Yaml.createYamlScalarBuilder().addLine(currentNode.toString()).buildPlainScalar(currentNode.comment().value(), comment)).build();
        for (int i = paths.length - 2; i >= 0; i--) {
            mapping = Yaml.createYamlMappingBuilder().add(paths[i], mapping).build();
        }
        currentMapping = new MergedYamlMapping(currentMapping, mapping, true);
    }

}
