package de.timeout.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public enum Skull {

    ARROW_LEFT("MHF_ArrowLeft"),
    ARROW_RIGHT("MHF_ArrowRight"),
    ARROW_UP("MHF_ArrowUp"),
    ARROW_DOWN("MHF_ArrowDown"),
    QUESTION("MHF_Question"),
    EXCLAMATION("MHF_Exclamation"),
    CAMERA("FHG_Cam"),

    ZOMBIE_PIGMAN("MHF_PigZombie"),
    PIG("MHF_Pig"),
    SHEEP("MHF_Sheep"),
    BLAZE("MHF_Blaze"),
    CHICKEN("MHF_Chicken"),
    COW("MHF_Cow"),
    SLIME("MHF_Slime"),
    SPIDER("MHF_Spider"),
    SQUID("MHF_Squid"),
    VILLAGER("MHF_Villager"),
    OCELOT("MHF_Ocelot"),
    HEROBRINE("MHF_Herobrine"),
    LAVA_SLIME("MHF_LavaSlime"),
    MOOSHROOM("MHF_MushroomCow"),
    GOLEM("MHF_Golem"),
    GHAST("MHF_Ghast"),
    ENDERMAN("MHF_Enderman"),
    CAVE_SPIDER("MHF_CaveSpider"),

    CACTUS("MHF_Cactus"),
    CAKE("MHF_Cake"),
    CHEST("MHF_Chest"),
    MELON("MHF_Melon"),
    LOG("MHF_OakLog"),
    PUMPKIN("MHF_Pumpkin"),
    TNT("MHF_TNT"),
    DYNAMITE("MHF_TNT2");
	
    private static final Base64 base64 = new Base64();
    private static final String TEXTURES = "textures";
    
    private String id;

    private Skull(String id) {
        this.id = id;
    }

    public static ItemStack getCustomSkull(String url) throws IllegalAccessException {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) {
            throw new IllegalStateException("Profile doesn't contain a property map");
        }
        byte[] encodedData = base64.encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        propertyMap.put(TEXTURES, new Property(TEXTURES, new String(encodedData)));
        ItemStack head = new ItemStack(Materials.SKULL_ITEM.material(), 1, (short) 3);
        ItemMeta headMeta = head.getItemMeta();
        Class<?> headMetaClass = headMeta.getClass();
        Reflections.getField(headMetaClass, "profile", GameProfile.class).set(headMeta, profile);
        head.setItemMeta(headMeta);
        return head;
    }
    
	public static ItemStack getSkull(GameProfile profile, String display) {
		try {
			Property property = profile.getProperties().get(TEXTURES).iterator().next();
			String value = property.getValue();
			String base64decoded = new String(Base64.decodeBase64(value));
			JsonObject json = new JsonParser().parse(base64decoded).getAsJsonObject();
			String url = json.get(TEXTURES).getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
			ItemStack skull = Skull.getCustomSkull(url);
			
			ItemMeta skullMeta = skull.getItemMeta();
			skullMeta.setDisplayName(display);
			skull.setItemMeta(skullMeta);
			return skull;
		} catch (IllegalAccessException e) {}	
		return ItemStackAPI.createItemStack(Materials.SKULL_ITEM, (short) 3, 1, "");
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getSkull(String name, String display) {
		GameProfile profile = Bukkit.getServer().getOfflinePlayer(name).isOnline() ? Reflections.getGameProfile(Bukkit.getServer().getPlayer(name)) : null;
		if(profile == null) {
			try(InputStream in = new URL("https://api.mojang.com/users/profiles/minecraft/" + name.toLowerCase()).openStream()) {
				InputStreamReader reader = new InputStreamReader(in);
				if(reader != null) {
					String trimmedUUID = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();
					UUID uuid = fromTrimmed(trimmedUUID);
					profile = new GameProfile(uuid, name);
					
					try(InputStream session = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + trimmedUUID + "?unsigned=false").openStream()) {
						JsonObject obj = new JsonParser().parse(new InputStreamReader(session)).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
						String value = obj.get("value").getAsString();
						String signature = obj.get("signature").getAsString();
						
						profile.getProperties().put(TEXTURES, new Property(TEXTURES, value, signature));
					}
				}
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Could not connect to MojangAPI & SessionServers", e);
			}
		}
		if(profile != null)return getSkull(profile, display);
		throw new NullPointerException();
	}
	
	public static ItemStack loadSkullFromGameprofileValue(String display, String value) {
		try {
			String base64decoded = new String(Base64.decodeBase64(value));
			JsonObject json = new JsonParser().parse(base64decoded).getAsJsonObject();
			String url = json.get(TEXTURES).getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
			ItemStack skull = Skull.getCustomSkull(url);
			
			ItemMeta meta = skull.getItemMeta();
			meta.setDisplayName(display);
			skull.setItemMeta(meta);
			return skull;
		} catch(IllegalAccessException e) {}
		return ItemStackAPI.createItemStack(Materials.SKULL_ITEM, (short) 3, 1, display);
	}
	
	private static UUID fromTrimmed(String trimmedUUID) {
		if(trimmedUUID != null) {
			StringBuilder builder = new StringBuilder(trimmedUUID.trim());
			try {
			    builder.insert(20, "-");
			    builder.insert(16, "-");
			    builder.insert(12, "-");
			    builder.insert(8, "-");
			} catch (StringIndexOutOfBoundsException e){}
			return UUID.fromString(builder.toString());
		}
		return null;
	}
    
    public String getId() {
        return id;
    }
    
    public static class Reflections {
    	
    	public static Class<?> getCraftBukkitClass(String clazz) {
    		try {
    			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
    			String name = "org.bukkit.craftbukkit." + version + clazz;
    			return Class.forName(name);
    		} catch (ClassNotFoundException e) {
    			Bukkit.getLogger().log(Level.WARNING, "Could not find CraftBukkit-Class " + clazz, e);
    		}
    		return null;
    	}
    	
    	public static <T> Field getField(Class<?> target, String name, Class<T> fieldtype) {
    		for(Field field : target.getDeclaredFields()) {
    			if((name == null || field.getName().equals(name)) && fieldtype.isAssignableFrom(field.getType())) {
    				field.setAccessible(true);
    				return field;
    			}
    		}
    		return null;
    	}
    	
    	public static GameProfile getGameProfile(Player player) {
   		 	try {
   		 		Class<?> craftplayerClass = getCraftBukkitClass("entity.CraftPlayer");
   		 		return craftplayerClass != null ? (GameProfile) craftplayerClass.getMethod("getProfile").invoke(player) : null;
   		 	} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
   				| SecurityException e) {
   		 		Bukkit.getLogger().log(Level.INFO, "Could not get GameProfile from Player " + player.getName(), e);
   		 	}
   		 	return new GameProfile(player.getUniqueId(), player.getName());
    	}
    }
}