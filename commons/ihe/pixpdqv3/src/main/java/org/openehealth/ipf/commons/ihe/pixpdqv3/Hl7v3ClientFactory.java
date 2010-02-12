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
package org.openehealth.ipf.commons.ihe.pixpdqv3;

import org.apache.cxf.endpoint.Client;
import org.openehealth.ipf.commons.ihe.ws.ItiClientFactory;
import org.openehealth.ipf.commons.ihe.ws.ItiServiceInfo;
import org.openehealth.ipf.commons.ihe.ws.cxf.databinding.plainxml.PlainXmlDataBinding;
import org.openehealth.ipf.commons.ihe.ws.cxf.payload.InNamespaceMergeInterceptor;
import org.openehealth.ipf.commons.ihe.ws.cxf.payload.InPayloadInjectorInterceptor;
import org.openehealth.ipf.commons.ihe.ws.cxf.payload.InPayloadExtractorInterceptor;

/**
 * Factory for HL7 v3 Web Service clients.
 * @author Dmytro Rud
 */
public class Hl7v3ClientFactory extends ItiClientFactory {

    /**
     * Constructs the factory.
     * @param serviceInfo
     *          the info about the web-service.
     * @param soap11
     *          whether SOAP 1.1 should be used instead of SOAP 1.2.
     * @param serviceUrl
     *          the URL of the web-service.
     */
    public Hl7v3ClientFactory(ItiServiceInfo serviceInfo, boolean soap11, String serviceUrl) {
        super(serviceInfo, serviceUrl);
    }

    
    @Override
    protected void configureInterceptors(Client client) {
        super.configureInterceptors(client);
        client.getInInterceptors().add(new InPayloadExtractorInterceptor());
        client.getInInterceptors().add(new InNamespaceMergeInterceptor());
        client.getInInterceptors().add(new InPayloadInjectorInterceptor(0));
        client.getEndpoint().getService().setDataBinding(new PlainXmlDataBinding());
    }
}