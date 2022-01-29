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
import com.loohp.yamlconfiguration.utils.UnicodeUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class RootConfigurationSection extends ConfigurationSection {

    private Map<String, WeakReference<ConfigurationSection>> createdSubsections;
    private ReentrantLock lock;

    protected RootConfigurationSection(YamlMapping rootMapping) {
        super(null, "", rootMapping);
        this.root = this;
        this.createdSubsections = new HashMap<>();
        this.lock = new ReentrantLock(true);
    }

    @Override
    public boolean isRoot() {
        return root == this;
    }

    @Override
    public void set(String path, Object value) {
        if (!isRoot()) {
            super.set(path, value);
            return;
        }
        lock.lock();
        if (value == null) {
            String[] paths = toPathArray(path);
            if (paths.length < 1) {
                currentMapping = Yaml.createYamlMappingBuilder().build();
            } else if (paths.length < 2) {
                YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
                for (YamlNode key : currentMapping.keys()) {
                    if (!key.asScalar().value().equals(paths[paths.length - 1])) {
                        builder = builder.add(key, currentMapping.value(key));
                    }
                }
                currentMapping = builder.build();
            } else {
                LinkedList<String> pathSections = new LinkedList<>(Arrays.asList(paths));
                String sectionKey = pathSections.removeLast();
                YamlMapping section = getMapping(fromPathList(pathSections));
                if (section != null) {
                    YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
                    for (YamlNode key : section.keys()) {
                        if (!key.asScalar().value().equals(sectionKey)) {
                            builder = builder.add(key, section.value(key));
                        }
                    }
                    YamlMapping mapping = builder.build(section.comment().value());
                    for (int i = paths.length - 3; i >= 0; i--) {
                        sectionKey = pathSections.removeLast();
                        builder = Yaml.createYamlMappingBuilder();
                        section = getMapping(fromPathList(pathSections));
                        for (YamlNode key : section.keys()) {
                            if (key.asScalar().value().equals(sectionKey)) {
                                builder = builder.add(key, mapping);
                            } else {
                                builder = builder.add(key, section.value(key));
                            }
                        }
                        mapping = Yaml.createYamlMappingBuilder().add(paths[i], builder.build(section.comment().value())).build();
                    }
                    sectionKey = pathSections.removeLast();
                    builder = Yaml.createYamlMappingBuilder();
                    for (YamlNode key : currentMapping.keys()) {
                        if (key.asScalar().value().equals(sectionKey)) {
                            builder = builder.add(key, mapping.value(key));
                        } else {
                            builder = builder.add(key, currentMapping.value(key));
                        }
                    }
                    currentMapping = builder.build(currentMapping.comment().value());
                }
            }
        } else {
            YamlNode newNode;
            if (value instanceof String) {
                value = UnicodeUtils.escape((String) value);
            }
            String comment = "";
            YamlNode currentNode = getNode(path);
            if (currentNode != null) {
                comment = currentNode.comment().value();
            }
            if (value instanceof Collection) {
                newNode = createSequence((Collection<?>) value).build(comment);
            } else if (value instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) value;
                if (section == this) {
                    lock.unlock();
                    throw new IllegalStateException("ConfigurationSection cannot be nested into self");
                }
                if (section.fromExistingYaml()) {
                    lock.unlock();
                    throw new IllegalStateException("ConfigurationSection from an existing Yaml cannot be set into another Yaml");
                }
                YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
                YamlMapping mapping = section.currentMapping;
                for (YamlNode key : mapping.keys()) {
                    builder = builder.add(key, mapping.value(key));
                }
                newNode = builder.build(comment);
                section.toSubsection(this, path);
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
        remapSubsections();
        lock.unlock();
    }

    @Override
    public void setAboveComment(String path, String comment) {
        if (!isRoot()) {
            super.setAboveComment(path, comment);
            return;
        }
        if (comment == null) {
            comment = "";
        } else {
            comment = UnicodeUtils.escape(comment);
        }
        lock.lock();
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
        remapSubsections();
        lock.unlock();
    }

    @Override
    public void setInlineComment(String path, String comment) {
        if (!isRoot()) {
            super.setInlineComment(path, comment);
            return;
        }
        if (comment == null) {
            comment = "";
        } else {
            comment = UnicodeUtils.escape(comment);
        }
        lock.lock();
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
        remapSubsections();
        lock.unlock();
    }

    @Override
    protected void toSubsection(RootConfigurationSection root, String currentPath) {
        if (!isRoot()) {
            super.toSubsection(root, currentPath);
            return;
        }
        lock.lock();
        this.root = root;
        this.currentPath = currentPath;
        for (Entry<String, WeakReference<ConfigurationSection>> entry : this.createdSubsections.entrySet()) {
            String fullPath = toFullPath(entry.getKey());
            WeakReference<ConfigurationSection> reference = entry.getValue();
            ConfigurationSection subsection = reference.get();
            if (subsection != null) {
                subsection.currentPath = fullPath;
                root.createdSubsections.put(fullPath, reference);
            }
        }
        this.createdSubsections = null;
        lock.unlock();
        this.lock = null;
    }

    protected ConfigurationSection createOrGetSubsection(String path, YamlMapping mapping) {
        if (!isRoot()) {
            throw new IllegalStateException("This RootConfigurationSection is no longer root.");
        }
        if (path.isEmpty()) {
            return this;
        }
        lock.lock();
        WeakReference<ConfigurationSection> reference = createdSubsections.get(path);
        ConfigurationSection section;
        if (reference == null || (section = reference.get()) == null) {
            createdSubsections.put(path, new WeakReference<>(section = new ConfigurationSection(this, path, mapping)));
        }
        lock.unlock();
        return section;
    }

    protected void remapSubsections() {
        lock.lock();
        Iterator<Entry<String, WeakReference<ConfigurationSection>>> itr = createdSubsections.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, WeakReference<ConfigurationSection>> entry = itr.next();
            ConfigurationSection section = entry.getValue().get();
            if (section == null) {
                itr.remove();
            } else {
                section.currentMapping = getMapping(entry.getKey());
            }
        }
        lock.unlock();
    }

}
