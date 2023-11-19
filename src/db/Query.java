package db;

import java.sql.*;

/*
 * DB 쉽게 쓰기 위해 만든 클래스.
 * getResultSet -> 반환값이 ResultSet 형식으로 나올 경우
 * execute -> 반환값 따로 없을경우 
 */

public class Query 
{
	private static final String MYSQL_USERNAME = "root";
	private static final String MYSQL_PASSWORD = "zoo@123456";

	private static Connection conn = null;
	
	public static ResultSet getResultSet(String sql, int parameter_num, String[] parameters)
	{
		ResultSet result = null;
		try
		{
			conn = DriverManager.getConnection
			(
					"jdbc:mysql://localhost:3306/HalliGalli",
					MYSQL_USERNAME,
					MYSQL_PASSWORD
					);
			PreparedStatement ppmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			for (int i=0; i<parameter_num; i++)
				ppmt.setString(i+1, parameters[i]);
			result = ppmt.executeQuery();
		}
		catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static ResultSet getLikeResultSet(String sql, int parameter_num, String[] parameters)
	{
		ResultSet result = null;
		try
		{
			conn = DriverManager.getConnection
			(
					"jdbc:mysql://localhost:3306/HalliGalli",
					MYSQL_USERNAME,
					MYSQL_PASSWORD
					);
			PreparedStatement ppmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			for (int i=0; i<parameter_num; i++)
				ppmt.setString(i+1, "%"+parameters[i]+"%");
			result = ppmt.executeQuery();
		}
		catch (Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static boolean execute(String sql, int parameter_num, String[] parameters)
	{
		Connection conn = null;
		int result = 0;
		try
		{
			conn = DriverManager.getConnection
			(
					"jdbc:mysql://localhost:3306/HalliGalli",
					MYSQL_USERNAME,
					MYSQL_PASSWORD
					);
			PreparedStatement ppmt = conn.prepareStatement(sql);
			for (int i=0; i<parameter_num; i++)
				ppmt.setString(i+1, parameters[i]);
			result = ppmt.executeUpdate();
		}
		catch (Exception e) {e.printStackTrace();}
		if (result == 1) return true;
		else return false;
	}
	
	public static void close()
	{
		if (conn != null)
		{
			try {conn.close();}
			catch (SQLException e) {e.printStackTrace();}
		}
	}
}
