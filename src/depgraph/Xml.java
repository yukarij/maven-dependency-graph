package depgraph;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Secure namespace-aware helpers for direct POM children. */
public final class Xml {
    /** Prevents utility construction. */
    private Xml() { }

    /** Parses XML with DTDs and external resources disabled. */
    public static Element parse(byte[] content) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(content)).getDocumentElement();
    }

    /** Returns direct element children, never nested plugin dependencies. */
    public static List<Element> children(Element parent, String name) {
        List<Element> result = new ArrayList<>();
        if (parent == null) {
            return result;
        }
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && element.getLocalName().equals(name)) {
                result.add(element);
            }
        }
        return result;
    }

    /** Returns an optional direct child. */
    public static Element child(Element parent, String name) {
        List<Element> elements = children(parent, name);
        return elements.isEmpty() ? null : elements.get(0);
    }

    /** Returns trimmed text or an empty string for an absent child. */
    public static String text(Element parent, String name) {
        Element element = child(parent, name);
        return element == null ? "" : element.getTextContent().trim();
    }
}
