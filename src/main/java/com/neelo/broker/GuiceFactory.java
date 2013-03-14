package com.neelo.broker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.neelo.broker.http.HttpPoster;

public class GuiceFactory {
	private static final Logger log = Logger.getLogger(GuiceFactory.class);

	private static Properties properties;
	private static Injector injector;
	private static boolean initialized = false;

	private GuiceFactory() {
	}

	public static void initalize(String brokerConfig) throws FileNotFoundException, IOException {
		if (initialized)
			return;

		log.info("Loading properties from: " + brokerConfig);
		properties = new Properties();
		properties.load(new FileInputStream(new File(brokerConfig)));

		injector = Guice.createInjector(new Module[] { new BrokerModule() });

		initialized = true;
	}

	public static boolean initialized() {
		return initialized;
	}

	public static Injector getInjector() {
		return injector;
	}

	private static class BrokerModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(Broker.class).in(Scopes.SINGLETON);
			bind(Router.class).in(Scopes.SINGLETON);
			bind(RouteTable.class).in(Scopes.SINGLETON);
			bind(MessageHandler.class).to(DefaultMessageHandler.class).in(Scopes.SINGLETON);
			bind(MessageFactory.class).in(Scopes.SINGLETON);
			bind(MessageManager.class).in(Scopes.SINGLETON);
			bind(HttpPoster.class).in(Scopes.SINGLETON);
			bind(Properties.class).annotatedWith(Names.named("config")).toInstance(properties);
			bind(Connection.class).toProvider(new ConnectionProvider(properties));
		}
	}

	static class ConnectionProvider implements Provider<Connection> {
		private DataSource dataSource;
		private final Properties props;

		ConnectionProvider(Properties props) {
			log.debug("Instantiating connection provider");
			this.props = props;
		}

		public Connection get() {
			try {
				return wrap();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private Connection wrap() throws Exception {
			String ds = (String) props.get("jdbc.datasource");
			if (StringUtils.isBlank(ds)) {
				String driver = (String) props.get("jdbc.driver");
				String url = (String) props.get("jdbc.url");
				String username = (String) props.get("jdbc.username");
				String password = (String) props.get("jdbc.password");

				Class.forName(driver);
				return DriverManager.getConnection(url, username, password);
			} else {
				if (dataSource == null) {
					Context ctx = new InitialContext();
					dataSource = (DataSource) ctx.lookup(ds);
				}
				return dataSource.getConnection();
			}
		}
	}
}
