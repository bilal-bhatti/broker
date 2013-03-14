package com.neelo.broker;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.neelo.broker.http.HttpPoster;

public class DefaultMessageHandler implements MessageHandler {
	private static final Logger log = Logger.getLogger(DefaultMessageHandler.class);

	private final MessageManager manager;
	private final HttpPoster poster;
	private final Integer zero = 0;

	@Inject
	public DefaultMessageHandler(MessageManager manager, HttpPoster poster) {
		this.manager = manager;
		this.poster = poster;
	}

	public void publish(Message msg) {
		try {
			Message response = poster.post(msg);

			Integer status = response.getHeader().getStatus();
			if (zero.equals(status)) {
				log.debug("Successfuly posted message");
				log.debug("Message: " + msg);
				log.debug("Response: " + response);
				if (msg.getId() != null) {
					log.debug("Deleting message");
					manager.delete(msg.getId());
				}
			} else {
				log.debug("Failed to post message");
				log.debug("Message: " + msg);
				log.debug("Response: " + response);
				if (msg.getHeader().isReliable() && msg.getId() == null) {
					log.debug("Saving message for retry");
					manager.save(msg);
				}
			}
		} catch (Exception e) {
			if (msg.getHeader().isReliable() && msg.getId() == null)
				manager.save(msg);
			log.error("Failed to post message", e);
		}
	}
}
