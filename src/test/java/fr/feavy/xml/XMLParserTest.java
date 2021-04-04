package fr.feavy.xml;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class XMLParserTest {

    @Test
    public void fileParsingIsCorrect() {
        File file = new File(getClass().getResource("/fileParsingIsCorrect.xml").getFile());
        try (FileInputStream fis = new FileInputStream(file)) {
            XMLElement root = XMLParser.parse(fis);
            assertEquals("div", root.getTagName());
            assertEquals(1, root.getChilds().size());
            assertEquals("Hello !", root.getChild(0).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void simpleParsingIsCorrect() {
        String input = "<div><text>Hello !</text></div>";
        XMLElement root = XMLParser.parse(input);
        assertEquals("div", root.getTagName());
        assertEquals(1, root.getChilds().size());
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
        XMLElement root = XMLParser.parse(input);
        List<XMLElement> childs = root.getChilds();

        List<XMLElement> test2Childs = root.getElementsByClassName("test2");
        List<XMLElement> testChilds = root.getElementsByClassName("test");

        // Then
        assertEquals(6, childs.size());

        assertEquals(4, test2Childs.size());
        assertEquals("test2", test2Childs.get(0).getContent());
        assertEquals("test test2 test3", test2Childs.get(1).getContent());
        assertEquals("test test2", test2Childs.get(2).getContent());
        assertEquals("test2!!", test2Childs.get(3).getContent());

        assertEquals(3, testChilds.size());
        assertEquals("test test2 test3", testChilds.get(0).getContent());
        assertEquals("test test2", testChilds.get(1).getContent());
        assertEquals("test", testChilds.get(2).getContent());
    }
}
