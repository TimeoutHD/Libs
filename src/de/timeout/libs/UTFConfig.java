package de.timeout.libs;

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
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.io.Files;

public class UTFConfig extends YamlConfiguration {
	
	public UTFConfig(File file) {
		try {
			load(file);
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not load Configuration " + file.getName(), e);
		}
	}
	
	public UTFConfig(InputStream stream) {
		try {
			load(stream);
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not load Configuration from InputStream", e);
		}
	}

	@Override
	public void save(File file) throws IOException {
		Validate.notNull(file, "File can't be null");
		Files.createParentDirs(file);
		String data = this.saveToString();
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
		
		try {
			writer.write(data);
		} finally {writer.close();}
	}
	
	@Override
	public String saveToString() {
		try {
			Field optionField = Reflections.getField(getClass(), "yamlOptions");
			Field representerField = Reflections.getField(getClass(), "yamlRepresenter");
			Field yamlField = Reflections.getField(getClass(), "yaml");
			
			if(optionField != null && representerField != null && yamlField != null) {
				optionField.setAccessible(true);
				representerField.setAccessible(true);
				yamlField.setAccessible(true);
				
				DumperOptions yamlOptions = (DumperOptions) optionField.get(this);
				Representer yamlRepresenter = (Representer) representerField.get(this);
				Yaml yaml = (Yaml) yamlField.get(this);
				DumperOptions.FlowStyle flow = DumperOptions.FlowStyle.BLOCK;
				
				yamlOptions.setIndent(this.options().indent());
				yamlOptions.setDefaultFlowStyle(flow);
				yamlOptions.setAllowUnicode(true);
				yamlRepresenter.setDefaultFlowStyle(flow);
				
				String header = this.buildHeader();
				String dump = yaml.dump(this.getValues(false));
				
				if("{}\n".equals(dump)) dump = "";
				return header + dump;
			}
		} catch(IllegalArgumentException | IllegalAccessException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Error in converting Configuration to String", e);
		}
		return "Error: Cannot be saved to String";
	}
	
	@Override
	public void load(File file) throws IOException, InvalidConfigurationException {
		Validate.notNull(file, "File can't be null");
		this.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
	}
	
	public void load(InputStream stream) throws IOException, InvalidConfigurationException {
		this.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
	}
}
