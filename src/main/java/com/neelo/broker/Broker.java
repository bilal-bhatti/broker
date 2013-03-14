package com.neelo.broker;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class Broker {
	private static final int QUEUE_SIZE = 100;

	private static Logger log = Logger.getLogger(Broker.class);

	private final Router router;
	private final Timer timer;
	private final ThreadPoolExecutor pool;
	private final MessageManager manager;
	private final PollingThread poller;

	@Inject
	public Broker(final Router router, final MessageManager manager) {
		this.router = router;
		this.manager = manager;
		this.timer = new Timer();
		this.poller = new PollingThread();

		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		this.pool = new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, queue);
		this.pool.prestartCoreThread();

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (pool.getQueue().size() < QUEUE_SIZE)
					pool.execute(poller);
			}
		};

		log.info("Starting broker threads");
		this.timer.schedule(task, 15000, 60000);
		// this.timer.schedule(task, 5000, 10000);
	}

	public void service(final Message msg) {
		pool.execute(new BroadcastThread(router, msg));
	}

	public void stop() {
		log.info("Stopping all broker threads");
		timer.cancel();
		pool.shutdown();
	}

	class PollingThread implements Runnable {
		public void run() {
			try {
				log.debug("Polling for messages");
				Message msg = null;
				while ((msg = manager.next()) != null) {
					pool.execute(new BroadcastThread(router, msg));
					if (pool.getQueue().size() > QUEUE_SIZE)
						break;
				}
			} catch (Exception e) {
				log.error("Failed to poll for messages", e);
			}
		}
	}
}
