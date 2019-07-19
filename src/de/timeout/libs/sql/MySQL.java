package de.timeout.libs.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This Class is a Hook into the MySQL-Database
 * @author timeout
 *
 */
public class MySQL {

	private String host, database;
	private int port;
	
	private Connection connection;
	
	public MySQL(String host, int port, String database) {
		this.host = host;
		this.database = database;
		this.port = port;			
	}
	
	/**
	 * Connect to MySQL-Database, but needs an username and his password
	 * 
	 * @param username the username of the database
	 * @param password the password of the user
	 * @return a bool, if the hook is successfully connected
	 * @throws SQLException if there are unexpected errors
	 */
	public boolean connect(String username, String password) throws SQLException {
		if(!isConnected()) {
			// Bungeecord manage Driver-initialization -> not necessary
			// DriverManager.registerDriver(new Driver());
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useUnicode=true&useJDBCCompilantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", username, password);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks, if a coonection exists and is used. 
	 * If there is no connection, or the connection is closed, the method will return false.
	 * Else the method will return true.
	 * 
	 * @return is the connection can be used.
	 * @throws SQLException if there are unexpected errors
	 */
	public boolean isConnected() throws SQLException {
		return connection != null && !connection.isClosed();
	}
	
	/**
	 * return the actual connection
	 * @return the actual connection as Connection-Interface
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * Puts parametrt (argumeants) into the statement and returns the statement.
	 * 
	 * @param statement the statement
	 * @param args the arguments
	 * @return the converted statement
	 * @throws SQLException If there was an error.
	 */
	private PreparedStatement convertStatement(String statement, String[] args) throws SQLException {
		if(statement != null) {
			//Do not close this Statement here!!
			PreparedStatement ps = connection.prepareStatement(statement);
			for(int i = 0; i < args.length; i++) ps.setObject(i +1, args[i]);
			return ps;
		} else throw new IllegalArgumentException("Statement cannot be null");
	}
	
	/**
	 * Execute a Void-Statement. A void statement is a statement, that returns a bool instead of a table like INSERT, UPDATE or DELETE.
	 * 
	 * @param statement The statement
	 * @param variables the arguments in the right order. Variables in statements are displayed with '?'
	 * @return a bool, which contains the result
	 * @throws SQLException If there were an error in the MySQL-Statement
	 * @throws IllegalStateException if the connection is closed of not availiable (show {@link MySQL#isConnected()} for more informations)
	 */
	public boolean executeVoidStatement(String statement, String... variables) throws SQLException {
		if(!isConnected()) {
			return convertStatement(statement, variables).execute();
		} else throw new IllegalStateException("Connection is closed. Please connect to a MySQL-Database before using any statements");
	}
	
	/**
	 * Execute a statement and returns a table. This method is used with the "SELECT"-Statement.
	 * The return-type is a table which has columns and tuples. 
	 *  
	 * @param statement The statement
	 * @param variables the arguments in the right order. Variables in statements are displayred with '?'
	 * @return the table as table object
	 * @throws SQLException If there is an error in your MySQL-Statement
	 * @throws IllegalStateException if the connection is closed of not availiable (show {@link MySQL#isConnected()} for more informations)
	 */
	public Table executeStatement(String statement, String... variables) throws SQLException {
		if(!isConnected()) {
			return new Table(convertStatement(statement, variables).executeQuery());
		} else throw new IllegalStateException("Connection is closed. Please connect to a MySQL-Database before using any statements");
	}
	
	/**
	 * This class is an instance of a typical mysql table. A table contains columns and rows. 
	 * A row will be called tuple in this class.
	 * 
	 * @author Timeout
	 *
	 */
	public static class Table {
		
		private final List<Row> tuples = new ArrayList<>();
		
		private Column[] columns;
		
		public Table(ResultSet rs) throws SQLException {
			// define ColumnCount
			columns = new Column[rs.getMetaData().getColumnCount()];
			for(int i = 0; i < columns.length; i++) {
				// initialize columns
				columns[i] = new Column(rs.getMetaData().getColumnName(i));
			}
			
			// write values in columns and rows
			int index = 0;
			while(rs.next()) {
				String[] cache = new String[columns.length];
				for(int i = 0; i < columns.length; i++) {
					cache[i] = rs.getString(i);
					columns[i].addValue(cache[i]);
				}
				
				tuples.add(new Row(index, cache));
				index++;
			}
		}
		
		/**
		 * Returns a certain value in your table. 
		 * The name of the column and the index of the row must be given
		 * 
		 * @param columnName the name of the column
		 * @param index the index of the row. Beginning with 0
		 * @return the value on this location
		 * @throws IllegalArgumentException if the columnname cannot be found.
		 */
		public String getValue(String columnName, int index) {
			for(Column column : columns)
				if(column.getName().equalsIgnoreCase(columnName)) return column.getValue(index);
			
			throw new IllegalArgumentException("Could not find Column " + columnName);
		}
		
		/**
		 * Returns an entire row in the Database.
		 * @param index the index of the row. Beginning with 0
		 * @return the entire row
		 */
		public Row getRow(int index) {
			int length = columns.length;
			String[] values = new String[length];
			String[] columnNames = new String[length];
			
			for(int i = 0; i < length; i++) {
				columnNames[i] = columns[i].getName();
				values[i] = columns[i].getValue(index);
			}
			
			return new Row(index, columnNames, values);
		}
		
		/**
		 * Checks if the table is empty
		 * @return the result
		 */
		public boolean isEmpty() {
			return tuples.isEmpty();
		}
	}
	
	/**
	 * This is a column of the table. A column has a name and values.
	 * 
	 * @author Timeout
	 *
	 */
	public static class Column {
		
		private final List<String> values = new ArrayList<>();
		
		private String name;
		
		public Column(String name) {
			this.name = name;
		}
		
		/**
		 * Returns the name of the column
		 * @return the name of the column
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Returns the value at a certain index. 
		 * @param the row of the value. Beginning with 0
		 * @return the value
		 */
		public String getValue(int index) {
			return index < values.size() ? values.get(index) : null;
		}
		
		private void addValue(String element) {
			values.add(element);
		}
	}
	
	/**
	 * This class represents a complete row in the table
	 * 
	 * @author timeout
	 *
	 */
	public static class Row {
		
		private int index;
		private String[] columnNames;
		private String[] values;
		
		public Row(int i, String[] columnNames, String... elements) {
			this.index = i;
			this.values = elements.clone();
			this.columnNames = columnNames.clone();
		}
		
		/**
		 * Returns the index of the row in the table.
		 * @return the index of the row. Beginning with 0
		 */
		public int getIndex() {
			return index;
		}
		
		/**
		 * Returns the value of a column
		 * @return the value as String.
		 */
		public String getValue(int column) {
			return values[column];
		}
				
		/**
		 * Returns a value of a column
		 * @param colomnName the name of the column
		 * @return the value as string
		 * 
		 * @throws IllegalArgumentException if columnName is null
		 * @throws IllegalStateException if the column cannot be found
		 */
		public String getValue(String columnName) {
			if(columnName != null) {
				int i;
				for(i = 0; i < columnNames.length; i++) {
					if(!columnName.equalsIgnoreCase(columnNames[i]) && (i == columnNames.length -1)) {
						throw new IllegalStateException("Cannot find Column " + columnName);
					} else if(columnName.equalsIgnoreCase(columnName)) break;
				}
				
				return values[i];
			} else throw new IllegalArgumentException("columnName cannot be null");
		}
		
		/**
		 * returns all values in the column sort by index
		 * @return all values as list
		 */
		public List<String> getTupleValues() {
			return Arrays.asList(values);
		}
	}

}