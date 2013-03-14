package com.neelo.broker;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;

public class RouteTable {
	private Map<String, Set<Route>> table;
	private final MessageManager manager;

	@Inject
	public RouteTable(final MessageManager manager) {
		this.manager = manager;
		this.table = new ConcurrentHashMap<String, Set<Route>>();

		load();
	}

	public void add(String topic, String consumer) {
		Set<Route> routes = table.get(topic);
		if (routes == null) {
			routes = new HashSet<Route>();
			table.put(topic, routes);
		}

		if (routes.add(new Route(topic, consumer))) {
			// now save to database
			manager.save(topic, consumer);
		}
	}

	public Set<Route> routes(Message msg) {
		return table.get(msg.getHeader().getTopic());
	}

	private void load() {
		List<Route> routes = manager.routes();
		for (Route route : routes) {
			Set<Route> tmp = table.get(route.getTopic());
			if (tmp == null) {
				tmp = new HashSet<Route>();
				table.put(route.getTopic(), tmp);
			}
			tmp.add(route);
		}
	}
}
