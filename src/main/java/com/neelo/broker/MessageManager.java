package com.neelo.broker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;

class MessageManager {
	private static final Logger log = Logger.getLogger(MessageManager.class);

	private final Provider<Connection> connection;

	private static final String MSG_INSERT = "insert into message (message,lock_time,state,consumer,attempts) values (?,?,?,?,?)";

	private static String MSG_NEXT;
	static {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from message ");
		sb.append("where ");
		sb.append("  lock_time is null or ? > lock_time ");
		sb.append("order by attempts asc for update");
		MSG_NEXT = sb.toString();
	}

	private static String MSG_DELETE;

	private final MessageFactory messageFactory;

	static {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from message where message_id = ? and lock_time is not null");
		MSG_DELETE = sb.toString();
	}

	@Inject
	public MessageManager(Provider<Connection> connection, MessageFactory messageFactory) {
		log.info("Initializing message manager");
		this.connection = connection;
		this.messageFactory = messageFactory;
	}

	public boolean save(Message msg) throws MessageException {
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			con = connection.get();

			pstmt = con.prepareStatement(MSG_INSERT);
			pstmt.setString(1, messageFactory.marshall(msg));
			pstmt.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			pstmt.setString(3, "A");
			pstmt.setString(4, msg.getConsumer());
			pstmt.setInt(5, 1);

			int count = pstmt.executeUpdate();

			return count > 0 ? true : false;
		} catch (Exception e) {
			throw new MessageException(e);
		} finally {
			try {
				pstmt.close();
				con.close();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	public Message next() throws MessageException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = connection.get();
			con.setAutoCommit(false);

			pstmt = con.prepareStatement(MSG_NEXT, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -15);
			pstmt.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
			pstmt.setFetchSize(1);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				Message message = messageFactory.unmarshall(rs.getString("message"));
				message.setId(rs.getLong("message_id"));
				message.setConsumer(rs.getString("consumer"));

				rs.updateTimestamp("lock_time", new Timestamp(Calendar.getInstance().getTimeInMillis()));
				int attempts = rs.getInt("attempts");
				rs.updateInt("attempts", attempts + 1);
				rs.updateRow();

				return message;
			}
			con.commit();
			return null;
		} catch (Exception e) {
			try {
				if (con != null)
					con.rollback();
			} catch (SQLException sqle) {
				log.error("Failed due to", sqle);
			}
			throw new MessageException(e);
		} finally {
			try {
				if (con != null)
					con.setAutoCommit(true);
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (Exception e) {
				log.error("Failed due to", e);
			}
		}
	}

	public void delete(long message_id) throws MessageException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = connection.get();

			pstmt = con.prepareStatement(MSG_DELETE);
			pstmt.setLong(1, message_id);

			pstmt.executeUpdate();
		} catch (Exception e) {
			throw new MessageException(e);
		} finally {
			try {
				pstmt.close();
				con.close();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	public boolean save(String topic, String consumer) throws MessageException {
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			con = connection.get();

			pstmt = con.prepareStatement("insert into message_route (topic, consumer) values (?,?)");
			pstmt.setString(1, topic);
			pstmt.setString(2, consumer);

			int count = pstmt.executeUpdate();

			return count > 0 ? true : false;
		} catch (Exception e) {
			throw new MessageException(e);
		} finally {
			try {
				pstmt.close();
				con.close();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	public List<Route> routes() throws MessageException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Route> routes = new ArrayList<Route>();
		try {
			con = connection.get();

			pstmt = con.prepareStatement("select * from message_route order by topic");

			rs = pstmt.executeQuery();

			while (rs.next()) {
				routes.add(new Route(rs.getString("topic"), rs.getString("consumer")));
			}
			return routes;
		} catch (Exception e) {
			try {
				if (con != null)
					con.rollback();
			} catch (SQLException sqle) {
				log.error("Failed due to", sqle);
			}
			throw new MessageException(e);
		} finally {
			try {
				if (con != null)
					con.setAutoCommit(true);
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (Exception e) {
				log.error("Failed due to", e);
			}
		}
	}
}
