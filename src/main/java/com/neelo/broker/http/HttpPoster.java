package com.neelo.broker.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.neelo.broker.Message;
import com.neelo.broker.MessageFactory;

public class HttpPoster {
	private static Logger log = Logger.getLogger(HttpPoster.class);

	private final MessageFactory factory;

	@Inject
	public HttpPoster(MessageFactory factory) {
		this.factory = factory;
	}

	public Message post(Message msg) throws Exception {
		OutputStreamWriter wr = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(msg.getConsumer());
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(factory.marshall(msg));
			wr.flush();

			// Get the response
			return factory.unmarshall(conn.getInputStream());
		} finally {
			wr.close();
			if (conn != null)
				conn.disconnect();
		}
	}

	public void excutePost(Message msg) throws Exception {
		URL url;
		HttpURLConnection connection = null;
		try {
			// Create connection
			url = new URL(msg.getConsumer());
			byte[] data = factory.marshall(msg).getBytes();
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			// connection.setRequestProperty("Content-Type",
			// "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", "" + data.length);
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(data);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			log.debug(response);
			rd.close();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
