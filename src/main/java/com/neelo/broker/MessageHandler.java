package com.neelo.broker;

public interface MessageHandler {
	void publish(Message msg);
}
