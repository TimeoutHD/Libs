package de.timeout.libs.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.io.Files;

import de.timeout.libs.Reflections;

/**
 * This class represents a Yaml Document with UTF-8 Coding
 * @author timeout
 *
 */
public class UTFConfig extends YamlConfiguration {
	
	/**
	 * These are option fields of the Yaml-Configuration. It is important to have access.
	 */
	private static final Field optionField = Reflections.getField(YamlConfiguration.class, "yamlOptions");
	private static final Field representerField = Reflections.getField(YamlConfiguration.class, "yamlRepresenter");
	private static final Field yamlField = Reflections.getField(YamlConfiguration.class, "yaml");
		
	private final List<String> original = new ArrayList<>();
			
	/**
	 * Create a UTF-Config of a File
	 * @param file the required file
	 */
	public UTFConfig(File file) {	
		try {	
			// read file lines
			this.original.addAll(Files.readLines(file, StandardCharsets.UTF_8));
			// load Config from file content
			load(file);
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not load Configuration " + file.getName(), e);
		}
	}
	
	/**
	 * Create a UTF-Config of an InputStream
	 * @param stream the used inputsteam
	 * @throws IOException If the stream cannot be read
	 */
	public UTFConfig(InputStream stream) throws IOException {
		this(IOUtils.toString(stream, StandardCharsets.UTF_8));
	}
	
	public UTFConfig(String source) {
		try {
			this.original.addAll(Arrays.asList(source.split("\n")));
			loadFromString(source);
		} catch (InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not load Configuration from String", e);
		}
	}

	/**
	 * This method write the current configuration into the file.
	 * @param file the file where the config should be written in
	 */
	@Override
	public void save(File file) throws IOException {
		// File cannot be null
		Validate.notNull(file, "File can't be null");
		// Create parent dirs of the file if they don't exist
		Files.createParentDirs(file);
		// Convert configuration into a String
		String data = this.saveToString();
		
		// writes String into the file and close writer due AutoCloseable
		try(Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			writer.write(data);
		}
	}
	
	/**
	 * This method converts the current configuration into a String 
	 * 
	 * @throws IllegalArgumentException if some attributes are null. Normally it does never come to this
	 */
	@Override
	public String saveToString() {
		// Load attributes with Reflection-Utils
		DumperOptions yamlOptions = (DumperOptions) Reflections.getValue(optionField, this);
		Representer yamlRepresenter = (Representer) Reflections.getValue(representerField, this);
		Yaml yaml = (Yaml) Reflections.getValue(yamlField, this);
		
		// if all values are loaded
		if(yamlOptions != null && yamlRepresenter != null && yaml != null) {
			// apply settings 
			yamlOptions.setIndent(this.options().indent());
			yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			yamlOptions.setAllowUnicode(true);
			yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

			String valueDump = yaml.dump(this.getValues(false)).replaceAll("\\{\\}\n", "");
			
			return MyersDiffUtils.diff3(original, valueDump);
		} else throw new IllegalArgumentException("Could not load required attributes from Configuration");
	}
	
	/**
	 * This method loads a Yaml-Configuration of a file
	 * @param file the file 
	 */
	@Override
	public void load(File file) throws IOException, InvalidConfigurationException {
		Validate.notNull(file, "File can't be null");
		this.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
	}
	
	/**
	 * This method loads a Yaml-Configuration of an InputStream
	 * @param stream the InputStream
	 * 
	 * @deprecated Just ignore it, it won't be removed. It's just deprecated cause the deprecation of {@link YamlConfiguration#load(InputStream)}
	 */
	@Deprecated
	public void load(InputStream stream) throws IOException, InvalidConfigurationException {
		Validate.notNull(stream, "InputStream cannot be null");
		this.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
	}
	

}
