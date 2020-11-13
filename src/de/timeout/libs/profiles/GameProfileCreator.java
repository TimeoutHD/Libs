package de.timeout.libs.profiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/**
 * This class represents a bridge to MineSkin.org to create own GameProfiles with custom skins
 * @author Timeout
 *
 */
public class GameProfileCreator implements Future<GameProfile> {
	
	private static final String URL_POST = "https://api.mineskin.org/generate/url?url=%s";
	private static final String FILE_POST = "https://api.mineskin.org/generate/upload";
	private static final String GENERATE_GET = "https://api.mineskin.org/generate?model=%s&name=&visibility=1";
	
	private static final ExecutorService service = Executors.newSingleThreadExecutor();
	
	private static int delay = 0;
	
	private String url;
	private File skinFile;
	
	private Future<JsonObject> request;
	private final Model model;
	
	public GameProfileCreator(@NotNull String url, @NotNull Model model) {
		// validate
		Validate.notNull(url, "URL cannot be null");
		Validate.notNull(model, "Model cannot be null");
		
		// initialize
		this.url = url;
		this.model = model;
	}
	
	public GameProfileCreator(@NotNull File skinFile, @NotNull Model model) throws IOException {
		// Validate
		Validate.notNull(skinFile, "File cannot be null");
		Validate.notNull(model, "Model cannot be null");
		Validate.isTrue("image/png".equalsIgnoreCase(Files.probeContentType(skinFile.toPath())), "File must be a png file");
		
		// initialize
		this.skinFile = skinFile;
		this.model = model;
	}
	
	@Override
	public GameProfile get() throws InterruptedException, ExecutionException {
		// define data request
		request = createDataRequest();
		
		// create GameProfile and mineskin object
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		JsonObject res = request.get();
		
		// update delay
		delay = res.get("nextRequest").getAsInt();
		
		// insert value and gameprofile
		JsonObject texture = res.get("data").getAsJsonObject().get("texture").getAsJsonObject();
		profile.getProperties().clear();
		profile.getProperties().put("textures", 
				new Property("textures", texture.get("value").getAsString(), texture.get("signature").getAsString()));
		
		// return full profile
		return profile;
	}
	
	private Future<JsonObject> createDataRequest() {
		return CompletableFuture.supplyAsync(() -> {
			// wait for completion (post decrement if not)
			while(delay-- > 0) {
				// sleep one second
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					Logger.getGlobal().log(Level.SEVERE, "Cannot wait until delay ends", e);
				}
			}
				
			// perform post request
			try {
				performPostRequest();
			} catch (IOException e) {
				Logger.getGlobal().log(Level.WARNING, "Unable to send Post-Request to MineSkin.org", e);
			}
				
			
			// perform get request
			try(InputStream in = createURLConnection(String.format(GENERATE_GET, model.getName())).getInputStream()) {
				// convert get to JsonObject
				 return new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
			} catch (IOException e) {
				Logger.getGlobal().log(Level.WARNING, "Unable to create Get-Request to MineSkin.org", e);
				return null;
			}
		}, service);
	}
	
	private void performPostRequest() throws IOException {		
		// create URL Connection
		URLConnection connection = createURLConnection(url != null ? String.format(URL_POST, url) : FILE_POST);
			
		// perform data post
		try(OutputStream post = connection.getOutputStream()) {
			// post file
			if(skinFile != null) post.write(Files.readAllBytes(skinFile.toPath()));
		} catch (IOException e) {
			Logger.getGlobal().log(Level.WARNING, "Unable to create Push-Request to Mineskin.org", e);
		}
	}
	
	/**
	 * Creates a URL-Connection with a custom header
	 * 
	 * @param url the url you want to access
	 * @return the URLConnection to the address
	 * @throws IOException if the connection could not be established
	 */
	private URLConnection createURLConnection(String url) throws IOException {
		URLConnection connection = new URL(url).openConnection();
		
		// set user agent
		connection.setRequestProperty("User-Agent", String.format("Libs 2.0_REBUILD (%s, %s) ",
				System.getProperty("os.name"), System.getProperty("os.version")));
		
		return connection;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return request.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return request.isCancelled();
	}

	@Override
	public boolean isDone() {
		return request.isDone();
	}

	@Override
	public GameProfile get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return null;
	}
	
	/**
	 * This enum represents the model of the gameprofile.
	 * @author Timeout
	 *
	 */
	public enum Model {
		
		STEVE("steve"), ALEX("alex");
		
		private final String name;
		
		Model(String name) {
			this.name = name;
		}
		
		/**
		 * This method returns the name of the model
		 * @return the name of the model
		 */
		public String getName() {
			return name;
		}
	}
}
