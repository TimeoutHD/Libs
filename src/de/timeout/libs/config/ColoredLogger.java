package de.timeout.libs.config;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;

/**
 * Extends the normal Bukkit Logger to write Colors
 * @author Timeout
 *
 */
public class ColoredLogger {

	private static final Logger LOGGER = Bukkit.getLogger();
	private static final String COLOR_PATTERN = "\u001b[38;5;%dm";
	private static final String FORMAT_PATTERN = "\u001b[%dm";
	
	private String prefix;
	private char colorFormatter = '&';
	
	/**
	 * Creates a new ColoredLogger
	 * @author Timeout
	 *
	 */
	public ColoredLogger() {
		this("");
	}
	
	/**
	 * Creates a new ColoredLogger
	 * @author Timeout
	 *
	 * @param prefix the prefix of the plugin with color char '&'
	 */
	public ColoredLogger(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * Creates a new ColoredLogger with a custom color char
	 * @author Timeout
	 *
	 * @param prefix the prefix of the plugin
	 * @param colorFormatter the color char which should replaced while converting
	 */
	public ColoredLogger(String prefix, char colorFormatter) {
		this.prefix = prefix;
		this.colorFormatter = colorFormatter;
	}
	
	/**
	 * Sets a new Prefix for this Logger
	 * @author Timeout
	 * 
	 * @param prefix the new prefix of the plugin
	 */
	public void setPrefix(String prefix) {
		this.prefix = convertStringMessage(prefix, colorFormatter);
	}
	
	/**
	 * Sets the ColorFormatter char
	 * @author Timeout
	 *
	 * @param colorFormatter the new Formatter
	 */
	public void setColorFormatter(char colorFormatter) {
		this.colorFormatter = colorFormatter;
	}
	
	/**
	 * Logs a message in console. See {@link Logger#log(Level, String)}
	 * The message will be converted in ColoredStrings with the prefix
	 * @author Timeout
	 * 
	 * @param level the level of the log
	 * @param message the message you want to show
	 */
	public void log(Level level, String message) {
		LOGGER.log(level, () -> prefix + convertStringMessage(message, colorFormatter));
	}
	
	/**
	 * Logs a message in console. See {@link Logger#log(Level, String, Throwable)}
	 * The message will be converted in colored strings with the chosen prefix
	 * @author Timeout
	 * 
	 * @param level the Level of the log
	 * @param message the message 
	 * @param e the exception 
	 */
	public void log(Level level, String message, Throwable e) {
		LOGGER.log(level, prefix + convertStringMessage(message, colorFormatter), e);
	}
	
	/**
	 * Converts a String with Minecraft-ColorCodes into Ansi-Colors.
	 * Returns null if the message is null
	 * @author Timeout
	 * 
	 * @param message the message. Can be null
	 * @param colorFormatter the formatter to translate messages
	 * @return the converted message or null if the message is null
	 */
	private static String convertStringMessage(String message, char colorFormatter) {
		// Continur if String is neither not null nor empty
		if(message != null && !message.isEmpty()) {
			// copy of string
			String messageCopy = String.copyValueOf(message.toCharArray());
			// run through message
			int index = 0;
			// while index is valid (max prelast char and not -1 for not found)
			while(index < (messageCopy.length() - 1) && (index = messageCopy.indexOf(index, colorFormatter)) != -1) {
				// get colorSet
				char code = message.charAt(index++);
				// check if a color exists
				ConsoleColor color = ConsoleColor.getColorByCode(code);
				if(color != null) {
					// replace all in color
					messageCopy = messageCopy.replace(String.valueOf(new char[] {colorFormatter, code} ), color.getAnsiColor());
				}
			}
			// return converted String
			return messageCopy;
		}
		// return message for nothing to compile
		return message;
	}
	
	/**
	 * Represents a set of Minecraft-ColorCodes and their ANSI-Codes
	 * @author Timeout
	 *
	 */
	private enum ConsoleColor {	
		
		BLACK('0', COLOR_PATTERN, 0),
		DARK_GREEN('2', COLOR_PATTERN, 2),
		DARK_RED('4', COLOR_PATTERN, 1),
		GOLD('6', COLOR_PATTERN, 172),
		DARK_GREY('8', COLOR_PATTERN, 8),
		GREEN('a', COLOR_PATTERN, 10),
		RED('c', COLOR_PATTERN, 9),
		YELLOW('e', COLOR_PATTERN, 11),
		DARK_BLUE('1', COLOR_PATTERN, 4),
		DARK_AQUA('3', COLOR_PATTERN, 30),
		DARK_PURPLE('5', COLOR_PATTERN, 54),
		GRAY('7', COLOR_PATTERN, 246),
		BLUE('9', COLOR_PATTERN, 4),
		AQUA('b', COLOR_PATTERN, 51),
		LIGHT_PURPLE('d', COLOR_PATTERN, 13),
		WHITE('f', COLOR_PATTERN, 15),
		STRIKETHROUGH('m', FORMAT_PATTERN, 9),
		ITALIC('o', FORMAT_PATTERN, 3),
		BOLD('l', FORMAT_PATTERN, 1),
		UNDERLINE('n', FORMAT_PATTERN, 4),
		RESET('r', FORMAT_PATTERN, 0);
		
		
		private char bukkitColor;
		private String ansiColor;
		
		private ConsoleColor(char bukkitColor, String pattern, int ansiCode) {
			this.bukkitColor = bukkitColor;
			this.ansiColor = String.format(pattern, ansiCode);
		}
		
		/**
		 * Searches if the code is a valid colorcode and returns the right enum
		 * @author Timeout
		 * 
		 * @param code the Minecraft-ColorCode without Formatter-Char
		 * @return the Color enum or null if no enum can be found
		 */
		@Nullable
		public static ConsoleColor getColorByCode(char code) {
			// run trough colors
			for(ConsoleColor color: values()) {
				// check code
				if(color.bukkitColor == code) return color;
			}
			// return null for not found
			return null;
		}
		
		/**
		 * Returns the ANSI-ColorCode of the Colorcode
		 * @author Timeout
		 * 
		 * @return the Ansi-ColorCode
		 */
		@Nonnull
		public String getAnsiColor() {
			return ansiColor;
		}
	}
}
