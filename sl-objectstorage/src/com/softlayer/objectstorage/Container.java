package com.softlayer.objectstorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.codec.EncoderException;
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
 * This class represents a container on the objectstorage server
 * 
 */
public class Container extends Client {

	private String name;
	private String baseUrl;
	private String username;
	private String password;
	private ArrayList<ObjectFile> objs;

	/**
	 * constructor for new container object on objectstorage server
	 * 
	 * @param name
	 *            the name of the server side objectstorage object
	 * @param containerName
	 *            the name of the container this object resides in
	 * @param baseUrl
	 *            the base sift rest api url
	 * @param username
	 *            the username for this session
	 * @param password
	 *            the password for this session
	 * @param auth
	 *            whether to auth this transaction with the REST api if not
	 *            (useful if you have not yet connected this session or if you
	 *            need/want to re-auth)
	 * @throws IOException
	 */
	public Container(String name, String baseUrl, String username,
			String password, boolean auth) throws IOException {

		super(baseUrl, username, password, auth);

		this.name = name;
		this.baseUrl = baseUrl;
		this.username = username;
		this.password = password;

	}

	/**
	 * returns the name of this container on the objectstorage server
	 * 
	 * @return a String representation of the container name on the
	 *         objectstorage server
	 */
	public String getName() {
		return name;
	}

	/**
	 * this method gets a list of all objects in this container on the
	 * objectstorage server
	 * 
	 * @return list of objectfiles
	 * @throws IOException
	 * @throws EncoderException
	 */
	public List<ObjectFile> listObjectFiles() throws IOException,
			EncoderException {

		if (this.objs == null)
			this.loadData();

		return objs;

	}

	/**
	 * create this container on the server
	 * 
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void create() throws EncoderException, IOException {
		Hashtable<String, String> params = super.createAuthParams();
		if (super.isValidName(this.name)) {
			String uName = super.saferUrlEncode(this.name);
			super.put(params, null, super.storageurl + "/" + uName);
		} else {
			throw new EncoderException("Invalid Container Name");
		}
	}

	/**
	 * enable CDN access to the files in this container
	 * 
	 * @param ttl
	 *            the ttl for the CDN objects in this container
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void enableCDN(Integer ttl) throws EncoderException, IOException {
		Hashtable<String, String> params = super.createAuthParams();
		if (ttl != null)
			params.put(Client.X_CDN_TTL, Integer.toString(ttl));
		String uName = super.saferUrlEncode(this.name);
		super.put(params, null, super.cdnurl + "/" + uName);
	}

	/**
	 * change the ttl value for this container on the objectstorage server
	 * 
	 * @param ttl
	 *            the ttl for the CDN objects in this container
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void updateCDNTTL(int ttl) throws EncoderException, IOException {

		Hashtable<String, String> params = super.createAuthParams();
		params.put(Client.X_CDN_TTL, Integer.toString(ttl));
		String uName = super.saferUrlEncode(this.name);
		super.post(params, null, super.cdnurl + "/" + uName);
	}

	/**
	 * disable CDN access to the files in this container
	 * 
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void disableCDN() throws EncoderException, IOException {

		Hashtable<String, String> params = super.createAuthParams();
		params.put(Client.X_CDN_ENABLED, "false");
		String uName = super.saferUrlEncode(this.name);
		super.post(params, null, super.cdnurl + "/" + uName);
	}

	/**
	 * remove this container from the objectstorage server, note this will throw
	 * an exception if the container is not empty
	 * 
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void remove() throws EncoderException, IOException {

		Hashtable<String, String> params = super.createAuthParams();
		String uName = super.saferUrlEncode(this.name);
		super.delete(params, super.storageurl + "/" + uName);

	}

	/**
	 * purge all CDN objects from this container
	 * 
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void purgeCDN() throws EncoderException, IOException {

		Hashtable<String, String> params = super.createAuthParams();
		String uName = super.saferUrlEncode(this.name);
		super.delete(params, super.cdnurl + "/" + uName);

	}

	/**
	 * Utility method for getting data from REST api to populate this object
	 * 
	 * @throws EncoderException
	 * @throws IOException
	 */
	private void loadData() throws EncoderException, IOException {
		Hashtable<String, String> params = super.createAuthParams();
		String uName = super.saferUrlEncode(this.name);
		ClientResource client = super.get(params, super.storageurl + "/"
				+ uName);
		Representation entity = client.getResponseEntity();
		String containers = entity.getText();
		StrTokenizer tokenize = new StrTokenizer(containers);
		tokenize.setDelimiterString("\n");
		String[] obj = tokenize.getTokenArray();
		this.objs = new ArrayList<ObjectFile>();
		for (String token : obj) {

			this.objs.add(new ObjectFile(token, this.name, this.baseUrl,
					this.username, this.password, false));

		}

	}

}
