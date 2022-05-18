package fr.feavy.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
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
    private final Map<String, Function<Map<String, String>, ? extends XMLElement>> elementFactories = new HashMap<>();
    public XMLParser() {
        with("style", StyleElement::new);
    }

    public XMLParser with(String tag, Function<Map<String, String>, ? extends XMLElement> factory) {
        this.elementFactories.put(tag.toLowerCase(), factory);
        return this;
    }

    public XMLParser set(String tag, Function<Map<String, String>, ? extends XMLElement> factory) {
        return with(tag, factory);
    }



    private static String inputStreamToString(InputStream inputStream) {
        StringBuilder strBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line).append("\n");
            }
        } catch (IOException ignored) { }
        return strBuilder.toString();
    }

    public XMLElement parse(InputStream inputStream) {
        return parse(inputStreamToString(inputStream));
    }

    public XMLElement parse(String fileContent) {
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

    private XMLElement newElement(String tag, Map<String, String> properties) {
        tag = tag.toLowerCase();
        Function<Map<String, String>, ? extends XMLElement> factory = elementFactories.get(tag);
        if(factory == null) {
            return new XMLElement(tag, properties);
        }
        return factory.apply(properties);
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
                    currentElement = newElement(tag, properties);
                } else {
                    XMLElement parent = currentElement;
                    XMLElement child = newElement(tag, properties);
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
                if(index-1 >= 0 && line.charAt(index-1) == '/') {
                    index--;
                }
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
