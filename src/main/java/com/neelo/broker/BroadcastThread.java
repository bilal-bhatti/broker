package com.neelo.broker;

import org.apache.log4j.Logger;

class BroadcastThread implements Runnable {
	private static final Logger log = Logger.getLogger(BroadcastThread.class);

	private final Router router;
	private final Message msg;

	public BroadcastThread(final Router router, final Message msg) {
		this.router = router;
		this.msg = msg;
	}

	public void run() {
		try {
			router.broadcast(msg);
		} catch (Exception e) {
			log.error("Failed to broadcast message", e);
		}
	}
}