/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.cxf;

import org.mule.api.MuleMessage;
import org.mule.config.i18n.LocaleMessageHandler;
import org.mule.module.client.MuleClient;
import org.mule.module.xml.util.XMLUtils;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.http.HttpConstants;
import org.mule.util.IOUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.custommonkey.xmlunit.XMLUnit;

public class CxfBasicTestCase extends FunctionalTestCase
{
    private String echoWsdl;

    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        echoWsdl = IOUtils.getResourceAsString("cxf-echo-service.wsdl", getClass());
        XMLUnit.setIgnoreWhitespace(true);
        try
        {
            XMLUnit.getTransformerFactory();
        }
        catch (TransformerFactoryConfigurationError e)
        {
            XMLUnit.setTransformerFactory(XMLUtils.TRANSFORMER_FACTORY_JDK5);
        }
    }

    public void testEchoService() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Content-Type", "application/soap+xml");
        InputStream xml = getClass().getResourceAsStream("/direct/direct-request.xml");
        MuleMessage result = client.send("http://localhost:63081/services/Echo", xml, props);
        assertTrue(result.getPayloadAsString().contains("Hello!"));
        String ct = result.getStringProperty(HttpConstants.HEADER_CONTENT_TYPE, "");
        assertEquals("text/xml; charset=UTF-8", ct);
    }

    public void testEchoServiceEncoding() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        String message = LocaleMessageHandler.getString("test-data",
            Locale.JAPAN, "CxfBasicTestCase.testEchoServiceEncoding", new Object[]{});
        MuleMessage result = client.send("cxf:http://localhost:63081/services/Echo?method=echo", message, null);
        String ct = result.getStringProperty(HttpConstants.HEADER_CONTENT_TYPE, "");

        assertEquals(message, result.getPayload());
        assertEquals("text/xml; charset=UTF-8", ct);
    }

    public void testEchoWsdl() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.request("http://localhost:63081/services/Echo?wsdl", 5000);
        assertNotNull(result.getPayload());
        XMLUnit.compareXML(echoWsdl, result.getPayloadAsString());
    }

    protected String getConfigResources()
    {
        return "basic-conf.xml";
    }

}