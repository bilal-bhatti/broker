package com.neelo.broker.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class TestServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(TestServlet.class);

	private String success;
	private String failure;

	@Override
	public void init(ServletConfig config) throws ServletException {
		success = "{\"header\":{\"status\":0,\"message\":\"Success\"}, \"body\":{}}";
		failure = "{\"header\":{\"status\":1,\"message\":\"Failure\"}, \"body\":{}}";
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		resp.setContentType("text/plain");
		byte[] msg = new byte[req.getContentLength()];
		req.getInputStream().read(msg);

		Random r = new Random();

		log.info("incoming message: " + new String(msg));
		if (r.nextBoolean())
			out.println(success);
		else
			out.println(failure);
		out.close();
	}
}
