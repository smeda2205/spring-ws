/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap;

import java.util.Iterator;
import javax.xml.namespace.QName;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractSoapHeaderTestCase extends AbstractSoapElementTestCase {

    protected SoapHeader soapHeader;

    protected final SoapElement createSoapElement() throws Exception {
        soapHeader = createSoapHeader();
        return soapHeader;
    }

    protected abstract SoapHeader createSoapHeader() throws Exception;

    public void testAddHeaderElement() throws Exception {
        QName qName = new QName("http://www.springframework.org", "localName", "spring");
        SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
        assertNotNull("No SoapHeaderElement returned", headerElement);
        assertEquals("Invalid qName for element", qName, headerElement.getName());
        String payload = "<content xmlns='http://www.springframework.org'/>";
        transformer.transform(new StringSource(payload), headerElement.getResult());
        assertHeaderElementEqual(headerElement,
                "<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>");
    }

    public void testExamineAllHeaderElement() throws Exception {
        QName qName = new QName("http://www.springframework.org", "localName", "spring");
        SoapHeaderElement headerElement = soapHeader.addHeaderElement(qName);
        assertEquals("Invalid qName for element", qName, headerElement.getName());
        assertNotNull("No SoapHeaderElement returned", headerElement);
        String payload = "<content xmlns='http://www.springframework.org'/>";
        transformer.transform(new StringSource(payload), headerElement.getResult());
        Iterator iterator = soapHeader.examineAllHeaderElements();
        assertNotNull("header element iterator is null", iterator);
        assertTrue("header element iterator has no elements", iterator.hasNext());
        headerElement = (SoapHeaderElement) iterator.next();
        assertEquals("Invalid qName for element", qName, headerElement.getName());
        StringResult result = new StringResult();
        transformer.transform(headerElement.getSource(), result);
        assertXMLEqual("Invalid contents of header element",
                "<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>",
                result.toString());
        assertFalse("header element iterator has too many elements", iterator.hasNext());
    }

    public void testExamineMustUnderstandHeaderElements() throws Exception {
        QName qName1 = new QName("http://www.springframework.org", "localName1", "spring");
        SoapHeaderElement headerElement1 = soapHeader.addHeaderElement(qName1);
        headerElement1.setMustUnderstand(true);
        headerElement1.setActorOrRole("role1");
        QName qName2 = new QName("http://www.springframework.org", "localName2", "spring");
        SoapHeaderElement headerElement2 = soapHeader.addHeaderElement(qName2);
        headerElement2.setMustUnderstand(true);
        headerElement2.setActorOrRole("role2");
        Iterator iterator = soapHeader.examineMustUnderstandHeaderElements("role1");
        assertNotNull("header element iterator is null", iterator);
        assertTrue("header element iterator has no elements", iterator.hasNext());
        SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
        assertEquals("Invalid name on header element", qName1, headerElement.getName());
        assertTrue("MustUnderstand not set on header element", headerElement.getMustUnderstand());
        assertEquals("Invalid role on header element", "role1", headerElement.getActorOrRole());
        assertFalse("header element iterator has too many elements", iterator.hasNext());
    }

    public void testGetResult() throws Exception {
        String content =
                "<spring:localName xmlns:spring='http://www.springframework.org'><spring:content/></spring:localName>";
        transformer.transform(new StringSource(content), soapHeader.getResult());
        Iterator iterator = soapHeader.examineAllHeaderElements();
        assertTrue("Header has no children", iterator.hasNext());
        SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
        assertFalse("Header has too many children", iterator.hasNext());
        assertHeaderElementEqual(headerElement, content);
    }

    protected void assertHeaderElementEqual(SoapHeaderElement headerElement, String expected) throws Exception {
        StringResult result = new StringResult();
        transformer.transform(headerElement.getSource(), result);
        assertXMLEqual("Invalid contents of header element", expected, result.toString());
    }


}