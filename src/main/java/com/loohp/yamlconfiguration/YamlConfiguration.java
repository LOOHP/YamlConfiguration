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

package com.loohp.yamlconfiguration;

import com.amihaiemil.eoyaml.Yaml;
import com.loohp.yamlconfiguration.utils.FixerUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class YamlConfiguration extends RootConfigurationSection {

    private File file;
    private boolean guessIndentation;
    private boolean fixValues;

    public YamlConfiguration(File file, boolean guessIndentation, boolean fixValues) throws IOException {
        super(Yaml.createYamlInput(fixValues ? FixerUtils.fixYaml(file) : file, StandardCharsets.UTF_8, guessIndentation).readYamlMapping());
        this.file = file;
        this.guessIndentation = guessIndentation;
        this.fixValues = fixValues;
    }

    public YamlConfiguration(File file, boolean guessIndentation) throws IOException {
        this(file, true, true);
    }

    public YamlConfiguration(File file) throws IOException {
        this(file, true);
    }

    public YamlConfiguration(InputStream inputStream, boolean guessIndentation, boolean fixValues) throws IOException {
        super(Yaml.createYamlInput(fixValues ? FixerUtils.fixYaml(inputStream) : inputStream, StandardCharsets.UTF_8, guessIndentation).readYamlMapping());
        this.file = null;
        this.guessIndentation = guessIndentation;
        this.fixValues = fixValues;
    }

    public YamlConfiguration(InputStream inputStream, boolean guessIndentation) throws IOException {
        this(inputStream, true, true);
    }

    public YamlConfiguration(InputStream inputStream) throws IOException {
        this(inputStream, true);
    }

    @Override
    public boolean fromExistingYaml() {
        return true;
    }

    public boolean hasFile() {
        return file != null;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String saveToString() {
        StringWriter writer = new StringWriter();
        try {
            save(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public void save(File file) {
        try {
            save(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (!hasFile()) {
            throw new UncheckedIOException(new FileNotFoundException("This YamlConfiguration is not created with a file"));
        }
        save(file);
    }

    public void save(OutputStream output) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            save(pw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(Writer writer) throws IOException {
        Yaml.createYamlPrinter(writer).print(currentMapping);
    }

    public void reload() {
        try {
            currentMapping = Yaml.createYamlInput(file, StandardCharsets.UTF_8, guessIndentation).readYamlMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
