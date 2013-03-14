package com.neelo.broker;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class Router {
	private static final Logger log = Logger.getLogger(Router.class);

	final private MessageHandler handler;
	final private RouteTable routeTable;

	@Inject
	public Router(MessageHandler handler, RouteTable routeTable) {
		this.handler = handler;
		this.routeTable = routeTable;
	}

	public void broadcast(Message msg) {
		if (StringUtils.isBlank(msg.getConsumer())) {
			Set<Route> routes = routeTable.routes(msg);
			if (routes == null) {
				log.warn("No routes found for message with topic: " + msg.getHeader().getTopic());
				return;
			}
			for (Route route : routes) {
				msg.setConsumer(route.getConsumer());
				handler.publish(msg);
			}
		} else {
			handler.publish(msg);
		}
	}
}
