/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.axiom;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AxiomHandlerTest {

    private static final String XML_1 =
            "<?xml version='1.0' encoding='UTF-8'?>" + "<?pi content?>" + "<root xmlns='namespace'>" +
                    "<prefix:child xmlns:prefix='namespace2' xmlns:prefix2='namespace3' prefix2:attr='value'>content</prefix:child>" +
                    "</root>";

    private static final String XML_2_EXPECTED =
            "<?xml version='1.0' encoding='UTF-8'?>" + "<root xmlns='namespace'>" + "<child xmlns='namespace2' />" +
                    "</root>";

    private static final String XML_2_SNIPPET =
            "<?xml version='1.0' encoding='UTF-8'?>" + "<child xmlns='namespace2' />";

    private static final String XML_3_ENTITY =
            "<predefined-entity-reference>&lt;&gt;&amp;&quot;&apos;</predefined-entity-reference>";

    private static final String XML_4_SNIPPET = "<?xml version='1.0' encoding='UTF-8'?>" + "<child xmlns='namespace1' />";
    
    private static final String XML_5_SNIPPET = "<?xml version='1.0' encoding='UTF-8'?>" + "<x:child xmlns:x='namespace1' />";

    private AxiomHandler handler;

    private OMDocument result;

    private XMLReader xmlReader;

    private OMFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = OMAbstractFactory.getOMFactory();
        result = factory.createOMDocument();
        xmlReader = XMLReaderFactory.createXMLReader();
    }

    @Test
    public void testContentHandlerDocumentNamespacePrefixes() throws Exception {
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        handler = new AxiomHandler(result, factory);
        xmlReader.setContentHandler(handler);
        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        xmlReader.parse(new InputSource(new StringReader(XML_1)));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.serialize(bos);
        assertXMLEqual("Invalid result", XML_1, bos.toString("UTF-8"));
    }

    @Test
    public void testContentHandlerDocumentNoNamespacePrefixes() throws Exception {
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        handler = new AxiomHandler(result, factory);
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(XML_1)));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.serialize(bos);
        assertXMLEqual("Invalid result", XML_1, bos.toString("UTF-8"));
    }

    @Test
    public void testContentHandlerElement() throws Exception {
        OMNamespace namespace = factory.createOMNamespace("namespace", "");
        OMElement rootElement = factory.createOMElement("root", namespace, result);
        handler = new AxiomHandler(rootElement, factory);
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(XML_2_SNIPPET)));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.serialize(bos);
        assertXMLEqual("Invalid result", XML_2_EXPECTED, bos.toString("UTF-8"));
    }
    
    @Test
    public void testContentHandlerElementWithSamePrefixAndDifferentNamespace() throws Exception {
        OMNamespace namespace = factory.createOMNamespace("namespace1", "");
        OMElement rootElement = factory.createOMElement("root", namespace, result);
        handler = new AxiomHandler(rootElement, factory);
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(XML_2_SNIPPET)));
        Iterator<?> it = result.getOMDocumentElement().getChildrenWithLocalName("child");
        assertTrue(it.hasNext());
        OMElement child = (OMElement) it.next();
        assertEquals("", child.getQName().getPrefix());
        assertEquals("namespace2", child.getQName().getNamespaceURI());
    }

    @Test
    public void testContentHandlerElementWithSameNamespacesAndPrefix() throws Exception {
        OMNamespace namespace = factory.createOMNamespace("namespace1", "");
        OMElement rootElement = factory.createOMElement("root", namespace, result);
        handler = new AxiomHandler(rootElement, factory);
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(XML_4_SNIPPET)));
        Iterator<?> it = result.getOMDocumentElement().getChildrenWithLocalName("child");
        assertTrue(it.hasNext());
        OMElement child = (OMElement) it.next();
        assertEquals("", child.getQName().getPrefix());
        assertEquals("namespace1", child.getQName().getNamespaceURI());
    }

    @Test
    public void testContentHandlerElementWithSameNamespacesAndDifferentPrefix() throws Exception {
        OMNamespace namespace = factory.createOMNamespace("namespace1", "");
        OMElement rootElement = factory.createOMElement("root", namespace, result);
        handler = new AxiomHandler(rootElement, factory);
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(XML_5_SNIPPET)));
        Iterator<?> it = result.getOMDocumentElement().getChildrenWithLocalName("child");
        assertTrue(it.hasNext());
        OMElement child = (OMElement) it.next();
        assertEquals("x", child.getQName().getPrefix());
        assertEquals("namespace1", child.getQName().getNamespaceURI());
    }

    @Test
    public void testContentHandlerPredefinedEntityReference() throws Exception {
        handler = new AxiomHandler(result, factory);
        xmlReader.setContentHandler(handler);
        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        xmlReader.parse(new InputSource(new StringReader(XML_3_ENTITY)));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.serialize(bos);
        assertXMLEqual("Invalid result", XML_3_ENTITY, bos.toString("UTF-8"));
    }
}
