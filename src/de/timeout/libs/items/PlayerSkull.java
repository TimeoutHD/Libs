package de.timeout.libs.items;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.timeout.libs.Reflections;
import net.md_5.bungee.api.ChatColor;

public class PlayerSkull extends ItemStack {
	
	public static final ItemStack SKELETON = new ItemStack(Material.SKELETON_SKULL);
	public static final ItemStack WITHER_SKELETON = new ItemStack(Material.WITHER_SKELETON_SKULL);
	public static final ItemStack ZOMBIE = new ItemStack(Material.ZOMBIE_HEAD);
	public static final ItemStack CREEPER = new ItemStack(Material.CREEPER_HEAD);
	public static final ItemStack ENDERDRAGON = new ItemStack(Material.DRAGON_HEAD);
	public static final ItemStack STEVE = new ItemStack(Material.PLAYER_HEAD);
	public static final ItemStack ALEX = new PlayerSkull("MHF_Alex");
	public static final ItemStack BLAZE = new PlayerSkull("MHF_Blaze");
	public static final ItemStack CAVE_SPIDER = new PlayerSkull("MHF_CaveSpider");
	public static final ItemStack CHICKEN = new PlayerSkull("MHF_Chicken");
	public static final ItemStack COW = new PlayerSkull("MHF_Cow");
	public static final ItemStack ENDERMAN = new PlayerSkull("MHF_ENDERMAN");
	public static final ItemStack GHAST = new PlayerSkull("MHF_GHAST");
	public static final ItemStack GOLEM = new PlayerSkull("MHF_Golem");
	public static final ItemStack HEROBRINE = new PlayerSkull("MHF_Herobrine");
	public static final ItemStack LAVA_SLIME = new PlayerSkull("MHF_LavaSlime");
	public static final ItemStack MUSHROOM_COW = new PlayerSkull("MHF_MushroomCow");
	public static final ItemStack OCELOT = new PlayerSkull("MHF_Ocelot");
	public static final ItemStack PIG = new PlayerSkull("MHF_Pig");
	public static final ItemStack ZOMBIE_PIGMAN = new PlayerSkull("MHF_PigZombie");
	public static final ItemStack SLIME = new PlayerSkull("MHF_Slime");
	public static final ItemStack SPIDER = new PlayerSkull("MHF_Spider");
	public static final ItemStack SQUID = new PlayerSkull("MHF_Squid");
	public static final ItemStack VILLAGER = new PlayerSkull("MHF_Villager");
	public static final ItemStack CACTUS = new PlayerSkull("MHF_Cactus");
	public static final ItemStack CAKE = new PlayerSkull("MHF_Cake");
	public static final ItemStack CHEST = new PlayerSkull("MHF_Cake");
	public static final ItemStack COCONUT_BROWN = new PlayerSkull("MHF_CoconutB");
	public static final ItemStack COCONUT_GREEN = new PlayerSkull("MHF_CoconutG");
	public static final ItemStack MELON = new PlayerSkull("MHF_Melon");
	public static final ItemStack OAK_LOG = new PlayerSkull("MHF_OakLog");
	public static final ItemStack PRESENT_GREEN = new PlayerSkull("MHF_Present1");
	public static final ItemStack PRESENT_RED = new PlayerSkull("MHF_Present2");
	public static final ItemStack TNT_WITH_SIGN = new PlayerSkull("MHF_TNT");
	public static final ItemStack TNT_WITHOUT_SIGN = new PlayerSkull("MHF_TNT");
	public static final ItemStack ARROW_UP = new PlayerSkull("MHF_ArrowUp");
	public static final ItemStack ARROW_DOWN = new PlayerSkull("MHF_ArrowDown");
	public static final ItemStack ARROW_LEFT = new PlayerSkull("MHF_ArrowLeft");
	public static final ItemStack ARROW_RIGHT = new PlayerSkull("MHF_ArrowRight");
	public static final ItemStack EXCLAMATION = new PlayerSkull("MHF_Exclamation");
	public static final ItemStack QUESTION = new PlayerSkull("MHF_Question");
	
	
	// Cache to save already downloaded profiles.
	private static final Map<UUID, GameProfile> profileCache = new ConcurrentHashMap<>();
	
	private GameProfile ownerProfile;
	private OfflinePlayer owner;
	
	public PlayerSkull(String displayname, int amount, OfflinePlayer owner) {
		super(new ItemStack(Material.PLAYER_HEAD,
				amount > 0 ? amount : 1));
		
		// Set Displayname
		ItemMeta meta = this.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayname));
		this.setItemMeta(meta);
		
		this.owner = owner;
		// Start new Thread asyncrously to get and write playerdata into skull.
		startDataThread();
	}
	
	public PlayerSkull(int amount, OfflinePlayer owner) {
		this(owner.getName(), amount, owner);
	}
	
	public PlayerSkull(String displayname, int amount, UUID owner) {
		this(displayname, amount, Bukkit.getOfflinePlayer(owner));
	}
	
	@SuppressWarnings("deprecation")
	public PlayerSkull(String username) {
		this(username, 1, Bukkit.getOfflinePlayer(username));
	}
	
	/**
	 * Start an async Thread which update the Skull after an owner-change
	 */
	private void startDataThread() {
		new Thread(new SkullChangeScheduler(this)).start();
	}

	/*
	 * Get a copy of this Skull's SkullMeta
	 */
	@Override
	public SkullMeta getItemMeta() {
		return (SkullMeta) super.getItemMeta();
	}

	/**
	 * Get Owner of this Skull
	 * @return Owner of this Skull
	 */
	public OfflinePlayer getOwner() {
		return owner;
	}
	
	/**
	 * Set a new SkullOwner of this Skull
	 * @param player the new Owner
	 */
	public void setOwner(OfflinePlayer player) {
		this.owner = player;
		startDataThread();
	}
	
	/**
	 * Get the Owner's GameProfile
	 * @return owner's GameProfile
	 */
	public GameProfile getOwnerProfile() {
		return ownerProfile;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.getClass() == this.getClass()) {
			PlayerSkull objSkull = (PlayerSkull) obj;
			return super.equals(obj) && owner.getUniqueId().toString().equalsIgnoreCase(objSkull.getOwner().getUniqueId().toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		
		hash *= owner.hashCode();
		hash *= ownerProfile != null ? ownerProfile.hashCode() : 1;
		return hash;
	}

	/**
	 * Set the SkullMeta of this Skull
	 * 
	 * @param itemMeta new SkullMeta
	 * @return True if successfully applied ItemMeta, see {@link ItemFactory#isApplicable(ItemMeta, ItemStack)}
	 */
	@Override
	public boolean setItemMeta(ItemMeta itemMeta) {
		if(!(itemMeta instanceof SkullMeta)) throw new IllegalArgumentException("ItemMeta must be an instance of SkullMeta!");
		return setItemMeta((SkullMeta) itemMeta);
	}
	
	/**
	 * Set the SkullMeta of this Skull
	 * 
	 * @param itemMeta new SkullMeta
	 * @return True if successfully applied ItemMeta, see {@link ItemFactory#isApplicable(ItemMeta, ItemStack)}
	 */
	public final boolean setItemMeta(SkullMeta itemMeta) {
		return super.setItemMeta(itemMeta);
	}

	private static class SkullChangeScheduler implements Runnable {

		private static Field texturesField = Reflections.getField(SkullMeta.class, "profile");
		
		private PlayerSkull skull;
		
		public SkullChangeScheduler(PlayerSkull skull) {
			this.skull = skull;
		}

		@Override
		public void run() {
			// Get full GameProfile from Owner
			GameProfile profile = getProfile();
			// Set field into Skull
			Reflections.setField(texturesField, skull.getItemMeta(), profile);
		}
		
		private GameProfile getProfile() {
			// Get profile from cache
			GameProfile profile = profileCache.get(skull.getOwner().getUniqueId());
			// If it does not exist
			if(profile == null) {
				// Check if player is online and if it's true, grab his GameProfile
				profile = skull.getOwner().isOnline() ? Reflections.getGameProfile((Player) skull.getOwner()) : null;
				// If the Player is offline
				if(profile == null) {
					String name = null;
					UUID uuid = null;
					// Download Mojang-UUID from MojangAPI
					try(InputStream in = new URL("https://api.mojang.com/users/profiles/minecraft/").openStream()) {
						// Convert site into JsonObject
						JsonObject source = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
						// Get Values
						uuid = fromTrimmed(source.get("id").getAsString());
						name = source.get("name").getAsString();
					} catch (IOException e) {
						Bukkit.getLogger().log(Level.SEVERE, "Could not get UUID from Player " + skull.getOwner().getName() + ". Use Alex-Skin...", e);
						// Use Steve-UUID by error
						uuid = UUID.fromString("c06f8906-4c8a-4911-9c29-ea1dbd1aab82");
						name = "MHF_STEVE";
					}
					// Put values into valid GameProfile
					profile = getUsableGameProfile(new GameProfile(uuid, name));
				}
				// Remove old data linked with ID
				profileCache.remove(skull.getOwner().getUniqueId());
				// Link new data with the Owner
				profileCache.put(skull.getOwner().getUniqueId(), profile);
				// Set Skulls ownerProfile attribute
				setOwnerProfile(profile);
			}
			return profile;
		}
		
		private GameProfile getUsableGameProfile(@Nonnull GameProfile body) {
			// If there was no error by getting the ID
			if(!"MHF_STEVE".equalsIgnoreCase(body.getName())) {
				try {
					// Create Settings for SessionServers
					URLConnection connection = new URL(
							"https://sessionserver.mojang.com/session/minecraft/profile/" 
							+ body.getId().toString().replaceAll("-", "") + "?unsigned=false").openConnection();
					connection.setUseCaches(false);
					connection.setDefaultUseCaches(false);
					connection.setDoInput(true);
					connection.setDoOutput(true);
					connection.addRequestProperty("Content-Type", "application/json");
					connection.addRequestProperty("User-Agent", "Mozilla/5.0");
					connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
					connection.addRequestProperty("Pragma", "no-cache");
					
					try(InputStream in = connection.getInputStream()) {
						// Convert relevant Data to JsonObject
						JsonObject obj = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
						String value = obj.get("value").getAsString();
						String signature = obj.get("signature").getAsString();
						
						// Delete old properties
						body.getProperties().clear();
						// Build property into PropertyMap
						body.getProperties().put("textures", new Property("textures", value, signature));
					}
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, "Cannot create Connection to SessionServer", e);
				}
			}
			return body;
		}
		
		private UUID fromTrimmed(String trimmedUUID) {
			if(trimmedUUID != null) {
				StringBuilder builder = new StringBuilder(trimmedUUID.trim());
				builder.insert(20, "-");
				builder.insert(16, "-");
				builder.insert(12, "-");
				builder.insert(8, "-");
				return UUID.fromString(builder.toString());
			}
			return null;
		}
		
		private void setOwnerProfile(GameProfile ownerProfile) {
			skull.ownerProfile = ownerProfile;
		}
	}
}
