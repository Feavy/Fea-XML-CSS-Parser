package fr.feavy.xml;

import java.util.*;
import java.util.function.Consumer;

public class XMLElement {
    protected final Map<String, String> attributes;
    private final String tagName;
    private List<XMLElement> childs = new ArrayList<>();
    private XMLElement parent;
    private String content = null;

    XMLElement(String tagName, Map<String, String> attributes) {
        this.tagName = tagName;
        this.attributes = attributes;
    }

    static String spaces(int amount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
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

    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    public String getTagName() {
        return this.tagName;
    }

    public List<XMLElement> getChilds() {
        return this.childs;
    }

    public XMLElement getChild(int index) {
        return this.childs.get(index);
    }

    public void removeChilds() {
        this.childs.clear();
    }

    public void removeChild(int index) {
        this.childs.remove(index);
    }

    public void removeChild(XMLElement child) {
        this.childs.remove(child);
    }

    public void addChild(XMLElement child) {
        this.childs.add(child);
        child.parent = this;
    }

    public void addChilds(Collection<XMLElement> childs) {
        for (XMLElement child : childs) {
            addChild(child);
        }
    }

    public boolean hasChilds() {
        return !childs.isEmpty();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder builder = new StringBuilder();
        if (content == null && childs.size() == 0) {
            return builder.append(spaces(indent)).append("<").append(tagName).append(getAttributesAsString()).append(" />\n").toString();
        }
        builder.append(spaces(indent)).append("<").append(tagName).append(getAttributesAsString()).append(">");
        if (content != null) {
            builder.append("\n").append(content);
        } else {
            builder.append("\n");
            for (XMLElement element : childs) {
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
        for (XMLElement child : childs) {
            if ((element = child.getElementById(id)) != null) {
                return element;
            }
        }
        return null;
    }

    public List<XMLElement> getElementsByTagName(String tagName) {
        List<XMLElement> rep = new ArrayList<>();
        for (XMLElement child : childs) {
            if (child.tagName.equals(tagName)) {
                rep.add(child);
            }
            rep.addAll(child.getElementsByTagName(tagName));
        }
        return rep;
    }

    public List<XMLElement> getElementsByClassName(String clazz) {
        List<XMLElement> rep = new ArrayList<>();
        for (XMLElement child : childs) {
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
        for (XMLElement child : childs) {
            rep.addChild(child.clone());
        }
        return rep;
    }

    public void visitDeep(Consumer<XMLElement> consumer) {
        for (XMLElement child : childs) {
            consumer.accept(child);
            child.visitDeep(consumer);
        }
    }
}