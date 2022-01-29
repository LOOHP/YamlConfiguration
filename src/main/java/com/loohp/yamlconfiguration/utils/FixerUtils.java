package com.loohp.yamlconfiguration.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixerUtils {

    public static final Pattern QUOTE_PATTERN = Pattern.compile("^( *(?:(?:- +)|(?:[^:]*: +)))(.*)$");
    public static final Pattern QUOTE_STRING_PATTERN = Pattern.compile("^(.*?)( #.*)?$");

    public static File fixYaml(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(fixYaml(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                pw.println(line);
            }
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static InputStream fixYaml(InputStream input) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(fixYaml(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            for (String line : lines) {
                pw.println(line);
            }
            pw.flush();
        }
        return new ByteArrayInputStream(output.toByteArray());
    }

    public static String fixYaml(String str) {
        Matcher matcher = QUOTE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String part1 = matcher.group(1);
            String part2 = matcher.group(2);
            if (!part2.startsWith("\"") && !part2.startsWith("'") && !part2.startsWith("[")) {
                part2 = quoteString(part2);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(part1 + part2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String quoteString(String str) {
        Matcher matcher = QUOTE_STRING_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String part1 = "\"" + matcher.group(1).replaceAll("[\\\\\\'\\\"](?!(?:u[0-9a-fA-F]{4})|(?:[ertpafv0]))", "\\\\$0") + "\"";
            String part2 = matcher.group(2);
            if (part2 == null) {
                part2 = "";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(part1 + part2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
