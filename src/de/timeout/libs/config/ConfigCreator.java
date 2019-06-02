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

import net.md_5.bungee.api.ChatColor;

public class ConfigCreator {

	private static final String CONFIG_LOADED = ChatColor.translateAlternateColorCodes('&', "&8[&aOut-Configuration&8] &a%f &f is loaded &asucessful.");
	private static final String CONFIG_GENERATE = ChatColor.translateAlternateColorCodes('&', "&8[&aOut-Configuration&8] &a%f &7could not be found: &aGenerate...");
	
	private JavaPlugin main;
	private String assetsDirectory;
	
	public ConfigCreator(JavaPlugin main, String assetsDirectory) {
		this.assetsDirectory = assetsDirectory;
		this.main = main;
	}
	
	/**
	 * This method returns the main class of your plugin.
	 * @return the main class
	 */
	public JavaPlugin getMain() {
		return main;
	}
	
	/**
	 * This file creates a written configuration in your plugin folder. Subfolders must be splitted with "/" like folder/config.yml.
	 * Don't forget dataendings. Also check, if the path of your internal configuration is similar to the path of the pluginfolder.
	 * @param configPath the path of your configuration. Must be similar to their real location. 
	 * @return the File itself
	 * @throws IOException if the system cannot create the file due input-output errors
	 */
	public File loadRessource(String configPath) throws IOException {
		// call loadFile()
		File configuration = loadFile(configPath);
		// If file is empty
		if(configuration.length() == 0L) {
			// copy files into subfolder
			try(InputStream in = main.getResource(Paths.get(assetsDirectory, configPath).toString());
					OutputStream out = new FileOutputStream(configuration)) {
				ByteStreams.copy(in, out);
			}
		}
		main.getLogger().log(Level.SEVERE, () -> CONFIG_LOADED.replaceAll("%f", configuration.getName()));
		return configuration;
	}
	
	/**
	 * This method creates an empty file in your plugin folder. Subfolders must be splitted with "/" like folder/file.txt.
	 * Don't forget dataendings
	 * @param filePath the path or name of the file
	 * @return the file itself
	 * @throws IOException if the system cannot create the file due input output errors
	 */
	private File loadFile(String filePath) throws IOException {
		File configFile = Paths.get(main.getDataFolder().getAbsolutePath(), filePath).toFile();
		if(!(configFile.getParentFile().mkdirs() || configFile.createNewFile())) 
			main.getLogger().log(Level.INFO, () -> CONFIG_GENERATE.replaceAll("%f", configFile.getName()));
		
		return configFile;
	}
}
