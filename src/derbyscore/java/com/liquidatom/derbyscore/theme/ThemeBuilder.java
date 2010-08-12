package com.liquidatom.derbyscore.theme;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.imageio.ImageIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: ThemeBuilder.java 4 2010-03-12 11:40:07Z russ $
 */
@ThreadSafe
@Immutable
public class ThemeBuilder {

    static private final Color DEFAULT_FONT_COLOR = new Color(0, 255, 0);

    /**
     * Read the input stream which contains the theme.xml definition for the theme.
     * 
     * @return A newly constructed Theme instance.
     */
    public Theme buildTheme(final URL themeDefinitionURL) throws SAXException, IOException, FontFormatException {
        Theme theme = new Theme(themeDefinitionURL);
        InputStream themeStream = themeDefinitionURL.openStream();
        try {
            InputSource themeSource = new InputSource(themeStream);
            DOMParser parser = new DOMParser();
            parser.parse(themeSource);
            Document doc = parser.getDocument();

            loadResources(doc, theme);
            loadLayout(doc, theme);

            return theme;
        }
        finally {
            themeStream.close();
        }
    }

    private void loadResources(final Document doc, final Theme theme) throws IOException, FontFormatException {
        NodeList nodes = doc.getElementsByTagName("resources");
        if (nodes.getLength() != 1) {
            throw new IllegalStateException("May only define one resources section in the theme.xml definition file.");
        }
        Node resources = nodes.item(0);
        nodes = resources.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node resource = nodes.item(i);
            if (resource instanceof Element) {
                Element element = (Element)resource;
                if ("image".equals(element.getTagName())) {
                    String id = element.getAttribute("id");
                    String src = element.getAttribute("src");
                    String transparency = element.getAttribute("transparency").toLowerCase();
                    int transparencyValue = Transparency.OPAQUE;
                    if ("translucent".equals(transparency)) {
                        transparencyValue = Transparency.TRANSLUCENT;
                    }
                    else if ("bitmask".equals(transparency)) {
                        transparencyValue = Transparency.BITMASK;
                    }
                    BufferedImage image = loadImage(new URL(theme.getBase() + src));
                    theme.addResource(id, image, transparencyValue);
                }
                else if ("font".equals(element.getTagName())) {
                    String id = element.getAttribute("id");
                    String src = element.getAttribute("src");
                    Font font = loadFont(new URL(theme.getBase() + src));
                    theme.addResource(id, font);
                }
            }
        }
    }

    private void loadLayout(final Document doc, final Theme theme) {
        NodeList nodes = doc.getElementsByTagName("layout");
        if (nodes.getLength() != 1) {
            throw new IllegalStateException("May only define one layout section within the definition.");
        }
        Node resources = nodes.item(0);
        nodes = resources.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element element = (Element)node;
                if ("text".equals(element.getTagName())) {
                    Rectangle position = readPosition(element);
                    Element fontElement = (Element)element.getElementsByTagName("font").item(0);
                    String fontRef = fontElement.getAttribute("ref");
                    String fontColor = fontElement.getAttribute("color");

                    Element value = (Element)element.getElementsByTagName("value").item(0);
                    String valueString = value.getTextContent();

                    String visibleCondition = null;
                    NodeList nodeList = element.getElementsByTagName("condition");
                    if (nodeList.getLength() == 1) {
                        Element conditionElement = (Element) nodeList.item(0);
                        visibleCondition = conditionElement.getTextContent();
                    }

                    theme.addElement(
                        new TextElement(theme, visibleCondition, position, valueString, fontRef, parseColor(fontColor)));
                }
                else if ("image".equals(element.getTagName())) {
                    Rectangle position = readPosition(element);
                    String imgRef = element.getAttribute("ref");
                    String conditionJs = null;
                    NodeList nodeList = element.getElementsByTagName("condition");
                    if (nodeList.getLength() == 1) {
                        Element conditionElement = (Element) nodeList.item(0);
                        conditionJs = conditionElement.getTextContent();
                    }

                    theme.addElement(new ImageElement(theme, conditionJs, position, imgRef));
                }
            }
        }
    }

    private Rectangle readPosition(final Element element) {
        int x = Integer.parseInt(element.getAttribute("x"));
        int y = Integer.parseInt(element.getAttribute("y"));

        int width = 0;
        try {
            width = Integer.parseInt(element.getAttribute("width"));
        }
        catch (NumberFormatException e) {
        }

        int height = 0;
        try {
            height = Integer.parseInt(element.getAttribute("height"));
        }
        catch (NumberFormatException e) {
        }

        return new Rectangle(x, y, width, height);
    }
    
    private BufferedImage loadImage(final URL imageURL) throws IOException {
        InputStream ins = imageURL.openStream();
        try {
            return ImageIO.read(ins);
        }
        finally {
            ins.close();
        }
    }

    private Font loadFont(final URL fontURL) throws FontFormatException, IOException {
        InputStream ins = fontURL.openStream();
        try {
            return Font.createFont(Font.TRUETYPE_FONT, ins);
        }
        finally {
            ins.close();
        }
    }

    private Color parseColor(String color) {
        Color result = null;
        if (color != null) {
            color = color.trim();
            if (color.startsWith("#")) {
                color = color.substring(1);
            }
            if (color.length() == 6) {
                int red = Integer.parseInt(color.substring(0, 2), 16);
                int green = Integer.parseInt(color.substring(2, 4), 16);
                int blue = Integer.parseInt(color.substring(4, 6), 16);
                result = new Color(red, green, blue);
            }
        }

        return result == null ? DEFAULT_FONT_COLOR : result;
    }
}
