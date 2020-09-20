package de.timeout.libs.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a FileConfiguration which is written in JSON
 * 
 * @author Timeout
 *
 */
public class JsonConfig extends FileConfiguration {
	
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
			
	/**
	 * Creates a new JsonConfiguration from a Json-String
	 * 
	 * @param json the Json-String. Cannot be null
	 * @throws IllegalArgumentException if the Json-String is null
	 */
	public JsonConfig(@NotNull String json) {
		Validate.notNull(json, "Json-Data cannot be null");
		try {
			loadFromString(json);
		} catch (InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load Configuration from String", e);			
		}
	}
	
	/**
	 * Creates a new JsonConfiguration from a File
	 * 
	 * @param json the .json file
	 * @throws IllegalArgumentException if the file is null
	 */
	public JsonConfig(@NotNull File json) {
		// read from File
		Validate.notNull(json, "File cannot be null");
		// load if file exists
		try {
			loadFromString(json.exists() && json.length() > 0 ? FileUtils.readFileToString(json, StandardCharsets.UTF_8) : "{}");
		} catch (InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, e, () -> "Cannot load Configuration from " + json.getName());
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, String.format("Cannot load %s. IO-Exception: ", json.getName()), e);
		}
	}
	
	public JsonConfig(@NotNull InputStream json) {
		// Validate
		Validate.notNull(json, "InputStream cannot be null");
		try {
			loadFromString(IOUtils.toString(json, StandardCharsets.UTF_8));
		} catch (InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load Configuration from InputStream", e);			
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load Json from InputStream. IO-Exception: ", e);
		}
	}

	@Override
	public @NotNull String saveToString() {
		// return to string
		return GSON.toJson(getValues(false));
	}

	/**
	 * A Json-Configuration is not a Yaml-Configuration. <br>
	 * It returns null in any case
	 */
	@Override
	public @NotNull JsonConfigOptions options() {
		// return options
		return (JsonConfigOptions) Optional.ofNullable(options).orElseGet(() -> {
			// create new options
			options = new JsonConfigOptions(this);
			// return options
			return options;
		});
	}

	@Override
	protected @NotNull String buildHeader() {
		// JSON does not support any comments
		return "";
	}

	@Override
	public void loadFromString(@NotNull String data) throws InvalidConfigurationException {
		// Validate
		Validate.notNull(data, "String cannot be null");
		// only continue if data is not empty
		if(!data.isEmpty()) {
			// create map
			Map<?, ?> map = GSON.fromJson(data, Map.class);
			// convert to sections
			convertMapsToSection(map, this);
		}
	}
	
	private void convertMapsToSection(@NotNull Map<?, ?> map, @NotNull ConfigurationSection section) {
		// Validate
		Validate.notNull(map, "Map cannot be null");
		Validate.notNull(section, "MemorySection cannot be null");
		// create result
		final Object root = applyMapType(map);
		// create map order
		if(root instanceof Map) {
			// override map
			map = (Map<?, ?>) root;
			// walk through their entries
			map.forEach((key1, value) -> {
				// define key and value to avoid compileerror in datatypes
				String key = key1.toString();
				// apply same to childs
				if (value instanceof Map) {
					// recursive call for submemorysecitions
					convertMapsToSection((Map<?, ?>) value, section.createSection(key));
				} else section.set(key, value);
			});
		} else section.set("", root);
	}
	
	private Object applyMapType(Map<?, ?> map) {
		// create map
		Map<String, Object> applied = new HashMap<>(map.size());
		// run through map
		map.forEach((key, value) -> {
			// check data type
			if (value instanceof Map) {
				// apply mao
				applied.put(key.toString(), applyMapType(applied));
			} else if (value instanceof List) {
				// apply list
				applied.put(key.toString(), applyListType((List<?>) value));
			} else applied.put(key.toString(), value);
		});
		// reutrn map
		return applied;
	}
	
	private Object applyListType(List<?> list) {
		// create list
		List<Object> data = new ArrayList<>();
		// run through list
		list.forEach(element -> {
			// check collection type
			if(element instanceof Map) {
				// apply map
				data.add(applyMapType((Map<?, ?>) element));
			} else if(element instanceof List) {
				// apply list
				data.add(applyListType((List<?>) element));
			} else data.add(element);
		});
		// return data
		return data;
	}
}
