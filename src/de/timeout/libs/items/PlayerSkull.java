package de.timeout.libs.items;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.timeout.libs.Reflections;
import net.md_5.bungee.api.ChatColor;

public class PlayerSkull extends ItemStack {
	
	private static final Class<?> craftmetaskullClass = Reflections.getCraftBukkitClass("inventory.CraftMetaSkull");
	private static final Field texturesField = Reflections.getField(craftmetaskullClass, "profile");
	
	public static final ItemStack SKELETON = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 0);
	public static final ItemStack WITHER_SKELETON = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 1);
	public static final ItemStack ZOMBIE = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 2);
	public static final ItemStack CREEPER = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 4);
	public static final ItemStack ENDERDRAGON = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 5);
	public static final ItemStack STEVE = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 3);
	
	// Cache to save already downloaded profiles.
	private static final Map<UUID, GameProfile> profileCache = new ConcurrentHashMap<>();
	
	private OfflinePlayer owner;
	private GameProfile profile;
	
	public PlayerSkull(String displayname, int amount, OfflinePlayer owner) throws InterruptedException, ExecutionException, TimeoutException {
		super(ItemStackAPI.createItemStack(Material.SKULL_ITEM, amount > 0 ? amount : 1, (short) 3, ChatColor.translateAlternateColorCodes('&', displayname)));
		this.owner = owner;
		
		Future<GameProfile> request = overrideGameProfile();
		// get Profile
		profile = request.get(5, TimeUnit.SECONDS);
		// Override profile field
		ItemMeta meta = getItemMeta();
		Reflections.setField(texturesField, meta, profile);
		setItemMeta(meta);
	}
	
	public PlayerSkull(int amount, OfflinePlayer owner) throws InterruptedException, ExecutionException, TimeoutException {
		this(owner.getName(), amount, owner);
	}
	
	public PlayerSkull(String displayname, int amount, UUID owner) throws InterruptedException, ExecutionException, TimeoutException {
		this(displayname, amount, Bukkit.getOfflinePlayer(owner));
	}
	
	@SuppressWarnings("deprecation")
	public PlayerSkull(String username) throws InterruptedException, ExecutionException, TimeoutException {
		this(username, 1, Bukkit.getOfflinePlayer(username));
	}
	
	public GameProfile getProfile() {
		return profile;
	}

	private Future<GameProfile> overrideGameProfile() {
		// start async task
		return CompletableFuture.supplyAsync(() -> {
			// get Profile
			GameProfile localProfile = profileCache.get(owner.getUniqueId());
			// if GameProfile is not in cache
			if(localProfile == null) {
				// if owner is not online
				if(!owner.isOnline()) {
					// create default gameprofile
					localProfile = new GameProfile(UUID.fromString("c06f8906-4c8a-4911-9c29-ea1dbd1aab82"), "MHF_Steve");
					// create String for trimmed id
					String name = null;
					String trimmedID = null;
					// start request
					Future<JsonObject> mojangRequest = getMojangData();
					
					// if owner is not in nms name cache
					if(owner.getUniqueId().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + owner.getName()).getBytes()))) {
						try {
							// get mojang data
							JsonObject mojangAnswer = mojangRequest.get(5, TimeUnit.SECONDS);
							// if data is valid. else return profile
							if(mojangAnswer != null) {
								// override trimmedid
								name = mojangAnswer.get("name").getAsString();
								trimmedID = mojangAnswer.get("id").getAsString();
							} else return localProfile;
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							Bukkit.getLogger().log(Level.SEVERE, "ERROR: Thread interrupted...", e);
						} catch (ExecutionException e) {
							Bukkit.getLogger().log(Level.SEVERE, "Unhandled exception: ", e);
						} catch (TimeoutException e) {
							Bukkit.getLogger().log(Level.WARNING, "Cannot get data of Player " + owner.getName() + " from MojangAPI. Connection timed out...");
						}
					} else {
						// get data from owner
						name = owner.getName();
						trimmedID = owner.getUniqueId().toString().replace("-", "");
					}
					// reinitialize profile
					localProfile = new GameProfile(fromTrimmed(trimmedID), name);
					// request from sessionserver
					Future<JsonObject> sessionRequest = getSessionServerData(trimmedID);
					try {
						// get data from session
						JsonObject sessionData = sessionRequest.get(5, TimeUnit.SECONDS);
						// if data is valid
						if(sessionData != null) {
							// remove unnnecessary data
							sessionData = sessionData.get("properties").getAsJsonArray().get(0).getAsJsonObject();
							// write data in GameProfile
							localProfile.getProperties().clear();
							localProfile.getProperties().put("textures", new Property("textures", sessionData.get("value").getAsString(), sessionData.get("signature").getAsString()));
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						Bukkit.getLogger().log(Level.SEVERE, "ERROR: Thread interrupted...", e);
					} catch (ExecutionException e) {
						Bukkit.getLogger().log(Level.SEVERE, "Unhandled exception: ", e);
					} catch (TimeoutException e) {
						Bukkit.getLogger().log(Level.WARNING, "Cannot get data of Player " + owner.getName() + " from SessionServers. Connection timed out...");
					}
				// get GameProfile from owner
				} else localProfile = Reflections.getGameProfile(owner.getPlayer());
			}
			// return profile
			return localProfile;
		});
	}
	
	private UUID fromTrimmed(String trimmedID) {
	     return UUID.fromString(Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")
	    		 .matcher(trimmedID.replace("-", ""))
	    		 .replaceAll("$1-$2-$3-$4-$5"));
	}
	
	private Future<JsonObject> getMojangData() {
		// start async task
		return CompletableFuture.supplyAsync(() -> {
			try(InputStream in = new URL("https://api.mojang.com/users/profiles/minecraft/" + owner.getName().toLowerCase(Locale.ENGLISH)).openStream()) {
				// return data from mojangapi
				return new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.WARNING, "Cannot get data of Player " + owner.getName() + " from MojangAPI. Please try again later");
			}
			// error. return null
			return null;
		});
	}
	
	private Future<JsonObject> getSessionServerData(String trimmedID) {
		// start async task
		return CompletableFuture.supplyAsync(() -> {
			try(InputStream in = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + trimmedID + "?unsigned=false").openStream()) {
				return new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.WARNING, "Cannot get data of Player " + owner.getName() + " from SessionServers. Please try again later");
			}
			// error. return null
			return null;
		});
	}
}
