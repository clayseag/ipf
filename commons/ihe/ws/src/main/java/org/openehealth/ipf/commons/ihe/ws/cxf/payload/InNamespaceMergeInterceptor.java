/*
 * Copyright 2009 the original author or authors.
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
package org.openehealth.ipf.commons.ihe.ws.cxf.payload;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.apache.cxf.interceptor.DocLiteralInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * CXF interceptor which inserts XML namespace declarations from incoming 
 * SOAP Envelope and SOAP Body elements into the String payload.
 * 
 * @author Dmytro Rud
 */
public class InNamespaceMergeInterceptor extends AbstractPhaseInterceptor<Message> {

    private final static Pattern XMLNS_PATTERN = Pattern.compile("xmlns:(\\w+)=\".+?\"");

    public InNamespaceMergeInterceptor() {
        super(Phase.UNMARSHAL);
        addAfter(DocLiteralInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Document document = (Document) message.getContent(Node.class);
        String payload = message.getContent(String.class);
        message.setContent(String.class, enrichNamespaces(document, payload));
    }

    
    /**
     * Copies namespace definitions from SOAP Envelope and SOAP Body elements 
     * of the given XML Document into the top-level element of the XML document
     * represented by the given String.
     * <p>
     * Caller is supposed to take care of the parameters' correctness,
     * so there is no sophisticated error handling. 
     * 
     * @param source
     *      source SOAP document as a DOM object.
     * @param target
     *      target XML Document as String.
     * @return target 
     *      XML document enriched with namespace declarations from the
     *      source XML document.
     */
    protected static String enrichNamespaces(Document source, String target) {
        Map<String, String> namespaces = new HashMap<String, String>();

        // collect namespace definitions from <soap:Envelope>
        Element envelope = source.getDocumentElement();
        addNamespacesFromElement(envelope, namespaces);

        // collect namespace definitions from <soap:Body>
        Node node = envelope.getFirstChild();
        while ((node != null) && !((node instanceof Element) && "Body".equals(node.getLocalName()))) {
            node = node.getNextSibling();
        }
        Element body = (Element) node;
        addNamespacesFromElement(body, namespaces);

        if (!namespaces.isEmpty()) {
            // ignore namespaces which are (re)defined in the payload
            int endPos = target.indexOf('>');
            if (target.charAt(endPos - 1) == '/') {
                --endPos;
            }
            String startTag = target.substring(0, endPos);
            Matcher matcher = XMLNS_PATTERN.matcher(startTag);
            while (matcher.find()) {
                namespaces.remove(matcher.group(1));
            }

            // insert remained definitions (if any)
            if (!namespaces.isEmpty()) {
                StringBuffer sb = new StringBuffer(startTag);
                for (String prefix : namespaces.keySet()) {
                    sb.append(" xmlns:").append(prefix).append("=\"")
                      .append(namespaces.get(prefix)).append('"');
                }
                sb.append(target.substring(endPos));
                return sb.toString();
            }
        }

        return target;
    }

    
    /**
     * Adds NS defined in the given XML element to the map.
     * <p>
     * Existing map items will be overwritten, so this method should be called
     * in the tree-descending order.
     */
    protected static void addNamespacesFromElement(Element elem, Map<String, String> map) {
        NamedNodeMap attributes = elem.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i) {
            Node attribute = attributes.item(i);
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI())) {
                map.put(attribute.getLocalName(), attribute.getTextContent());
            }
        }
    }

}