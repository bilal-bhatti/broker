package com.neelo.broker;

import java.io.InputStream;
import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;

public class MessageFactory extends ObjectMapper {
	public Message getSuccess() {
		Message success = new Message();
		success.getHeader().setStatus(0);
		success.getHeader().setMessage("Success");

		return success;
	}

	public Message getFailure(String message) {
		Message failure = new Message();
		failure.getHeader().setStatus(1);
		failure.getHeader().setMessage(message);

		return failure;
	}

	public Message newInstance(String topic, boolean reliable) {
		return new Message(topic, reliable);
	}

	public String marshall(Message message) throws Exception {
		StringWriter writer = new StringWriter();
		writeValue(writer, message);
		return writer.toString();
	}

	public Message unmarshall(String json) throws Exception {
		return readValue(json, Message.class);
	}

	public Message unmarshall(InputStream stream) throws Exception {
		return readValue(stream, Message.class);
	}
}