package de.timeout.libs.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Represents a FileConfiguration which is written in JSON
 * 
 * @author Timeout
 *
 */
public class JsonConfig extends FileConfiguration {
	
	private static final JsonParser PARSER = new JsonParser();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	private final List<String> original = new ArrayList<>();
		
	/**
	 * Creates a new JsonConfiguration from a Json-String
	 * 
	 * @param json the Json-String. Cannot be null
	 * @throws IllegalArgumentException if the Json-String is null
	 */
	public JsonConfig(String json) {
		Validate.notNull(json, "Json-Data cannot be null");
		try {
			loadFromString(json);
		} catch (JsonParseException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load Configuration from String", e);			
		}
	}
	
	/**
	 * Creates a new JsonConfiguration from a File
	 * 
	 * @param json the .json file
	 * @throws IllegalArgumentException if the file is null
	 */
	public JsonConfig(File json) {
		// read from File
		Validate.notNull(json, "File cannot be null");
		try {
			loadFromString(FileUtils.readFileToString(json, StandardCharsets.UTF_8));
		} catch (JsonParseException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load Configuration from " + json.getName(), e);			
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, String.format("Cannot load %s. IO-Exception: ", json.getName()), e);
		}
	}
	
	public JsonConfig(InputStream json) {
		// Validate
		Validate.notNull(json, "InputStream cannot be null");
		try {
			loadFromString(IOUtils.toString(json));
		} catch (JsonParseException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load Configuration from InputStream", e);			
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load Json from InputStream. IO-Exception: ", e);
		}
	}

	@Override
	public void loadFromString(@Nonnull String arg0) throws InvalidConfigurationException {
		// Validate
		Validate.notNull(arg0, "String cannot be null");
		// initialize original
		original.addAll(Arrays.asList(arg0.split("\n")));
		// load data
		JsonElement data = PARSER.parse(arg0);
		// if data is not a JSON-Object
		if(!data.isJsonObject()) {
			// create map of data
			Map<String, JsonElement> input = new HashMap<>();
			// add all values to map
			((JsonObject) data).entrySet().forEach(entry -> input.put(entry.getKey(), entry.getValue()));
			
			// check input
			if(input.isEmpty()) {
				// convert data
				convertMapToSection(this, input);
			}
		} else throw new InvalidConfigurationException("A JsonConfig must be a JsonObject!");
	}

	@Override
	protected String buildHeader() {
		return null;
	}

	@Override
	public String saveToString() {
		// create JsonObject
		JsonObject obj = convertSectionToObject(this);
		return MyersDiffUtils.diff3(original, GSON.toJson(obj));
	}
	
	/**
	 * Converts a ConfigurationSection to JsonObject
	 * @author Timeout
	 * 
	 * @param section the section you want to convert. Cannot be null
	 * @throws IllegalArgumentException if the section is null
	 * @return the converted section as JsonObject
	 */
	@Nonnull
	private static JsonObject convertSectionToObject(ConfigurationSection section) {
		// Validate
		Validate.notNull(section, "Section cannot be null");
		// create JsonObject
		JsonObject obj = new JsonObject();
		// run through section
		section.getKeys(false).forEach(key -> {
			// check values data type
			if(section.isConfigurationSection(key)) {
				// is configurationsection
				// convert section and add to this obj
				obj.add(key, convertSectionToObject(section.getConfigurationSection(key)));
			} else if(section.isList(key)) {
				// is list
				// convert list to JsonArray and add it to obj
				obj.add(key, convertListToArray(section.getList(key)));
			} else if(section.get(key) != null) {
				// key is primitive
				obj.add(key, convertValueToPrimitive(section.get(key)));
			}
		});
		return obj;
	}
	
	/**
	 * Converts a List into a JsonArray
	 * 
	 * @param list the list you want to convert
	 * @return the list converted as JsonArray
	 */
	@Nonnull
	private static JsonArray convertListToArray(List<?> list) {
		// create JsonArray
		JsonArray array = new JsonArray();
		// run through array
		list.forEach(element -> {
			// check elements datatype
			if(element instanceof ConfigurationSection) {
				// convert to JsonObject and add it here
				array.add(convertSectionToObject((ConfigurationSection) element));
			} else if(element instanceof List) {
				// convert to JsonArray and add it here
				array.add(convertListToArray((List<?>) element));
			} else if(element == null) {
				// convert to JsonNull and add it here
				array.add(null);
			} else {
				// add JsonPrimitive to array
				array.add(convertValueToPrimitive(element));
			}
		});
		// return array
		return array;
	}
	
	/**
	 * Converts a Map into a ConfigurationSection and links it to its root
	 * 
	 * @param section the root of the Map
	 * @param input the converted JsonObject
	 * @throws IllegalArgumentException if section or input is null
	 */
	@Nonnull
	private static void convertMapToSection(ConfigurationSection section, Map<String, JsonElement> input) {
		// Validate
		Validate.notNull(section, "Root-Section cannot be null");
		Validate.notNull(input, "Input cannot be null");
		// run through sections
		input.entrySet().forEach(entry -> {
			// get Key and value
			String key = entry.getKey();
			JsonElement value = entry.getValue();
			// validate value
			if(entry.getValue() instanceof JsonObject) {
				// is configurationsection
				convertMapToSection(section.createSection(key), convertObjectToMap((JsonObject) value));
			} else if(value instanceof JsonPrimitive) {
				// is a property
				section.set(key, convertPrimitive((JsonPrimitive) value));
			} else if(value instanceof JsonArray) {
				// is a list
				section.set(key, convertArrayToList(section.createSection(key), (JsonArray) value));
			}
		});
	}
	
	/**
	 * Converts a JsonObject to a Map with property key and JsonElement value
	 * 
	 * @param object the JsonObject you want to convert. Cannot be null
	 * @throws IllegalArgumentException if the JsonObject is null
	 * @return the converted JsonObject into a Map
	 */
	@Nonnull
	private static Map<String, JsonElement> convertObjectToMap(JsonObject object) {
		// create new Map
		Map<String, JsonElement> map = new HashMap<>();
		// add entrys to map
		object.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
		// return map
		return map;
	}
	
	/**
	 * Converts a JsonPrimitive to an Object
	 * 
	 * @param primitive the primitive you want to convert. Cannot be null
	 * @throws IllegalArgumentException if the primitive is null
	 * @return the value as Object (Wrapper)
	 */
	@Nonnull
	private static Object convertPrimitive(JsonPrimitive primitive) {
		// Validate
		Validate.notNull(primitive, "Data cannot be a JsonNull");
		// check data
		if(primitive.isBoolean()) return primitive.getAsBoolean();
		else if(primitive.isNumber()) return primitive.getAsNumber();
		else return primitive.getAsString();
	}
	
	/**
	 * Converts a value in a JsonPrmitive
	 * 
	 * @param value the value you want to convert. It cannot be null
	 * @throws IllegalArgumentException if the value is null
	 * @return the converted JsonPrimitive
	 */
	@Nonnull
	private static JsonPrimitive convertValueToPrimitive(Object value) {
		// Validate
		Validate.notNull(value, "Value cannot be null");
		// convert into right datatype
		if(value instanceof Boolean) return new JsonPrimitive((Boolean) value);
		else if(value instanceof Number) return new JsonPrimitive((Number) value);
		else if(value instanceof Character) return new JsonPrimitive((Character) value);
		else return new JsonPrimitive((String) value);
	}
	
	/**
	 * Converts a JsonArray into an ArrayList
	 * 
	 * @param section the Root-Section. Cannot be null
	 * @param data the data you want to convert. Cannot be null
	 * @throws IllegalArgumentException if any argument is null
	 * @return the data converted as list
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	private static List<?> convertArrayToList(ConfigurationSection section, JsonArray data) {
		// Validate
		Validate.notNull(section, "Root-Section cannot be null");
		Validate.notNull(data, "JsonArray cannot be null");
		// create List
		List<?> list = new ArrayList<>();
		// run through data
		for(int i = 0; i < data.size(); i++) {
			// get element
			JsonElement element = data.get(i);
			// check if data is
			if(data.isJsonObject()) {
				// create configuration section
				ConfigurationSection subSection = section.createSection(String.valueOf(i));
				// fill section with data
				convertMapToSection(subSection, convertObjectToMap((JsonObject) element));
				// add to list
				((List<ConfigurationSection>) list).add(subSection);
			} else if(data.isJsonArray()) {
				// is another list
				((List<List<?>>) list).add(convertArrayToList(section.createSection(String.valueOf(i)), (JsonArray) element));
			} else if(data.isJsonNull()) {
				// is null
				list.add(null);
			} else {
				// is primitive
				((List<Object>) list).add((Object) convertPrimitive((JsonPrimitive) element));
			}
		}
		// return list
		return list;
	}
}
