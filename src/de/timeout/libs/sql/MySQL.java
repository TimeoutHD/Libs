package de.timeout.libs.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

/**
 * This Class is a Hook into the MySQL-Database
 * @author timeout
 *
 */
public class MySQL {
	
	private static final Executor executor = Executors.newFixedThreadPool(3);
	
	private String host;
	private String database;
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
			// Bungeecord / Bukkit manage Driver-initialization -> not necessary. Only necessary when you use this outside the Bukkit / Bungecord API
			// DriverManager.registerDriver(new Driver());
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database
					+ "?autoReconnect=true"
					+ "&useOldAliasMetadataBehavior=true"
					+ "&useUnicode=true"
					+ "&useJDBCCompilantTimezoneShift=true"
					+ "&useLegacyDatetimeCode=false"
					+ "&serverTimezone=UTC"
					+ "&useSSL=false",
				username, password);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks, if a connection exists and is used. 
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
	 * Disconnect from a MySQL-Database if this object is connected
	 * 
	 * @return the result. true if the object is disconnected successful, false if the object isn't connected at all
	 * @throws SQLException if there are unexpected errors
	 */
	public boolean disconnect() throws SQLException {
		if(isConnected()) {
			this.connection.close();
			return true;
		}
		return false;
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
	private PreparedStatement convertStatement(String statement, Object[] args) throws SQLException {
		if(statement != null) {
			//Do not close this Statement here!!
			PreparedStatement ps = connection.prepareStatement(statement);
			for(int i = 0; i < args.length; i++) ps.setString(i +1, args[i].toString());
			return ps;
		} else throw new IllegalArgumentException("Statement cannot be null");
	}
	
	/**
	 * Executes a Void-Statement. A void statement is a statement, that returns a bool instead of a table like INSERT, UPDATE or DELETE.
	 * 
	 * @param statement The statement.
	 * @param variables the arguments in the right order. Variables in statements are displayed with '?'
	 * @return a bool which contains the result
	 * @throws TimeoutException if the connection to your database timed out
	 * @throws SQLException if there were an error in the MySQL-Statement
	 */
	public boolean executeVoidStatement(String statement, Object... variables) throws TimeoutException, SQLException {
		try {
			return executeFutureVoidStatement(statement, variables).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Logger.getGlobal().log(Level.SEVERE, "Cannot execute Void-Statement. Thread interrupted", e);
		} catch (ExecutionException e) {
			Logger.getGlobal().log(Level.WARNING, "Unhandled exception while executing void statement", e);
		}
		return false;
	}
	
	/**
	 * Execute a statement and returns a table. This method is used with the "SELECT"-Statement.
	 * The return-type is a table which has columns and tuples. 
	 *  
	 * @param statement The statement
	 * @param variables the arguments in the right order. Variables in statements are displayred with '?'
	 * @return the table as table object or null if there were an error
	 * @throws SQLException If there is an error in your MySQL-Statement
	 * @throws TimeoutException if the connection to the database timed out
	 * @throws IllegalStateException if the connection is closed of not availiable (show {@link MySQL#isConnected()} for more informations)
	 */
	public Table executeStatement(String statement, Object... variables) throws TimeoutException, SQLException {
		try {
			return executeFutureStatement(statement, variables).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Logger.getGlobal().log(Level.SEVERE, "Fatal error while executing statement. Thread interrupted", e);
		} catch (ExecutionException e) {
			Logger.getGlobal().log(Level.WARNING, "Unhandled exception while executing statement", e);
		}
		return null;
	}
	
	/**
	 * Execute a Void-Statement. A void statement is a statement, that returns a bool instead of a table like INSERT, UPDATE or DELETE.
	 * 
	 * @param statement The statement
	 * @param variables the arguments in the right order. Variables in statements are displayed with '?'
	 * @return a Future with a bool which contains the result
	 * @throws SQLException If there were an error in the MySQL-Statement
	 * @throws IllegalStateException if the connection is closed of not availiable (show {@link MySQL#isConnected()} for more informations)
	 */
	public Future<Boolean> executeFutureVoidStatement(String statement, Object... variables) throws SQLException {
		if(isConnected()) {
			return CompletableFuture.supplyAsync(() -> {
					try {
						return convertStatement(statement, variables).execute();
					} catch (SQLException e) {
						throw new CompletionException(e);
					}
			}, executor);
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
	public Future<Table> executeFutureStatement(String statement, Object... variables) throws SQLException {
		if(isConnected()) {
			return CompletableFuture.supplyAsync(() -> {
				try {
					return new Table(convertStatement(statement, variables).executeQuery());
				} catch (SQLException e) {
					throw new CompletionException(e);
				}
			}, executor);
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
		
		private final List<Row> rows = new ArrayList<>();
		
		private Column[] columns;
		
		public Table(ResultSet rs) throws SQLException {
			// define ColumnCount
			columns = new Column[rs.getMetaData().getColumnCount()];
			// define ColumnNames
			String[] columnNames = new String[columns.length];
			for(int i = 0; i < columns.length; i++) {
				// initialize columns
				columns[i] = new Column(rs.getMetaData().getColumnName(i +1));
				columnNames[i] = rs.getMetaData().getColumnLabel(i + 1);
			}
			
			// write values in columns and rows
			int index = 0;
			while(rs.next()) {
				String[] cache = new String[columns.length];
				for(int i = 0; i < columns.length; i++) {
					cache[i] = rs.getString(i +1);
					columns[i].addValue(cache[i]);
				}
				
				rows.add(new Row(index, columnNames, cache));
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
			
			throw new IllegalArgumentException("Could not find Column " + columnName + ". Valid columns are " + getColumnNames());
		}
		
		/**
		 * This Method returns a line of valid Column-Names
		 * @return the columnnames
		 */
		private String getColumnNames() {
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < columns.length -1; i++) builder.append(columns[i].getName() + ", ");
			builder.append(columns[columns.length -1].getName());
			return builder.toString();
		}
		
		/**
		 * Returns a certain value in your table
		 * Both indices must be given and begins with 0
		 * 
		 * @param columnIndex the index of the column begins with 0
		 * @param rowIndex the index of the row begins with 0
		 * @return the value on this location
		 * @throws ArrayIndexOutOfBoundsException if the columnIndex is greater than the actual amount of columns
		 */
		public String getValue(int columnIndex, int rowIndex) {
			if(columnIndex < columns.length) {
				return columns[columnIndex].getValue(rowIndex);
			} else throw new ArrayIndexOutOfBoundsException("ColummIndex is greater than actual columns");
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
		 * Returns all Rows of this Table
		 * @return rows as ArrayList
		 */
		public List<Row> getRows() {
			return new ArrayList<>(rows);
		}
		
		/**
		 * Returns all Columns of this Table
		 * @return all Columns as Column Array
		 */
		public Column[] getColumns() {
			return columns.clone();
		}
		
		/**
		 * Checks if the table is empty
		 * @return the result
		 */
		public boolean isEmpty() {
			return rows.isEmpty();
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
			// validate
			Validate.notNull(columnName, "columnName cannot be null");
			// for every column in row
			for(int i = 0; i < columnNames.length; i++) {
				// if column is found return value
				if(columnName.equals(columnNames[i])) return values[i];
			}
			// column cannot be found
			throw new IllegalStateException("Cannot find Column " + columnName);
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
