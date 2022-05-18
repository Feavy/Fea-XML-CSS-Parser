package fr.feavy.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StyleElement extends XMLElement {
    private List<CSSRule> rules = new ArrayList<>();

    StyleElement(Map<String, String> attributes) {
        super("style", attributes);
    }

    public StyleElement() {
        super("style", new HashMap<>());
    }

    public List<CSSRule> getRules() {
        return rules;
    }

    public void setRules(List<CSSRule> rules) {
        this.rules = rules;
    }

    public CSSRule getRule(int i) {
        return rules.get(i);
    }

    public void addRule(CSSRule rule) {
        rules.add(rule);
    }

    public void removeRule(CSSRule rule) {
        rules.remove(rule);
    }

    public void removeRule(int index) {
        rules.remove(index);
    }

    @Override
    public String getContent() {
        StringBuilder builder = new StringBuilder();

        for (CSSRule rule : rules) {
            builder.append(rule.toString(0));
        }

        return builder.toString();
    }

    @Override
    public void setContent(String text) {
        super.setContent(null);
        this.rules = CSSRule.fromString(text);
    }

    public String toString(int indent) {
        StringBuilder builder = new StringBuilder();
        builder.append(spaces(indent)).append("<style").append(getAttributesAsString()).append(">\n");
        for (CSSRule rule : rules) {
            builder.append(rule.toString(indent + 2));
        }
        builder.append(spaces(indent)).append("</style>\n");
        return builder.toString();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public StyleElement clone() {
        StyleElement rep = new StyleElement(new HashMap<>(this.attributes));
        rep.setContent(this.getContent());
        List<CSSRule> rules = this.rules.stream().map(CSSRule::clone).collect(Collectors.toList());
        rep.setRules(rules);
        return rep;
    }
}
