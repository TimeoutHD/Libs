package de.timeout.libs.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.io.ByteStreams;

public class ConfigCreator {
	
	private static final String ANSI_GREY = "\u001B[90m";
	private static final String ANSI_LIME = "\u001B[92m";
	private static final String ANSI_WHITE = "\u001B[97m";
	private static final String ANSI_RESET = "\u001B[0m";

	private static final String CONFIG_LOADED = ANSI_GREY + "[" + ANSI_LIME + "Out-Configuration" + ANSI_GREY + "] " + ANSI_LIME + "%f " + ANSI_WHITE + " is loaded " + ANSI_LIME + "sucessfully" + ANSI_WHITE + "." + ANSI_RESET;
	private static final String CONFIG_GENERATE = ANSI_GREY + "[" + ANSI_LIME + "Out-Configuration" + ANSI_GREY + "] "+ ANSI_LIME + "%f " + ANSI_WHITE + "could not be found: " + ANSI_LIME + "Generate..." + ANSI_RESET;
	
	private File dataFolder;
	private String assetsDirectory;
	
	public ConfigCreator(File datafolder, String assetsDirectory) {
		this.assetsDirectory = assetsDirectory;
		this.dataFolder = datafolder;
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
			try(InputStream in = this.getClass().getClassLoader().getResourceAsStream(Paths.get(assetsDirectory, configPath).toString());
					OutputStream out = new FileOutputStream(configuration)) {
				ByteStreams.copy(in, out);
			}
		}
		Logger.getGlobal().log(Level.INFO, () -> CONFIG_LOADED.replace("%f", configuration.getName()));
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
		File configFile = Paths.get(dataFolder.getAbsolutePath(), filePath).toFile();
		if(configFile.getParentFile().mkdirs() || configFile.createNewFile()) 
			Logger.getGlobal().log(Level.INFO, () -> CONFIG_GENERATE.replace("%f", configFile.getName()));
		
		return configFile;
	}
}
