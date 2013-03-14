package com.neelo.broker.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.neelo.broker.Broker;
import com.neelo.broker.GuiceFactory;
import com.neelo.broker.MessageFactory;
import com.neelo.broker.Message;

public class BrokerServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(BrokerServlet.class);

	private Broker broker;
	private MessageFactory factory;

	private String success;

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			String brokerConfig = config.getInitParameter("configuration");

			if (StringUtils.isBlank(brokerConfig))
				brokerConfig = "broker.properties";

			String path = config.getServletContext().getRealPath("/WEB-INF/" + brokerConfig);

			if (!GuiceFactory.initialized())
				GuiceFactory.initalize(path);

			broker = GuiceFactory.getInjector().getInstance(Broker.class);
			factory = GuiceFactory.getInjector().getInstance(MessageFactory.class);

			success = factory.marshall(factory.getSuccess());
		} catch (ServletException se) {
			log.error("Failed to initialize broker", se);
			throw se;
		} catch (Exception e) {
			log.error("Failed to initialize broker", e);
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		broker.stop();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
		resp.getWriter().write(
				"<html><head><title>A Simple Message Broker</title>"
						+ "</head><body>This is a simple and lightweight message broker</body></html>");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		resp.setContentType("text/plain");

		try {
			Message msg = factory.unmarshall(req.getInputStream());
			broker.service(msg);
			out.println(success);
			out.close();

		} catch (Exception e) {
			log.error("Failed to process incoming message", e);
			try {
				out.print(factory.marshall(factory.getFailure(e.getMessage())));
			} catch (Exception e1) {
				log.error("Failed to marshall to JSON", e1);
			}
		}
	}
}
