package de.timeout.libs.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteStreams;

public class ConfigCreator {

	private static final String CONFIG_LOADED = "§8[§aOut-Configuration§8] §a%f §f is §asucessful loaded";
	private static final String CONFIG_GENERATE = "§8[§aOut-Configuration§8] §a%f §7could not be found: §aGenerate...";
	
	private JavaPlugin main;
	private String assetsDirectory;
	
	public ConfigCreator(JavaPlugin main, String assetsDirectory) {
		this.assetsDirectory = assetsDirectory;
		this.main = main;
	}
	
	public JavaPlugin getMain() {
		return main;
	}
	
	public File loadRessource(String configPath) throws IOException {
		File configuration = loadFile(configPath);
		if(configuration.length() == 0L) {
			try(InputStream in = main.getResource(Paths.get(assetsDirectory, configPath).toString());
					OutputStream out = new FileOutputStream(configuration)) {
				ByteStreams.copy(in, out);
			}
		}
		main.getLogger().log(Level.SEVERE, () -> CONFIG_LOADED.replaceAll("%f", configuration.getName()));
		return configuration;
	}
	
	private File loadFile(String filePath) throws IOException {
		File configFile = Paths.get(main.getDataFolder().getAbsolutePath(), filePath).toFile();
		if(!(configFile.getParentFile().mkdirs() || configFile.createNewFile())) 
			main.getLogger().log(Level.INFO, () -> CONFIG_GENERATE.replaceAll("%f", configFile.getName()));
		
		return configFile;
	}
}
