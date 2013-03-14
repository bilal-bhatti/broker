package com.neelo.broker;

public class MessageException extends RuntimeException {
	public MessageException(Throwable t) {
		super(t);
	}

	public MessageException(String message) {
		super(message);
	}
}
