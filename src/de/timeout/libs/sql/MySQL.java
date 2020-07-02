package de.timeout.libs.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
			connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s"
					+ "?autoReconnect=true"
					+ "&useOldAliasMetadataBehavior=true"
					+ "&useUnicode=true"
					+ "&useJDBCCompilantTimezoneShift=true"
					+ "&useLegacyDatetimeCode=false"
					+ "&serverTimezone=UTC"
					+ "&useSSL=false", host, port, database),
			username, password);
			return connection != null;
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
	 * @return the result. true if the object is disconnected successful, false if the connection is still open
	 * @throws SQLException if there are unexpected errors
	 */
	public boolean disconnect() throws SQLException {
		this.connection.close();
		return !isConnected();
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
	public ResultSet executeStatement(String statement, Object... variables) throws TimeoutException, SQLException {
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
					try(PreparedStatement prepStatement = convertStatement(statement, variables)) {
						return prepStatement.execute();
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
	public Future<ResultSet> executeFutureStatement(String statement, Object... variables) throws SQLException {
		if(isConnected()) {
			return CompletableFuture.supplyAsync(() -> {
				try(PreparedStatement prepStatement = convertStatement(statement, variables)) {
					return prepStatement.executeQuery();
				} catch (SQLException e) {
					throw new CompletionException(e);
				}
			}, executor);
		} else throw new IllegalStateException("Connection is closed. Please connect to a MySQL-Database before using any statements");
	}
}
