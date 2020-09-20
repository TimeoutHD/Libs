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

public final class ConfigCreator {
	
	private static final ColoredLogger logger = new ColoredLogger("&8[&aLibs&8] ");
	
	private ConfigCreator() {
		/* UTILS DON'T REQUIRE A CONSTRUCTOR */
	}
	
	public static UTFConfig loadUTFConfig(String internalConfigPath, File out) throws IOException {
		return new UTFConfig(loadRessource(internalConfigPath, out));
	}
	
	public static JsonConfig loadJsonConfig(String configPath, File out) throws IOException {
		return new JsonConfig(loadRessource(configPath, out));
	}
	
	/**
	 * 
	 * @param internalConfigPath the path of the config inside of the jar
	 * @param out the file where all values should be written
	 * @return the file itself
	 * @throws IOException if the configuration could not be copied from jar to its destination
	 */
	public static File loadRessource(String internalConfigPath, File out) throws IOException {
		// create file in datafolder
		File dataFile = loadFile(out);
		String internalPath = Paths.get(internalConfigPath).toString();
		
		// copy from internal config if file is empty
		if(dataFile.length() == 0L) {
			try(InputStream in = ConfigCreator.class.getResourceAsStream(internalPath);
					OutputStream output = new FileOutputStream(dataFile)) {
				if(in != null) {
					ByteStreams.copy(in, output);
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
	 * @param out the path or name of the file
	 * @return the file itself
	 * @throws IOException if the system cannot create the file due input output errors
	 */
	private static File loadFile(File out) throws IOException {
		// create Folder if not exists
		Files.createParentDirs(out);
		
		if(out.createNewFile()) 
			logger.log(Level.INFO, String.format("&7Created new file %s in datafolder", out.getName()));
		
		return out;
	}
}
