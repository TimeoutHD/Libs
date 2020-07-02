package de.timeout.libs.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.Level;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class ConfigCreator {
	
	private static final ColoredLogger logger = new ColoredLogger("&8[&aLibs&8] ");
	
	private File dataFolder;
	
	public ConfigCreator() throws IOException {	
		// load datafolder with jar's location and pluginname in Yaml
		this.dataFolder = new File(
			new File(ConfigCreator.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile(), 
			new UTFConfig(this.getClass().getResourceAsStream("plugin.yml")).getString("name")
		);
	}
	
	public UTFConfig loadUTFConfig(String internalConfigPath, String externalPath) throws IOException {
		return new UTFConfig(loadRessource(internalConfigPath, externalPath));
	}
	
	public JsonConfig loadJsonConfig(String configPath, String externalPath) throws IOException {
		return new JsonConfig(loadRessource(configPath, externalPath));
	}
	
	/**
	 * 
	 * @param internalConfigPath
	 * @param copyPath
	 * @return
	 * @throws IOException 
	 */
	public File loadRessource(String internalConfigPath, String copyPath) throws IOException {
		// create file in datafolder
		File dataFile = loadFile(copyPath);
		String internalPath = Paths.get(internalConfigPath).toString();
		
		// copy from internal config if file is empty
		if(dataFile.length() == 0L) {
			try(InputStream in = this.getClass().getResourceAsStream(internalPath);
					OutputStream out = new FileOutputStream(dataFile)) {
				if(in != null) {
					ByteStreams.copy(in, out);
					logger.log(Level.INFO, String.format("&7Loaded File %s &asuccessfully", dataFile.getName()));
				} else logger.log(Level.WARNING, 
						String.format("&cUnable to copy data from internal file %s inside jar into file %s",
								internalPath, dataFile.getPath()));
			}
		}
		
		return dataFile;
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
		// create Folder if not exists
		Files.createParentDirs(configFile);
		
		if(configFile.createNewFile()) 
			logger.log(Level.INFO, String.format("&7Created new file %s in datafolder", configFile.getName()));
		
		return configFile;
	}
}
