package fr.feavy.xml;

import java.util.*;

import static fr.feavy.xml.XMLElement.spaces;

public class CSSRule {
    private Set<String> selectors;
    private Map<String, String> properties;

    public CSSRule() {
        this.selectors = new HashSet<>();
        this.properties = new HashMap<>();
    }

    public CSSRule(Set<String> selectors, Map<String, String> properties) {
        this.selectors = selectors;
        this.properties = properties;
    }

    public static List<CSSRule> fromString(String text) {
        List<CSSRule> rep = new ArrayList<>();

        text = text.replaceAll("\\s+", "");

        String[] declarations = text.split("}");

        for (String declaration : declarations) {
            Set<String> selectors = new HashSet<>();
            Map<String, String> properties = new HashMap<>();
            CSSRule current = new CSSRule(selectors, properties);

            String[] strings = declaration.split("\\{");
            
            if(strings.length < 2) {
                continue;
            }

            String[] selectorsStr = strings[0].split(",");
            String[] propertiesStr = strings[1].split(";");

            selectors.addAll(Arrays.asList(selectorsStr));

            for (String property : propertiesStr) {
                String[] parts = property.split(":");
                properties.put(parts[0], parts[1]);
            }

            rep.add(current);
        }
        return rep;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public boolean hasStyle(String style) {
        return hasProperty(style);
    }

    public String getPropertyValue(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public Set<String> getSelectors() {
        return selectors;
    }

    public void addSelector(String selector) {
        this.selectors.add(selector);
    }

    public boolean hasSelector(String selector) {
        return selectors.contains(selector);
    }

    public boolean hasClass(String clazz) {
        return selectors.contains("." + clazz);
    }

    public String toString(int indent) {
        StringBuilder rep = new StringBuilder();
        rep.append(spaces(indent)).append(String.join(", ", selectors)).append(" {").append("\n");
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            rep.append(spaces(indent + 2)).append(entry.getKey()).append(": ").append(entry.getValue()).append(";\n");
        }
        rep.append(spaces(indent)).append("}\n");
        return rep.toString();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public CSSRule clone() {
        return new CSSRule(new HashSet<>(this.selectors), new HashMap<>(this.properties));
    }
}
