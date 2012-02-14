package com.softlayer.objectstorage.test;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Form;
import org.restlet.data.Parameter;

import static org.junit.Assert.assertTrue;

import com.softlayer.objectstorage.Account;
import com.softlayer.objectstorage.Container;
import com.softlayer.objectstorage.ObjectFile;

/**
 * Unless otherwise noted, all files are released under the MIT license,
 exceptions contain licensing information in them.
  
   Copyright (C) 2012 SoftLayer Technologies, Inc.
  
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
  
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
  
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
  
 Except as contained in this notice, the name of SoftLayer Technologies, Inc. shall not
 be used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from SoftLayer Technologies, Inc. .
 * 
 */
public class UnitTest extends TestCase{
	Account account;
	String baseUrl;
	String user;
	String password;

	@Before
	public void setUp() {

		baseUrl = "http://dal05.objectstorage.service.networklayer.com";
		user = "kmcdonald:user1";
		password = "fpEjmVD";
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(UnitTest.class);
	}

	@Test
	public void testAuth() throws IOException {
		account = new Account(baseUrl, user, password);
	}

	@Test
	public void testContainerCreate() throws IOException, EncoderException {
		Container container = new Container("RestletContainer",
				baseUrl, user, password,
				true);
		container.create();
		account = new Account(baseUrl, user, password);
		List<Container> containers = account.listAllContainers();

		for (Container cont : containers) {
			if (cont.getName().equalsIgnoreCase("RestletContainer")) {

				System.out.println(cont.getName());
				return;
			}

		}
		assertTrue(false);
	}

	@Test
	public void testObjectFileCreate() throws IOException, EncoderException {
		ObjectFile ofile = new ObjectFile("restletupload.txt",
				"RestletContainer", baseUrl,
				user, password, true);
		Map<String, String> tags = new HashMap<String, String>();
		tags.put("testtag", "testvalue");
		ofile.uploadFile("restletupload.txt", tags);
		Container container = new Container("RestletContainer",
				baseUrl, user, password,
				true);
		List<ObjectFile> files = container.listObjectFiles();
		for (ObjectFile file : files) {
			if (file.getName().equalsIgnoreCase("restletupload.txt")) {

				
				return;
			}
		}
		assertTrue(false);
	}

	@Test
	public void testGetMetaTag() throws IOException, EncoderException {
		ObjectFile ofile = new ObjectFile("restletupload.txt",
				"RestletContainer", baseUrl,
				user, password, false);
		Map<String, Object> tags = ofile.getMetaTags();
		Object hold = tags.get("org.restlet.http.headers");
		Form form = (Form) hold;
		Parameter tag = form.getFirst("X-Object-Meta-Testtag");

		assertTrue(tag.getValue().equalsIgnoreCase("testvalue"));

	}
	
	@Test
	public void testSearch() throws IOException, EncoderException {
		account = new Account(baseUrl, user, password);
		String result = account.search(baseUrl, "restlet", null, null, null, null, null, null, false);
		assertTrue(result.indexOf("Restlet")>=0);
	}

	@Test
	public void testEnableCDN() throws IOException, EncoderException {
		Container container = new Container("RestletContainer",
				baseUrl, user, password,
				true);

		container.enableCDN(null);

	}
	
	@Test
	public void testUpdateCDNTTL() throws IOException, EncoderException {
		Container container = new Container("RestletContainer",
				baseUrl, user, password,
				true);

		container.updateCDNTTL(60);

	}
	
	@Test
	public void testContainerCDNPurge() throws IOException, EncoderException {
		Container container = new Container("RestletContainer",
				baseUrl, user, password,
				true);

		container.purgeCDN();

	}
	
	@Test
	public void tesFileCDNPurge() throws IOException, EncoderException {
		ObjectFile ofile = new ObjectFile("restletupload.txt",
				"RestletContainer", baseUrl,
				user, password, false);

		ofile.purgeCDN();

	}

	@Test
	public void testDisableCDN() throws IOException, EncoderException {
		Container container = new Container("RestletContainer",
				baseUrl, user, password,
				true);

		container.disableCDN();

	}

	@Test
	public void testObjectFileRemove() throws IOException, EncoderException {
		ObjectFile ofile = new ObjectFile("restletupload.txt",
				"RestletContainer", baseUrl,
				user, password, true);
		ofile.remove();
		Container container = new Container("RestletContainer",
				baseUrl, user, password,
				true);
		List<ObjectFile> files = container.listObjectFiles();
		for (ObjectFile file : files) {
			if (file.getName().equalsIgnoreCase("restletupload.txt")) {

				System.out.println(file.getName());
				assertTrue(false);
			}
		}

	}

	@Test
	public void testContainerRemove() throws IOException, EncoderException {
		Container container = new Container("RestletContainer",
				baseUrl, user, password,
				true);
		container.remove();
		account = new Account(baseUrl, user, password);
		List<Container> containers = account.listAllContainers();

		for (Container cont : containers) {
			if (cont.getName().equalsIgnoreCase("RestletContainer")) {

				System.out.println(cont.getName());
				assertTrue(false);
				return;
			}

		}

	}

}