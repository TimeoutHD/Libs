package de.timeout.libs.profiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/**
 * This class represents a bridge to MineSkin.org to create own GameProfiles with custom skins
 * @author Timeout
 *
 */
public class GameProfileCreator implements Supplier<GameProfile> {
	
	private static final String URL_FORMAT = "https://api.mineskin.org/generate/url?url=%s&name=%s&model=%s&visibility=%s";
	
	private String url;
	private Model model;
	
	public GameProfileCreator(String url, Model model) {
		// validate
		Validate.notNull(url, "URL cannot be null");
		Validate.notNull(model, "Model cannot be null");
		// initialize
		this.url = url;
		this.model = model;
	}

	@Override
	public GameProfile get() {
		// create empty profile
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);

		// create UTL connection
		try(InputStream in = new URL(String.format(URL_FORMAT, url, "", model.getName(), 0)).openStream()) {
			// get Response
			JsonObject response = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject()
					.get("data").getAsJsonObject()
					.get("texture").getAsJsonObject();
			// get Data
			String value = response.get("value").getAsString();
			String signature = response.get("signature").getAsString();
			// write data in profile
			profile.getProperties().put("textures", new Property("textures", value, signature));
		} catch (IOException e) {
			Logger.getGlobal().log(Level.WARNING, "Cannot get response from MineSkin.org", e);
		} 
		return profile;
	}
	
	/**
	 * This enum represents the model of the gameprofile.
	 * @author Timeout
	 *
	 */
	public enum Model {
		
		STEVE("steve"), ALEX("alex");
		
		private String name;
		
		private Model(String name) {
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
