package de.timeout.libs.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JsonConfig extends FileConfiguration {
	
	private static final JsonParser PARSER = new JsonParser();
		
	public JsonConfig(String json) {
		Validate.notNull(json, "Json-Data cannot be null");
		try {
			loadFromString(json);
		} catch (JsonParseException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load Configuration from String", e);			
		}
	}
	
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
		return obj.toString();
	}
	
	private static JsonObject convertSectionToObject(ConfigurationSection section) {
		// create JsonObject
		JsonObject object = new JsonObject();
		// run through section
		section.getKeys(false).forEach(key -> {
			// get Value
			Object value = section.get(key);
			// check if value is another section
			if(value instanceof ConfigurationSection) {
				// call recursive
				object.add(key, convertSectionToObject((ConfigurationSection) value));
			} else if(value instanceof List) {
				// convert to JsonArray
				JsonArray list = new JsonArray();
				((List<?>)value).forEach(element -> {
					// check for string
					if(element instanceof String) list.add(new JsonPrimitive((String) element));
					// check for boolean
					else if(element instanceof Boolean) list.add(new JsonPrimitive((Boolean) element));
					// check for number
					else if(element instanceof Number) list.add(new JsonPrimitive((Number) element));
					// check for character
					else if(element instanceof Character) list.add(new JsonPrimitive((Character) element));
					// check for configurationsections
					else if(element instanceof ConfigurationSection) list.add(convertSectionToObject((ConfigurationSection) element));
				});
				// add list to object
				object.add(key, list);
			} else if(value instanceof String) {
				// add string property
				object.addProperty(key, (String) value);
			} else if(value instanceof Number) {
				// add number property
				object.addProperty(key, (Number) value);
			} else if(value instanceof Boolean) {
				// add boolean property
				object.addProperty(key, (Boolean) value);
			} else if(value instanceof Character) {
				// add character property
				object.addProperty(key, (Character) value);
			}
		});
		// return object
		return object;
	}
	
	private static void convertMapToSection(ConfigurationSection section, Map<String, JsonElement> input) {
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
	
	private static Map<String, JsonElement> convertObjectToMap(JsonObject object) {
		// create new Map
		Map<String, JsonElement> map = new HashMap<>();
		// add entrys to map
		object.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
		// return map
		return map;
	}
	
	private static Object convertPrimitive(JsonPrimitive primitive) {
		// check data
		if(primitive.isBoolean()) return primitive.getAsBoolean();
		else if(primitive.isNumber()) return primitive.getAsNumber();
		else return primitive.getAsString();
	}
	
	/**
	 * Converts a JsonArray into an ArrayList
	 * @param section
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<?> convertArrayToList(ConfigurationSection section, JsonArray data) {
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
