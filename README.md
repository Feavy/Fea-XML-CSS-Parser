# Fea-XML-Parser
Simple Java XML Parser with CSS support

## Examples

### Parsing a String

```java
String myString = "<div><text>Hello !</text></div>";
XMLElement rootDiv = new XMLParser().parse(myString);
```

### Parsing a File

```
File file = new File(getClass().getResource("myFile.xml").getFile());
try (FileInputStream fis = new FileInputStream(file)) {
    XMLElement root = XMLParser.parse(fis);
} catch (IOException e) {
    e.printStackTrace();
}
```

### Custom tag handling

```java
String myString = "<div><text id=\"my-text\">Hello !</text></div>";
XMLParser parser = new XMLParser().with("text", TextElement::new);
XMLElement rootDiv = parser.parse(myString);
TextElement textElement = (TextElement) parser.getElementById("my-text");
```