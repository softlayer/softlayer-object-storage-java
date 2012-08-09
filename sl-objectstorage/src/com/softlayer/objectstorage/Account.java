package com.softlayer.objectstorage;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.lang.text.StrTokenizer;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Unless otherwise noted, all files are released under the MIT license,
 * exceptions contain licensing information in them.
 * 
 * Copyright (C) 2012 SoftLayer Technologies, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * Except as contained in this notice, the name of SoftLayer Technologies, Inc.
 * shall not be used in advertising or otherwise to promote the sale, use or
 * other dealings in this Software without prior written authorization from
 * SoftLayer Technologies, Inc.
 * 
 * Portions Copyright © 2008-9 Rackspace US, Inc.
 * 
 * 
 * Object representing the base object storage account
 * 
 * 
 * 
 */
public class Account extends Client {

	String username;
	String password;

	/**
	 * constructor for setting up an account object
	 * 
	 * @param baseUrl
	 *            base objectstorage api url
	 * @param username
	 *            username to use for this api connection
	 * @param password
	 *            password for the account
	 * @throws IOException
	 */
	public Account(String baseUrl, String username, String password)
			throws IOException {
		super(baseUrl, username, password, true);
		this.username = username;
		this.password = password;

	}

	/**
	 * list all containers on this account
	 * 
	 * @return a list of container objects
	 * @throws IOException
	 */
	public List<Container> listAllContainers() throws IOException {
		Hashtable<String, String> params = super.createAuthParams();
		ClientResource client = super.get(params, super.storageurl);
		Representation entity = client.getResponseEntity();
		String containers = entity.getText();
		StrTokenizer tokenize = new StrTokenizer(containers);
		tokenize.setDelimiterString("\n");
		String[] cont = tokenize.getTokenArray();
		ArrayList<Container> conts = new ArrayList<Container>();
		for (String token : cont) {

			conts.add(new Container(token, super.baseurl, this.username,
					this.password, false));

		}

		return conts;

	}

	/**
	 * list all CDN enabled containers for this account
	 * 
	 * @return a list of containers with CDN enabled
	 * @throws IOException
	 */
	public List<Container> listAllCDNContainers() throws IOException {
		Hashtable<String, String> params = super.createAuthParams();
		params.put(X_CONTENT, "cdn");
		ClientResource client = super.get(params, super.storageurl);
		Representation entity = client.getResponseEntity();
		String containers = entity.getText();
		StrTokenizer tokenize = new StrTokenizer(containers);
		tokenize.setDelimiterString("\n");
		String[] cont = tokenize.getTokenArray();
		ArrayList<Container> conts = new ArrayList<Container>();
		for (String token : cont) {

			conts.add(new Container(token, super.baseurl, this.username,
					this.password, false));

		}

		return conts;

	}

	/**
	 * List the CDN urls for streaming and SSL and streaming
	 * 
	 * @return a string list of URLs
	 * @throws IOException
	 */
	public List<String> listCDNUrls() throws IOException {
		Hashtable<String, String> params = super.createAuthParams();
		params.put(X_CONTENT, "cdn");
		ClientResource client = super.head(params, super.storageurl);
		ArrayList<String> urls = new ArrayList<String>();
		urls.add(getCustomHttpHeader(X_CDN_URL, client));
		urls.add(getCustomHttpHeader(X_CDN_SSL_URL, client));
		urls.add(getCustomHttpHeader(X_CDN_STREAM_HTTP_URL, client));
		urls.add(getCustomHttpHeader(X_CDN_STREAM_FLASH_URL, client));
		return urls;
	}

	/**
	 * get a container by the specified name
	 * 
	 * @param containerName
	 *            the name for the container to return
	 * @return the container with the matching name
	 * @throws IOException
	 */
	public Container getContainer(String containerName) throws IOException {

		Container container = new Container(containerName, super.baseurl,
				this.username, this.password, false);

		return container;
	}

	/**
	 * Search the container for values
	 * 
	 * @param base
	 *            the base url for objectstorage api
	 * @param query
	 *            terms for searching
	 * @param limit
	 *            number of results to return (pass null for default value)
	 * @param start
	 *            pagination offset for results to return (pass null for default
	 *            value)
	 * @param field
	 *            name of a field to limit the search to (pass null for default
	 *            value)
	 * @param type
	 * @param format
	 * @param marker
	 * @param recursive
	 * @return search result
	 * @throws IOException
	 */
	public String search(String base, String query, Long limit, Long start,
			String field, String type, String format, String marker,
			boolean recursive) throws IOException {

		Hashtable<String, String> params = super.createAuthParams();
		params.put(X_CONTEXT, "search");
		String url = super.storageurl
				+ super.makeSearchUrl(query, limit, start, field, type, format,
						marker, recursive);

		ClientResource client = super.get(params, url);
		Representation entity = client.getResponseEntity();
		String s = entity.getText();
		return s;
	}
}
