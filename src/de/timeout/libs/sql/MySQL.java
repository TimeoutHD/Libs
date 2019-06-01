package de.timeout.libs.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mysql.jdbc.Driver;

/**
 * Diese Klasse dient als Verbindung zur MySQL-Datenbank.
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
	 * Verbindet zur MySQL-Datenbank, benötigt jedoch einen Benutzernamen und das dazugehörige Passwort.
	 * 
	 * @param username Der Benutzername der Programms
	 * @param password Das dazugehörige Password
	 * @return Das Resultat, ob es verbunden ist oder nicht.
	 * @throws SQLException Wenn es einen unerwarteten Fehler gibt
	 */
	public boolean connect(String username, String password) throws SQLException {
		if(!isConnected()) {
			DriverManager.registerDriver(new Driver());
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useUnicode=true&useJDBCCompilantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", username, password);
			return true;
		}
		return false;
	}
	
	/**
	 * Prüft, ob eine Verbindung genutzt werden kann. Wenn keine Verbindung besteht oder die Verbindung geschlossen ist,
	 * ist das Ergebnis false. Ansonsten kann die Verbindung genutzt werden.
	 * @return
	 * @throws SQLException
	 */
	public boolean isConnected() throws SQLException {
		return connection != null && !connection.isClosed();
	}
	
	/**
	 * Gibt die aktuelle Verbindung zurück. 
	 * @return die aktuelle Verbindung als Connection-Interface
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * Fügt die Parameter (Argumente) in das Statement ein und gibt dieses zurück
	 * @param statement das Statement
	 * @param args die Argumente
	 * @return Das konvertierte Statement
	 * @throws SQLException Wenn es ein Fehler bei der Konvertierung gab
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
	 * Führt ein Void-Statement aus. Ein Void-Statement ist ein Statement, dass nur eine Erfolgsbestätigung sendet, anstatt
	 * einer Relation. Diese Methode ist nützlich, wenn man die Befehle INSERT, UPDATE oder DELETE ausführen möchte.
	 * 
	 * @param statement Das Statement
	 * @param variables Die Variablen in der richtigen Reihenfolge. Variablen können im Statement mit '?' erstellt werden.
	 * @return die Erfolgsbestätigung als Boolean.
	 * @throws SQLException Wenn im Syntax des MySQL-Statments ein Fehler vorliegt.
	 * @throws IllegalStateException Wenn die Verbindung geschlossen oder nicht nutzbar ist, quasi wenn {@link MySQL#isConnected()} false zurückgibt.
	 */
	public boolean executeVoidStatement(String statement, String... variables) throws SQLException {
		if(!isConnected()) {
			return convertStatement(statement, variables).execute();
		} else throw new IllegalStateException("Connection is closed. Please connect to a MySQL-Database before using any statements");
	}
	
	/**
	 * Führt ein Statement aus und gibt eine geeignete Relation zurück. Diese Methode ist hauptsächlich beim Befehl SELECT sinnvoll.
	 * Das zurückgegebene Objekt ist eine Table, die verschiedene Spalten (Columns) und Zeilen (Tuples) hat. Ein Datensatz wird
	 * in dieser Klasse Element genannt. 
	 *  
	 * @param statement Das MySQL-Statement
	 * @param variables Die Variablen in der richtigen Reihenfolge. Variablem können im Statement mit '?' erstellt werden
	 * @return Die Relation als Table-Objekt.
	 * @throws SQLException Wenn im Syntax des MySQL-Statments ein Fehler vorliegt.
	 * @throws IllegalStateException Wenn die Verbindung geschlossen oder nicht nutzbar ist, quasi wenn {@link MySQL#isConnected()} false zurückgibt.
	 */
	public Table executeStatement(String statement, String... variables) throws SQLException {
		if(!isConnected()) {
			return new Table(convertStatement(statement, variables).executeQuery());
		} else throw new IllegalStateException("Connection is closed. Please connect to a MySQL-Database before using any statements");
	}
	
	/**
	 * Diese Klasse ist eine Instanz einer MySQL-Relation. Eine Relation besteht aus Attributen (Columns) und 
	 * Zeilen (Rows). Ein Row wird in dieser Klasse Tuple genannt. Die Tuple ist wiederum in Elemente (Datensätze) unterteilt.  
	 * @author Timeout
	 *
	 */
	public static class Table {
		
		private final List<Tuple> tuples = new ArrayList<>();
		
		private Column[] columns;
		
		public Table(ResultSet rs) throws SQLException {
			// Columnanzahl definieren.
			columns = new Column[rs.getMetaData().getColumnCount()];
			for(int i = 0; i < columns.length; i++) {
				// Columns initialisieren
				columns[i] = new Column(rs.getMetaData().getColumnName(i));
			}
			
			// Werte in Tuplen und Columns speichern
			int index = 0;
			while(rs.next()) {
				String[] cache = new String[columns.length];
				for(int i = 0; i < columns.length; i++) {
					cache[i] = rs.getString(i);
					columns[i].addValue(cache[i]);
				}
				
				tuples.add(new Tuple(index, cache));
				index++;
			}
		}
		
		/**
		 * Gibt einen bestimmten Wert aus der Relation zurück. Dabei muss der Attributname (Spaltenname) und die Zeile
		 * angegeben werden. 
		 * 
		 * @param columnName Der Attributname (Spaltenname) 
		 * @param index Die Zeilenzahl
		 * @return Das Element (Die Zelle) der Relation an der Stelle
		 */
		public String getValue(String columnName, int index) {
			for(Column column : columns)
				if(column.getName().equalsIgnoreCase(columnName)) return column.getValue(index);
			
			throw new IllegalArgumentException("Could not find Column " + columnName);
		}
		
		/**
		 * Gibt eine Spalte (Tuple) der Relation zurück. Dabei ist der Index von 0 aus zu zählen.
		 * @param index der Index
		 * @return Die komplette Spalte
		 */
		public Tuple getTuple(int index) {
			int length = columns.length;
			String[] values = new String[length];
			String[] columnNames = new String[length];
			
			for(int i = 0; i < length; i++) {
				columnNames[i] = columns[i].getName();
				values[i] = columns[i].getValue(index);
			}
			
			return new Tuple(index, columnNames, values);
		}
		
		/**
		 * Gibt zurück, ob eine Relation leer ist
		 * @return Ob die Relation leer ist als Boolean
		 */
		public boolean isEmpty() {
			return tuples.isEmpty();
		}
	}
	
	/**
	 * Das ist ein Attribut (eine Spalte) der Relation. Eine Spalte hat einen Namen und mehrere Attributwerte.
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
		 * Gibt den Namen des Attributs zurück
		 * @return den Attributnamen
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Gibt den Attributwert an einer bestimmten Stelle zurück. Dafür muss eine Zeile angegeben werden.
		 * @param index die Zeile, wo sich der gesuchte Attributwert befindet
		 * @return Der Attributwert als String.
		 */
		public String getValue(int index) {
			return index < values.size() ? values.get(index) : null;
		}
		
		public void addValue(String element) {
			values.add(element);
		}
	}
	
	/**
	 * Diese Klasse vertritt eine gesamte Zeile der Relation.
	 * @author timeout
	 *
	 */
	public static class Tuple {
		
		private int index;
		private String[] columnNames;
		private String[] values;
		
		public Tuple(int i, String[] columnNames, String... elements) {
			this.index = i;
			this.values = elements.clone();
			this.columnNames = columnNames.clone();
		}
		
		/**
		 * Gibt die Zeilenzahl der Tuple zurück (Wo sich die Zeile befindet)
		 * @return die Zeilenzahl
		 */
		public int getIndex() {
			return index;
		}
		
		/**
		 * Gibt den Attributwert an einem Attrubut (einer Spalte) zurück
		 * @param column die Zahl der Spalte
		 * @return Der Attributwert als String.
		 */
		public String getElement(int column) {
			return values[column];
		}
				
		/**
		 * Gibt den Attrubutwert an einem Attrubut (einer Spalte) zurück. Diese Methode ignoriert Case
		 * @param columnName der Name des Attributs.
		 * @return Der Attributwert als String.
		 * 
		 * @throws IllegalArgumentException Wenn columnName null ist.
		 * @throws IllegalStateException Wenn der gesuchte columnName nicht gefunden werden kann
		 */
		public String getElement(String columnName) {
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
		 * Gibt alle Attributwerte als Liste zurück. Sortiert nach der richtigen Reihenfolge der Attribute in der Relation
		 * @return alle Attribute als Liste.
		 */
		public List<String> getTupleValues() {
			return Arrays.asList(values);
		}
	}

}
