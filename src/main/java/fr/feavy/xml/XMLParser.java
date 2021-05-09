package fr.feavy.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLParser {
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("(?<key>[:a-zA-Z0-9_-]+)=\"(?<value>[^\"]*)\"");
    private ListIterator<String> iterator;
    private String tag = null;
    private Map<String, String> properties = new HashMap<>();
    private boolean isEndTag = false;
    private boolean isStartAndEndTag = false;
    private StringBuilder currentText;
    private XMLElement currentElement;
    private XMLParser() {

    }

    public static XMLElement parse(InputStream inputStream) {
        return new XMLParser()._parse(inputStream);
    }

    public static XMLElement parse(String data) {
        return new XMLParser()._parse(data);
    }

    private static String inputStreamToString(InputStream inputStream) {
        StringBuilder strBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
        }
        return strBuilder.toString();
    }

    private XMLElement _parse(InputStream inputStream) {
        return _parse(inputStreamToString(inputStream));
    }

    private XMLElement _parse(String fileContent) {
        fileContent = fileContent.replaceAll(">(.*)<", ">\n$1\n<")
                .replaceAll(">(.)", ">\n$1")
                .replaceAll("(.)<", "$1\n<");
        String[] lines = fileContent.split("\n");
        for(int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim();
        }
        iterator = Arrays.asList(lines).listIterator();
        return parseElement();
    }

    private XMLElement parseElement() {
        if (!iterator.hasNext()) {
            return currentElement;
        }
        readTag();
        if (tag != null) {
            if (!isEndTag) {
                currentText = new StringBuilder();
                if (currentElement == null) {
                    currentElement = tag.equals("style") ? new StyleElement(properties) : new XMLElement(tag, properties);
                } else {
                    XMLElement parent = currentElement;
                    XMLElement child = tag.equals("style") ? new StyleElement(properties) : new XMLElement(tag, properties);
                    if (!isStartAndEndTag) {
                        currentElement = child;
                    }
                    parent.addChild(child);
                }
            } else {
                if (currentText != null) {
                    String text = currentText.toString();
                    if (text.length() > 0) {
                        currentElement.setContent(text);
                    }
                }
                if (currentElement.parent() == null) {
                    return currentElement;
                }
                currentElement = currentElement.parent();
                currentText = new StringBuilder();
            }
        } else {
            String text = iterator.next();
            if (text.length() > 0) {
                if(currentText.length() > 0) {
                    currentText.append("\n");
                }
                currentText.append(text);
            }
        }
        return parseElement();
    }

    private void readTag() {
        tag = null;

        String line = iterator.next();
        if (line.length() > 0 && line.charAt(0) == '<') {
            StringBuilder allLine = new StringBuilder(line);
            while (line.charAt(line.length() - 1) != '>') {
                line = iterator.next();
                allLine.append(" ").append(line);
            }
            line = allLine.toString();
            // New tag
            int index = line.indexOf(" ");
            if (index < 0) {
                index = line.indexOf(">");
            }
            if (index < 0) {
                index = line.indexOf("/");
            }
            if (index < 0) {
                index = line.indexOf("\n");
            }
            tag = line.substring(1, index);
            isEndTag = tag.charAt(0) == '/';
            isStartAndEndTag = !isEndTag && line.charAt(line.length() - 2) == '/';
            if (isEndTag) {
                tag = tag.substring(1);
            }
            if (!isEndTag) {
                properties = new HashMap<>();
                Matcher matcher = ATTRIBUTE_PATTERN.matcher(line);
                while (matcher.find()) {
                    properties.put(matcher.group("key"), matcher.group("value"));
                }
            }
        } else {
            iterator.previous();
        }
    }
}
