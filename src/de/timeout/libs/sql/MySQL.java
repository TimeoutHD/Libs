package de.timeout.libs.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * This Class is a Hook into the MySQL-Database
 * @author Timeout
 *
 */
public class MySQL {
	
	private static final Executor executor = Executors.newFixedThreadPool(3);
	
	private String host;
	private String database;
	private int port;
	
	private Connection connection;
	
	/**
	 * Creates a new hook for accessing MySQL
	 * 
	 * @param host the hostname of your MySQL-Server
	 * @param port the port of your MySQL-Server
	 * @param database the database you want to use
	 */
	public MySQL(@NotNull String host, int port, @NotNull String database) {
		// Validate
		Validate.notEmpty(host, "Hostname can neither be null nor empty");
		Validate.isTrue(port > 0, "Port must be a positive number greater than zero");
		Validate.notEmpty(database, "Database can neither be null nor empty");
		
		this.host = host;
		this.database = database;
		this.port = port;			
	}
	
	/**
	 * Connect to MySQL-Database, but requires an username and his password
	 * 
	 * @param username the username of the database
	 * @param password the password of the user
	 * @return a boolean if the hook is successfully connected
	 * @throws SQLException if there were unexpected errors during the connection
	 */
	public boolean connect(@NotNull String username, @Nullable String password) throws SQLException {
		// create properties 
		MysqlDataSource properties = new MysqlDataSource();
		properties.setAutoReconnect(true);
		properties.setUseOldAliasMetadataBehavior(true);
		properties.setUseUnicode(true);
		properties.setUseLegacyDatetimeCode(false);
		properties.setServerTimezone("UTC");
		properties.setUseSSL(true);
		properties.setCreateDatabaseIfNotExist(true);
		
		// connect to server
		return connect(username, password, properties);
	}
	
	/**
	 * Connect to MySQL-Database with custom settings. 
	 * 
	 * @param username the username you want to use. Cannot be null
	 * @param password the password of the user
	 * @param properties your customized password
	 * @return a boolean if the hook is successfully connected
	 * @throws SQLException
	 */
	public boolean connect(
			@NotNull String username, 
			@Nullable String password,
			@NotNull MysqlDataSource properties) throws SQLException {
		// Validate
		Validate.notEmpty(username, "Username can neither be null nor empty");
		Validate.notNull(properties, "Properties cannot be null");
		
		// fill with data
		properties.setUrl(host);
		properties.setPort(port);
		properties.setDatabaseName(database);
		
		// connect to server
		connection = properties.getConnection(username, password);
		
		return connection != null;
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
	 * return the current connection
	 * 
	 * @return the current connection 
	 * 	Is null before {@link MySQL#connect(String, String, MysqlDataSource)} or {@link MySQL#connect(String, String)} was triggered
	 */
	@Nullable
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * Creates a new PreparedStatement with parameters / arguments. 
	 * 
	 * @param statement the statement. Cannot be null
	 * @param args the arguments you want to use
	 * @return the prepared statement. Cannot be null
	 * @throws SQLException If there was an error while converting the string and the arguments into a prepared statement
	 * @throws IllegalArgumentException if parameter statement is null
	 */
	@NotNull
	public PreparedStatement createStatement(@NotNull String statement, Object[] args) throws SQLException {
		// Validate
		Validate.notEmpty(statement, "Statement can neither be null nor empty");

		// Create Statement
		PreparedStatement ps = connection.prepareStatement(statement);
		for(int i = 0; i < args.length; i++) ps.setString(i + 1, args[i].toString());
		return ps;
	}
	
	/**
	 * Executes an asynchronous query statement
	 * 
	 * @param statement the statement you want to execute. Cannot be null
	 * @param query a function which will be executed after the bridge received a ResultSet answer of the statement
	 * @throws IllegalArgumentException if the statement is null
	 */
	public void executeAsyncQuery(@NotNull PreparedStatement statement, @Nullable Consumer<ResultSet> query) {
		// Validate
		Validate.notNull(statement, "Statement cannot be null");
		
		// start async runnable
		CompletableFuture.runAsync(() -> {
			// execute statement
			try(ResultSet rs = statement.executeQuery()) {
				// apply function
				if(query != null) query.accept(rs);
			} catch(SQLException e) {
				Logger.getGlobal().log(Level.WARNING, "Unable to execute SQL-Statement", e);
			}
		}, executor);
	}
	
	/**
	 * Executes an asynchronous update / delete / insert statement 
	 * 
	 * @param statement the statement you want to execute. Cannot be null
	 * @param result a function which will be executed after the bridge received a boolean answer of the statement
	 * @throws IllegalArgumentException if the statement is null
	 */
	public void executeAsync(@NotNull PreparedStatement statement, @Nullable Consumer<Boolean> result) {
		// Validate
		Validate.notNull(statement, "Statement cannot be null");
		
		// start async runnable
		CompletableFuture.runAsync(() -> {
			// execute statement
			try {
				// get result
				boolean res = statement.execute();
				
				// apply function if function exists
				if(result != null) result.accept(res);
			} catch (SQLException e) {
				Logger.getGlobal().log(Level.WARNING, "Unable to execute SQL-Statement", e);
			}
		}, executor);
	}
	
	/**
	 * Executes an asynchronous update / delete / insert statement
	 * @param statement the statement you want to execute. Cannot be null
	 * @throws IllegalArgumentException if the statement is null
	 */
	public void executeAsync(@NotNull PreparedStatement statement) {
		executeAsync(statement, null);
	}
}
