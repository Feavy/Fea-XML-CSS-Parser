package fr.feavy.xml;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XMLParserTest {

    @Test
    public void fileParsingIsCorrect() {
        File file = new File(getClass().getResource("/fileParsingIsCorrect.xml").getFile());
        try (FileInputStream fis = new FileInputStream(file)) {
            XMLElement root = new XMLParser().parse(fis);
            assertEquals("div", root.getTagName());
            assertEquals(1, root.getChildren().size());
            assertEquals("Hello !", root.getChild(0).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void simpleParsingIsCorrect() {
        String input = "<div><text>Hello !</text></div>";
        XMLElement root = new XMLParser().parse(input);
        assertEquals("div", root.getTagName());
        assertEquals(1, root.getChildren().size());
        assertEquals("Hello !", root.getChild(0).getContent());
    }

    @Test
    public void getElementsByClassName() {
        // Given
        String input = "<div>" +
                            "<p class=\"test2\">test2</p>" +
                            "<p class=\"test test2 test3\">test test2 test3</p>" +
                            "<p></p>" +
                            "<p class=\"test test2\">test test2</p>" +
                            "<p class=\"test2\">test2!!</p>" +
                            "<p class=\"test\">test</p>" +
                      "</div>";

        // When
        XMLElement root = new XMLParser().parse(input);
        List<XMLElement> children = root.getChildren();

        List<XMLElement> test2Children = root.getElementsByClassName("test2");
        List<XMLElement> testChildren = root.getElementsByClassName("test");

        // Then
        assertEquals(6, children.size());

        assertEquals(4, test2Children.size());
        assertEquals("test2", test2Children.get(0).getContent());
        assertEquals("test test2 test3", test2Children.get(1).getContent());
        assertEquals("test test2", test2Children.get(2).getContent());
        assertEquals("test2!!", test2Children.get(3).getContent());

        assertEquals(3, testChildren.size());
        assertEquals("test test2 test3", testChildren.get(0).getContent());
        assertEquals("test test2", testChildren.get(1).getContent());
        assertEquals("test", testChildren.get(2).getContent());
    }

    @Test
    public void styleIsHandled() {
        // Given
        String input = "<html>\n" +
                "<style id=\"st\">\n" +
                ".red {\n" +
                "color: red;" +
                "}" +
                "</style>\n" +
                "</html>";

        // When
        XMLElement root = new XMLParser().parse(input);

        // Then
        assertEquals(StyleElement.class, root.getElementById("st").getClass());
        StyleElement style = (StyleElement) root.getElementById("st");
        assertEquals(1, style.getRules().size());
        CSSRule rule = style.getRule(0);
        assertEquals(".red", rule.getSelectors().iterator().next());
        assertEquals("red", rule.getProperties().get("color"));
    }

    @Test
    public void customFactoryWorks() {
        // Given
        String input = "<html>\n" +
                "<div id=\"dv\">\n" +
                "</div>\n" +
                "</html>";

        XMLParser parser = new XMLParser().with("div", DivElement::new);
        // When
        XMLElement root = parser.parse(input);
        // Then
        assertEquals(DivElement.class, root.getElementById("dv").getClass());
    }
}
