package com.softlayer.objectstorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
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
 * base client for making calls to objectstorage API
 * 
 * 
 */
public class Client {

	private static final int GET = 0;
	private static final int POST = 1;
	private static final int PUT = 2;
	private static final int DELETE = 3;
	private static final int HEAD = 4;
	static final String USERNAME = "x-auth-user";
	static final String PASSWORD = "x-auth-key";
	static final String X_AUTH_TOKEN = "X-Auth-Token";
	static final String X_STORAGE_URL = "X-Storage-Url";
	static final String X_COPY_FROM = "X-Copy-From";
	static final String X_OBJECT_META = "X-Object-Meta-";
	static final String X_CDN_MANAGEMENT_URL = "X-CDN-Management-URL";
	static final String X_CONTENT = "X-Content";
	static final String X_CONTEXT = "X-Context";
	static final String X_CDN_URL = "X-cdn-url";
	static final String X_CDN_SSL_URL = "X-cdn-ssl-url";
	static final String X_CDN_STREAM_HTTP_URL = "X-cdn-stream-http-url";
	static final String X_CDN_STREAM_FLASH_URL = "X-cdn-stream-flash-url";
	static final String X_CDN_TTL = "X-TTL";
	static final String X_CDN_ENABLED = "X-CDN-Enabled";
	private static final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
	protected String username;
	protected String password;
	protected String authToken;
	protected String storageurl;
	protected String cdnurl;
	protected String baseurl;
	protected String authurl;

	Logger logger = Logger.getLogger(Client.class);

	/**
	 * allows for simple creation of an authenticated http client session with
	 * objectstorage api
	 * 
	 * @param baseUrl
	 *            the base url for the objectstorage instance
	 * @param username
	 *            username for SL portal user to be used for susequent calls
	 * @param password
	 *            password for the given user or the api key as a String
	 * @throws IOException
	 */
	public Client(String baseUrl, String username, String password, boolean auth)
			throws IOException {
		this.username = username;
		this.password = password;
		baseurl = baseUrl;
		authurl = baseUrl + "/auth/v1.0";
		if (auth)
			this.auth(username, password);
	}

	/**
	 * convenience method for making an auth call to objectstorage via restlet
	 * client
	 * 
	 * 
	 * @param username
	 *            username for SL portal user to be used for susequent calls
	 * @param password
	 *            password for the given user or the api key as a String
	 * 
	 * @throws IOException
	 */
	protected void auth(String username, String password) throws IOException {

		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(USERNAME, username);
		params.put(PASSWORD, password);

		ClientResource client = this.get(params, authurl);

		this.authToken = getCustomHttpHeader(X_AUTH_TOKEN, client);
		this.storageurl = getCustomHttpHeader(X_STORAGE_URL, client);
		this.cdnurl = getCustomHttpHeader(X_CDN_MANAGEMENT_URL, client);

	}

	/**
	 * wrapper utility for making POST requests via restlet client
	 * 
	 * @param params
	 *            Hashtable with all form/request params
	 * @param url
	 *            the url to make the client request to
	 * 
	 * @throws IOException
	 */
	protected ClientResource post(Hashtable<String, String> params,
			Representation representation, String url) throws IOException {
		return this.httpRequest(params, url, representation, POST);
	}

	/**
	 * wrapper utility for making PUT requests via restlet client
	 * 
	 * @param params
	 *            Hashtable with all form/request params
	 * @param url
	 *            the url to make the client request to
	 * @throws IOException
	 */
	protected ClientResource put(Hashtable<String, String> params,
			Representation representation, String url) throws IOException {
		return this.httpRequest(params, url, representation, PUT);
	}

	/**
	 * wrapper utility for making GET requests via restlet client
	 * 
	 * @param params
	 *            Hashtable with all form/request params
	 * @param url
	 *            the url to make the client request to
	 * 
	 * @throws IOException
	 */
	protected ClientResource get(Hashtable<String, String> params, String url)
			throws IOException {
		return this.httpRequest(params, url, null, GET);
	}

	/**
	 * wrapper utility for making GET requests via restlet client
	 * 
	 * @param params
	 *            Hashtable with all form/request params
	 * @param url
	 *            the url to make the client request to
	 * @throws IOException
	 */
	protected ClientResource head(Hashtable<String, String> params, String url)
			throws IOException {
		return this.httpRequest(params, url, null, HEAD);
	}

	/**
	 * wrapper utility for making DELETE requests via restlet client
	 * 
	 * @param params
	 *            Hashtable with all form/request params
	 * @param url
	 *            the url to make the client request to
	 * @throws IOException
	 */
	protected void delete(Hashtable<String, String> params, String url)
			throws IOException {
		this.httpRequest(params, url, null, DELETE);
	}

	/**
	 * Utility for making restlet http requests
	 * 
	 * @param params
	 *            Hashtable with all form/request params
	 * @param url
	 *            the url to make the client request to
	 * @param type
	 *            the type of request (GET,POST,PUT,DELETE)
	 * @throws IOException
	 */
	private ClientResource httpRequest(Hashtable<String, String> params,
			String url, Representation representation, int type)
			throws IOException {
		Context ctx = new Context();
		if (url.toLowerCase().startsWith("https")) {
			ctx.getParameters().add("truststorePath",
					"objectstoragecacerts.jks");
			ctx.getParameters().add("truststorePassword", "changeit");
			ctx.getParameters().add("truststoreType", "JKS");
		}
		ClientResource requestResource = new ClientResource(ctx, url);

		switch (type) {

		case POST:
			Representation post = getRepresentation(params);
			requestResource.post(post);
			break;

		case PUT:
			setCustomHttpHeader(params, requestResource);
			requestResource.put(representation);
			break;

		case GET:
			setCustomHttpHeader(params, requestResource);
			requestResource.get();

			break;

		case HEAD:
			setCustomHttpHeader(params, requestResource);
			requestResource.head();

			break;

		case DELETE:
			setCustomHttpHeader(params, requestResource);
			requestResource.delete();
			break;

		}

		return requestResource;

	}

	/**
	 * Utility for adding form params to a restlet representation
	 * 
	 * @param params
	 *            Hashtable of params to be converted to form params
	 * @return the restlet Representation with the form added
	 */
	private static Representation getRepresentation(
			Hashtable<String, String> params) {
		// Gathering informations into a Web form.
		Form form = new Form();
		Enumeration<String> en = params.keys();
		while (en.hasMoreElements()) {
			String key = en.nextElement();
			String value = params.get(key);
			form.add(key, value);
		}

		return form.getWebRepresentation();

	}

	/**
	 * Set the value of a custom HTTP header
	 * 
	 * @param header
	 *            the custom HTTP header to set the value for, for example
	 *            <code>X-MyApp-MyHeader</code>
	 * @param value
	 *            the value of the custom HTTP header to set
	 */
	public static void setCustomHttpHeader(Hashtable<String, String> params,
			ClientResource client) {

		Map<String, Object> reqAttribs = client.getRequestAttributes();
		Form headers = (Form) reqAttribs.get(RESTLET_HTTP_HEADERS);
		if (headers == null) {
			headers = new Form();
			reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
		}

		Enumeration<String> en = params.keys();
		while (en.hasMoreElements()) {
			String header = en.nextElement();
			String value = params.get(header);
			headers.add(header, value);
		}
	}

	/**
	 * Set the value of a custom HTTP header
	 * 
	 * @param header
	 *            the custom HTTP header to set the value for, for example
	 *            <code>X-MyApp-MyHeader</code>
	 * @param value
	 *            the value of the custom HTTP header to set
	 */
	public static String getCustomHttpHeader(String name, ClientResource client) {

		Response resp = client.getResponse();
		Map<String, Object> att = resp.getAttributes();
		Form responseHeaders = (Form) att.get(RESTLET_HTTP_HEADERS);
		return responseHeaders.getFirstValue(name, true);

	}

	/**
	 * Utility for creating a request param search string
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
	 * @return a string url query
	 */
	protected static String makeSearchUrl(String query, Long limit, Long start,
			String field, String type, String format, String marker,
			boolean recursive) {
		ArrayList<String> params = new ArrayList<String>();
		if (query != null) {
			params.add("q=" + query);
		}
		if (limit != null) {
			params.add("limit=" + limit.longValue());
		}
		if (start != null) {
			params.add("start=" + start.longValue());
		}
		if (field != null) {
			params.add("field=" + field);
		}
		if (type != null) {
			params.add("type=" + type);
		}
		if (format != null) {
			params.add("format=" + format);
		}
		if (marker != null) {
			params.add("marker=" + field);
		}
		if (recursive) {
			params.add("recursive=" + recursive);
		}

		String p = "";

		for (String s : params) {

			p += (s + "&");
		}

		String base = "?" + p;
		return base;
	}

	/**
	 * Utility for authorizing and adding the auth token to the header
	 * 
	 * @return Hashtable with auth params set
	 * @throws IOException
	 */
	protected Hashtable<String, String> createAuthParams() throws IOException {
		if (authToken == null)
			this.auth(this.username, this.password);
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(Client.X_AUTH_TOKEN, this.authToken);
		return params;
	}

	/**
	 * special wrapper function for a url encoding (handles +'s etc)
	 * 
	 * @return a url safe string
	 * @throws EncoderException
	 */
	protected String saferUrlEncode(String value) throws EncoderException {
		URLCodec ucode = new URLCodec();
		String uName = ucode.encode(value).replaceAll("\\+", "%20");
		return uName;
	}

	/**
	 * Utility to validate if name is valid for use as objectstorage name
	 * 
	 * @param name
	 * @return true if name is valid
	 */
	protected boolean isValidName(String name) {
		if (name == null) {
			return false;
		}
		int length = name.length();
		if (length == 0 || length > 256) {
			return false;
		}
		if (name.indexOf('/') != -1) {
			return false;
		}
		return true;
	}

}
