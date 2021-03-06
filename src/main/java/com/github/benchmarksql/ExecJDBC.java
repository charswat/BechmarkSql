package com.github.benchmarksql;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.github.benchmarksql.jtpcc.jTPCCUtil;

/**
 * Command line program to process SQL DDL statements, from a text input file,
 * to any JDBC Data Source.
 * 
 * @author Denis Lussier - 2004-2014
 */
public class ExecJDBC {

	public static void main(String[] args) {

		Connection conn = null;
		Statement stmt = null;
		String rLine = null;
		StringBuffer sql = new StringBuffer();

		try {

			Properties ini = new Properties();
			if (System.getProperty("prop") == null) {
				throw new Exception("ERROR: Properties file is invalid.");
			}
			ini.load(new FileInputStream(System.getProperty("prop")));

			// Register jdbcDriver
			Class.forName(ini.getProperty("driver"));

			// make connection
			conn = DriverManager.getConnection(ini.getProperty("conn"), ini.getProperty("user"),
					ini.getProperty("password"));
			conn.setAutoCommit(true);

			// Create Statement
			stmt = conn.createStatement();

			// Open inputFile
			if (jTPCCUtil.getSysProp("commandFile", null) == null) {
				throw new Exception("ERROR: Invalid SQL script.");
			}
			BufferedReader in = new BufferedReader(new FileReader(jTPCCUtil.getSysProp("commandFile", null)));

			// loop thru input file and concatenate SQL statement fragments
			while ((rLine = in.readLine()) != null) {

				String line = rLine.trim();

				if (line.length() != 0) {
					if (line.startsWith("--")) {
						System.out.println(line); // print comment line
					} else {
						sql.append(line);
						if (line.endsWith(";")) {
							execJDBC(stmt, sql);
							sql = new StringBuffer();
						} else {
							sql.append("\n");
						}
					}

				} // end if

			} // end while

			in.close();

		} catch (IOException ie) {
			System.out.println(ie.getMessage());

		} catch (SQLException se) {
			System.out.println(se.getMessage());

		} catch (Exception e) {
			e.printStackTrace();

			// exit Cleanly
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally

		} // end try

	} // end main

	static void execJDBC(Statement stmt, StringBuffer sql) {

		System.out.println(sql);

		try {

			stmt.execute(sql.toString().replace(';', ' '));

		} catch (SQLException se) {
			System.out.println(se.getMessage());
		} // end try

	} // end execJDBCCommand

} // end ExecJDBC Class
