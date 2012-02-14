package com.softlayer.objectstorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
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
 * This represents a file object on the objectstorage server
 * 
 * 
 */
public class ObjectFile extends Client {

	private String name;
	private String containerName;
	private byte[] bytes;
	private Map<String, Object> headers;

	/**
	 * This class represents a file object in a container on the objectstorage
	 * server
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
	public ObjectFile(String name, String containerName, String baseUrl,
			String username, String password, boolean auth) throws IOException {

		super(baseUrl, username, password, auth);
		this.containerName = containerName;
		this.name = name;

	}

	/**
	 * Get the name of the file on the objectstorage server
	 * 
	 * @return the name of the file on the objectstorage server
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the file on the objectstorage server
	 * 
	 * @param name
	 *            the name of the file on the objectstorage server
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * return the headers for this client transaction
	 * 
	 * @return Map of the name value pairs of the headers for this client
	 *         transaction
	 * @throws EncoderException
	 * @throws IOException
	 */
	public Map<String, Object> getMetaTags() throws EncoderException,
			IOException {
		if (headers == null)
			this.loadFileData();

		return headers;

	}

	/**
	 * returns a byte[] representation of the file from the objectstorage server
	 * 
	 * @return a byte[] representation of the file from the objectstorage server
	 * @throws EncoderException
	 * @throws IOException
	 */
	public byte[] getBytes() throws EncoderException, IOException {

		if (this.bytes == null)
			this.loadFileData();

		return bytes;
	}

	/**
	 * upload this file from a local file copy to the objectstorage server
	 * 
	 * @param localFileLocation
	 *            string representation of the path of the local file
	 * @param tags
	 *            Map of tags to attach to this file
	 * @return etag value of this upload
	 * @throws EncoderException
	 * @throws IOException
	 */
	public String uploadFile(String localFileLocation, Map<String, String> tags)
			throws EncoderException, IOException {
		if (super.isValidName(this.name)) {
			File file = new File(localFileLocation);
			bytes = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(bytes);
			Hashtable<String, String> params = super.createAuthParams();
			Iterator<Map.Entry<String, String>> it = tags.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
						.next();
				params.put(Client.X_OBJECT_META + pairs.getKey(),
						pairs.getValue());
				it.remove();
			}

			String uName = super.saferUrlEncode(this.containerName);
			String fName = super.saferUrlEncode(this.name);
			Representation representation = new InputRepresentation(
					new ByteArrayInputStream(bytes), MediaType.ALL);
			ClientResource client = super.put(params, representation,
					super.storageurl + "/" + uName + "/" + fName);
			this.headers = client.getResponseAttributes();
			Form head = (Form) this.headers.get("org.restlet.http.headers");
			return head.getFirstValue("Etag");
		} else {
			throw new EncoderException("invalid file name");
		}

	}

	/**
	 * removes this file form the objectstorage server
	 * 
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void remove() throws EncoderException, IOException {
		// super.auth(username, password);
		Hashtable<String, String> params = super.createAuthParams();
		String uName = super.saferUrlEncode(this.containerName);
		String fName = super.saferUrlEncode(this.name);
		super.delete(params, super.storageurl + "/" + uName + "/" + fName);

	}

	/**
	 * purge this file from CDN
	 * 
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void purgeCDN() throws EncoderException, IOException {
		// super.auth(username, password);
		Hashtable<String, String> params = super.createAuthParams();
		String uName = super.saferUrlEncode(this.containerName);
		String fName = super.saferUrlEncode(this.name);
		super.delete(params, super.cdnurl + "/" + uName + "/" + fName);

	}

	/**
	 * make a file copy in this container from another file
	 * 
	 * @param container
	 *            the source container to copy from
	 * @param objectName
	 *            the source file to copy from
	 * @throws EncoderException
	 * @throws IOException
	 */
	public void copyFrom(String container, String objectName)
			throws EncoderException, IOException {
		String cuName = super.saferUrlEncode(container);
		String cfName = super.saferUrlEncode(objectName);
		String sourceUrl = cuName + "/" + cfName;
		Hashtable<String, String> params = super.createAuthParams();
		params.put(Client.X_COPY_FROM, sourceUrl);

		String uName = super.saferUrlEncode(this.containerName);
		String fName = super.saferUrlEncode(this.name);

		super.put(params, null, super.storageurl + "/" + uName + "/" + fName);
	}

	/**
	 * Utility method for getting data from REST api to populate this object
	 * 
	 * @throws EncoderException
	 * @throws IOException
	 */
	private void loadFileData() throws EncoderException, IOException {
		Hashtable<String, String> params = super.createAuthParams();
		URLCodec ucode = new URLCodec();
		String uName = ucode.encode(this.containerName)
				.replaceAll("\\+", "%20");
		String fName = ucode.encode(this.name).replaceAll("\\+", "%20");
		ClientResource client = super.get(params, super.storageurl + "/"
				+ uName + "/" + fName);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		client.get().write(outputStream);
		this.bytes = outputStream.toByteArray();
		this.headers = client.getResponseAttributes();
	}

}
