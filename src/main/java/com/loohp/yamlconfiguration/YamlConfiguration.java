package com.loohp.yamlconfiguration;

import com.amihaiemil.eoyaml.Yaml;

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

    public YamlConfiguration(File file, boolean guessIndentation) throws IOException {
        super(Yaml.createYamlInput(file, StandardCharsets.UTF_8, guessIndentation).readYamlMapping());
        this.file = file;
    }

    public YamlConfiguration(File file) throws IOException {
        this(file, true);
    }

    public YamlConfiguration(InputStream inputStream, boolean guessIndentation) throws IOException {
        super(Yaml.createYamlInput(inputStream, StandardCharsets.UTF_8, guessIndentation).readYamlMapping());
        this.file = null;
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
            currentMapping = Yaml.createYamlInput(file).readYamlMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
