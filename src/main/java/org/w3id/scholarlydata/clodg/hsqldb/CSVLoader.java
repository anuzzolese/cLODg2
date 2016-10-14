package org.w3id.scholarlydata.clodg.hsqldb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

public class CSVLoader {
	private static final
		String SQL_INSERT = "INSERT INTO ${table}(${keys}) VALUES(${values})";
	private static final
		String SQL_CREATE = "CREATE TABLE ${table} (${fields})";
	
	private static final String TABLE_REGEX = "\\$\\{table\\}";
	private static final String KEYS_REGEX = "\\$\\{keys\\}";
	private static final String VALUES_REGEX = "\\$\\{values\\}";
	private static final String FIELDS_REGEX = "\\$\\{fields\\}";
	private Connection connection;
	private char seprator;
	
	private String dbName;
	
	public CSVLoader(String dbName) {
		
		
		this.dbName = dbName;
		File dbFolder = new File(dbName).getParentFile();
		if(dbFolder.exists())
			try {
				FileUtils.forceDelete(dbFolder);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			
			this.connection = DriverManager.getConnection("jdbc:hsqldb:file:" + dbName, "SA", "");
			
			Statement st = connection.createStatement();
			st.execute("SET DATABASE SQL SYNTAX MYS TRUE");
			
			String expression = "CREATE FUNCTION SHA1(in VARCHAR(100000))"
					+ "RETURNS VARCHAR(100000) "
					+ "LANGUAGE JAVA DETERMINISTIC NO SQL "
					+ "EXTERNAL NAME 'CLASSPATH:org.apache.commons.codec.digest.DigestUtils.shaHex'";
			st.execute(expression);
			
			this.seprator = ',';
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Public constructor to build CSVLoader object with
	 * Connection details. The connection is closed on success
	 * or failure.
	 * @param connection
	 */
	public CSVLoader(Connection connection) {
		this.connection = connection;
		//Set default separator
		this.seprator = ',';
	}
	/**
	 * Parse CSV file using OpenCSV library and load in
	 * given database table.
	 * @param csvFile Input CSV file
	 * @param tableName Database table name to import data
	 * @param truncateBeforeLoad Truncate the table before inserting
	 * 			new records.
	 * @throws Exception
	 */
	public void loadCSV(File csvFile) throws Exception {
		CSVReader csvReader = null;
		if(null == this.connection) {
			throw new Exception("Not a valid connection.");
		}
		try {
			csvReader = new CSVReader(new FileReader(csvFile), this.seprator);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error occured while executing file. "
					+ e.getMessage());
		}
		String[] headerRow = csvReader.readNext();
		if (null == headerRow) {
			throw new FileNotFoundException(
					"No columns defined in given CSV file." +
					"Please check the CSV file format.");
		}
		
		for(int i=0; i<headerRow.length; i++){
			headerRow[i] = "`" + headerRow[i] + "`";
			//headerRow[i] = headerRow[i].replaceAll(" ", "_");
		}
		
		String tableName = csvFile.getName().replace(".csv", "");
		createSchema(tableName, headerRow);
		
		String questionmarks = StringUtils.repeat("?,", headerRow.length);
		questionmarks = (String) questionmarks.subSequence(0, questionmarks
				.length() - 1);
		String query = SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
		query = query
				.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
		query = query.replaceFirst(VALUES_REGEX, questionmarks);
		
		String[] nextLine;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = this.connection;
			con.setAutoCommit(false);
			
			ps = con.prepareStatement(query);
			
			final int batchSize = 1000;
			int count = 0;
			int rowCount = 0;
			while ((nextLine = csvReader.readNext()) != null) {
				rowCount++;
				
				if (null != nextLine) {
					int index = 1;
					for (String string : nextLine) {
						/*date = DateUti.convertToDate(string);
						if (null != date) {
							ps.setDate(index++, new java.sql.Date(date
									.getTime()));
						} else {
						}
						*/
						ps.setString(index++, string);
					}
					ps.addBatch();
				}
				if (++count % batchSize == 0) {
					ps.executeBatch();
				}
			}
			ps.executeBatch(); // insert remaining records
			con.commit();
		} catch (Exception e) {
			con.rollback();
			e.printStackTrace();
			throw new Exception(
					"Error occured while loading data from file to database."
							+ e.getMessage());
		} finally {
			if (null != ps)
				ps.close();
			csvReader.close();
		}
	}
	
	public void loadCSV(File...csvFiles) throws Exception {
		for(File csvFile : csvFiles)
			loadCSV(csvFile);
	}
	
	public char getSeprator() {
		return seprator;
	}
	public void setSeprator(char seprator) {
		this.seprator = seprator;
	}
	
	private void createSchema(String table, String[] header){
		
		StringBuilder sb = new StringBuilder();
		
		for(String field : header){
			if(sb.length() > 0) sb.append(", ");
			//sb.append("\"");
			sb.append(field);
			//sb.append("\"");
			sb.append(" VARCHAR(1000000)");
		}
		
		String query = SQL_CREATE.replaceFirst(TABLE_REGEX, table);
		query = query.replaceFirst(FIELDS_REGEX, sb.toString());
		
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = this.connection;
			con.setAutoCommit(false);
			ps = con.prepareStatement(query);
			
			final int batchSize = 1000;
			int count = 0;
			//Date date = null;
			
			ps.executeUpdate();
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	public void dropDatabase() throws SQLException{
		Statement st = null;

        st = connection.createStatement();    // statements
        
        String expression = "DROP SCHEMA PUBLIC CASCADE";

        int i = st.executeUpdate(expression);    // run the query

        if (i == -1) {
            System.out.println("db error : " + expression);
        }

        st.close();
        
        connection.close();
        
        try {
			FileUtils.forceDelete(new File(dbName).getParentFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Connection getConnection() {
		return connection;
	}
}