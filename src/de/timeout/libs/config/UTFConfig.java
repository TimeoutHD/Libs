package de.timeout.libs.config;

import java.io.BufferedReader;
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
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
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
	 * @deprecated will be removed if {@link YamlConfiguration#load(InputStream)} don't exist
	 */
	@Deprecated
	public UTFConfig(InputStream stream) {
		try {
			this.original.addAll(new BufferedReader(new InputStreamReader(stream)).lines().parallel().collect(Collectors.toList()));
			// load Config from InputStream
			load(stream);
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not load Configuration from InputStream", e);
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
			
			return diff3(valueDump);
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
	
	private String diff3(String dump) {
		// get List
		List<String> dumpList = Arrays.asList(dump.split("\n"));
		
		try {
			// generating diff information
			Patch<String> diff = DiffUtils.diff(original, dumpList);
			
			// run though changes
			for(AbstractDelta<String> delta : new ArrayList<>(diff.getDeltas())) {
				// copy targetList and sourceList
				List<String> targetCopy = new ArrayList<>(delta.getTarget().getLines());
				List<String> sourceCopy = new ArrayList<>(delta.getSource().getLines());
				// check if delta is change delta
				if(delta.getType() == DeltaType.DELETE) {
					// remove comment changes
					removeCommentChanges(sourceCopy);
					// apply
					if(!sourceCopy.isEmpty()) {
						delta.getSource().setLines(sourceCopy);
					} else diff.getDeltas().remove(delta);
				} else if(delta.getType() == DeltaType.CHANGE) {
					// update changes
					applyCommentInsertion(sourceCopy, targetCopy);
					// remove comment changes
					delta.getTarget().setLines(targetCopy);
				}
			}
			// apply patch and return
			return String.join("\n", DiffUtils.patch(original, diff).toArray(new String[0]));
		} catch (DiffException | PatchFailedException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot compute changes. Load old values. Please report this error", e);
		}
		// return dump after error
		return dump;
	}
	
	/**
	 * Removes all Deltas which delete comments in File
	 * @param list the list of changes (sources)
	 */
	private static void removeCommentChanges(List<String> list) {
		// remove all comments
		list.removeIf(filter -> filter.trim().isEmpty() || filter.trim().startsWith("#"));
		// run through lines
		for(int i = 0; i < list.size(); i++) {
			// get Line
			String line = list.get(i);
			// if comment is on this line
			int commentStart = line.indexOf('#');
			if(commentStart != -1) {
				// replace comment change
				list.set(i, line.substring(0, commentStart -1));
			}
		}
	}
	
	/**
	 * Merges all comments in Change-Delta into list target
	 * @param source the line before change
	 * @param target the changes itself
	 */
	private static void applyCommentInsertion(List<String> source, List<String> target) {
		// iterate through source
		for(int i = 0; i < source.size(); i++) {
			String line = source.get(i);
			// if this is a comment line
			if(line.trim().isEmpty() || line.trim().startsWith("#")) {
				// add value to target list
				target.add(i, line);
			} else {
				// check if value has a comment inside
				int commentBegin = line.indexOf('#');
				if(commentBegin != -1) {
					// add comment to target list
					target.set(i, target.get(i) + line.substring(commentBegin));
				}
			}
		}
	}
}
