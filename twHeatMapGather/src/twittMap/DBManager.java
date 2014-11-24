package twittMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
	Connection conn;
	Statement stmt;
	String url;
	String user;
	String password;
	String sql;
	ResultSet rs;
	String queryFormat = "(select sLatitude, sLongitude from status "
			+ "where sDate>\'%s\' and sDate<\'%s\') " + "union "
			+ "(select sLatitude, sLongitude from status "
			+ "where sDate='%s' and sTime>='%s') " + "union "
			+ "(select sLatitude, sLongitude from status "
			+ "where sDate='%s' and sTime<='%s')";
	String queryFormatSameDate = "select sLatitude, sLongitude from status "
			+ "where sDate=\'%s\' and sTime between \'%s\' and \'%s\' ";

	public DBManager() {
		conn = null;
		stmt = null;
		url = null;
		user = null;
		password = null;
		sql = null;
		rs = null;
	}

	public void getDirver() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("load driver error");
			e.printStackTrace();
		}
	}

	public void connect() {
		try {
			url = "jdbc:mysql://localhost:3306/twittmap";
			user = "root";
			password = "";
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			System.out.println("connect error");
			e.printStackTrace();
		}
	}

	public void connectAWS() {
		try {
			url = "jdbc:mysql://twittmap.czbkhl5yewet.ap-northeast-1.rds.amazonaws.com:3306/twittmap";
			user = "root";
			password = "roottoor";
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			System.out.println("connect error");
			e.printStackTrace();
		}
	}

	public void update(StreamStatus streamStatus) {
		long sId = streamStatus.sId;
		String sName = streamStatus.sName;
		String sText = streamStatus.sText;
		double sLatitude = streamStatus.sLatitude;
		double sLongitude = streamStatus.sLongitude;
		java.sql.Time sTime = new java.sql.Time(streamStatus.sTime.getTime());
		java.sql.Date sDate = new java.sql.Date(streamStatus.sTime.getTime());
		try {
			stmt = conn.createStatement();
			PreparedStatement updateStatus = null;
			sql = "insert into status values(?,?,?,?,?,?,?)";

			try {
				updateStatus = conn.prepareStatement(sql);
				updateStatus.setLong(1, sId);
				updateStatus.setString(2, sName);
				updateStatus.setDouble(3, sLatitude);
				updateStatus.setDouble(4, sLongitude);
				updateStatus.setString(5, sText);
				updateStatus.setTime(6, sTime);
				updateStatus.setDate(7, sDate);
				// updateStatus.setdat

				updateStatus.executeUpdate();
			} catch (SQLException e) {
				// System.out.println(sId + "||" +sText);
			} finally {
				updateStatus.close();
			}
		} catch (SQLException e) {
			System.out.println("sql error");
			e.printStackTrace();
		}
	}

	public String queryNum() {
		String out = null;
		try {
			stmt = conn.createStatement();
			sql = "select count(*) as num from status ";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				out = rs.getString("num") + "";
				// System.out.println(out);
			}

		} catch (SQLException e) {
			System.out.println("sql error");
			e.printStackTrace();
		}
		return out;
	}

	public void shutdown() {
		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			System.out.println("close db error");
			e.printStackTrace();
		}
	}

	public List<List<String>> queryLatLng(String begindate, String begintime,
			String enddate, String endtime) {
		List<List<String>> result = new ArrayList<List<String>>();
		try {
			stmt = conn.createStatement();
			if (begindate.equals(enddate))
				sql = String.format(queryFormatSameDate, begindate, begintime,
						endtime);
			else
				sql = String.format(queryFormat, begindate, enddate, begindate,
						begintime, enddate, endtime);
			System.out.println(sql);

			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				List<String> pair = new ArrayList<String>();
				pair.add(rs.getString("sLatitude"));
				pair.add(rs.getString("sLongitude"));
				result.add(pair);
			}
		} catch (SQLException e) {
			System.out.println("sql error");
			e.printStackTrace();
		}
		return result;
	}
	/*
	 * public static void main(String args[]) { DBManager ma = new DBManager();
	 * ma.getDirver(); ma.connectAWS(); ma.queryNum(); ma.shutdown(); }
	 */

}
