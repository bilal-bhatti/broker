package com.neelo.broker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(value = false)
public class Message {
	private Header header;
	private Map<String, Object> body;
	private Long id;
	private String consumer;

	public Message() {
		super();
		header = new Header();
		body = new LinkedHashMap<String, Object>();
	}

	public Message(String topic) {
		this();
		header.setTopic(topic);
	}

	public Message(String topic, Boolean reliable) {
		this(topic);
		header.setReliable(reliable);
	}

	@JsonIgnore
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonIgnore
	public String getConsumer() {
		return consumer;
	}

	public void setConsumer(String consumer) {
		this.consumer = consumer;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@JsonWriteNullProperties(value = false)
	static class Header {
		private String topic;
		private boolean reliable;
		private Integer status;
		private String message;

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}

		public boolean isReliable() {
			return reliable;
		}

		public void setReliable(boolean reliable) {
			this.reliable = reliable;
		}

		public Integer getStatus() {
			return status;
		}

		public void setStatus(Integer status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}
}
