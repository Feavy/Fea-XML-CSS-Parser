package fr.feavy.xml;

import java.util.*;
import java.util.function.Consumer;

public class XMLElement implements Cloneable {
    protected final Map<String, String> attributes;
    private final String tagName;
    private final List<XMLElement> children = new ArrayList<>();
    private XMLElement parent;
    private String content = null;

    public XMLElement(String tagName, Map<String, String> attributes) {
        this.tagName = tagName;
        this.attributes = attributes;
    }

    public XMLElement(String tagName) {
        this(tagName, new HashMap<>());
    }

    static String spaces(int amount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    public boolean hasAttribute(String name) {
        return getAttribute(name) != null;
    }

    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public String getAttribute(String key) {
        String value = attributes.get(key);
        if (value == null && hasParent()) {
            value = parent.getAttribute(key);
        }
        return value;
    }

    private String getAttributesAsString() {
        StringBuilder attributes = new StringBuilder();
        if (this.attributes.size() > 0) {
            attributes.append(" ");
        }
        Iterator<Map.Entry<String, String>> iterator = this.attributes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> attribute = iterator.next();
            attributes.append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
            if (iterator.hasNext()) {
                attributes.append(" ");
            }
        }
        return attributes.toString();
    }

    public String getId() {
        return getAttribute("id");
    }

    public String getClassAsString() {
        String attribute = getAttribute("class");
        if (attribute == null) {
            return "";
        }
        return attribute;
    }

    public Set<String> getClasses() {
        String attribute = getAttribute("class");
        if (attribute == null) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(attribute.split(" ")));
    }

    public void setClass(String clazz) {
        this.setAttribute("class", clazz);
    }

    public void addClass(String clazz) {
        Set<String> classList = getClasses();
        classList.add(clazz);
        setAttribute("class", String.join(" ", classList));
    }

    public void removeClass(String clazz) {
        Set<String> classList = getClasses();
        if (classList.isEmpty()) {
            return;
        }
        classList.remove(clazz);
        setAttribute("class", String.join(" ", classList));
    }

    public String getTagName() {
        return this.tagName;
    }

    public List<XMLElement> getChildren() {
        return this.children;
    }

    public XMLElement getChild(int index) {
        return this.children.get(index);
    }

    public void removeChildren() {
        this.children.clear();
    }

    public void removeChild(int index) {
        this.children.remove(index);
    }

    public void removeChild(XMLElement child) {
        this.children.remove(child);
    }

    public void addChild(XMLElement child) {
        this.children.add(child);
        child.parent = this;
    }

    public void addChildren(Collection<XMLElement> childs) {
        for (XMLElement child : childs) {
            addChild(child);
        }
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder builder = new StringBuilder();
        if (content == null && children.size() == 0) {
            return builder.append(spaces(indent)).append("<").append(tagName).append(getAttributesAsString()).append(" />\n").toString();
        }
        builder.append(spaces(indent)).append("<").append(tagName).append(getAttributesAsString()).append(">");
        if (content != null && content.trim().length() > 0) {
            builder.append("\n").append(content);
        } else {
            builder.append("\n");
            for (XMLElement element : children) {
                builder.append(element.toString(indent + 2));
            }
            builder.append(spaces(indent));
        }
        builder.append("</").append(tagName).append(">\n");
        return builder.toString();
    }

    public XMLElement getElementById(String id) {
        if (id.equals(this.getId())) {
            return this;
        }
        XMLElement element;
        for (XMLElement child : children) {
            if ((element = child.getElementById(id)) != null) {
                return element;
            }
        }
        return null;
    }

    public List<XMLElement> getElementsByTagName(String tagName) {
        List<XMLElement> rep = new ArrayList<>();
        for (XMLElement child : children) {
            if (child.tagName.equals(tagName)) {
                rep.add(child);
            }
            rep.addAll(child.getElementsByTagName(tagName));
        }
        return rep;
    }

    public List<XMLElement> getElementsByClassName(String clazz) {
        List<XMLElement> rep = new ArrayList<>();
        for (XMLElement child : children) {
            if (child.getClasses().contains(clazz)) {
                rep.add(child);
            }
            rep.addAll(child.getElementsByTagName(tagName));
        }
        return rep;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public XMLElement parent() {
        return this.parent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public XMLElement clone() {
        XMLElement rep = new XMLElement(this.tagName, new HashMap<>(this.attributes));
        rep.setContent(this.getContent());
        for (XMLElement child : children) {
            rep.addChild(child.clone());
        }
        return rep;
    }

    public void visitDeep(Consumer<XMLElement> consumer) {
        consumer.accept(this);
        for (XMLElement child : children) {
            consumer.accept(child);
            child.visitDeep(consumer);
        }
    }

}
