package de.timeout.libs.items;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.bind.JsonTreeReader;

import de.timeout.libs.BukkitReflections;
import de.timeout.libs.Reflections;

/**
 * Utilities for ItemStacks
 * 
 * @author Timeout
 *
 */
public final class ItemStacks {
	
	private static final Class<?> itemClass = BukkitReflections.getNMSClass("Item");
	private static final Class<?> itemstackClass = BukkitReflections.getNMSClass("ItemStack");
	private static final Class<?> craftitemstackClass = BukkitReflections.getCraftBukkitClass("inventory.CraftItemStack");
	private static final Class<?> nbttagcompoundClass = BukkitReflections.getNMSClass("NBTTagCompound");
	private static final Class<?> localelanguageClass = BukkitReflections.getNMSClass("LocaleLanguage");
	
	private static final Method AS_NMS_COPY = Reflections.getMethod(craftitemstackClass, "asNMSCopy");
	private static final Method HAS_TAG = Reflections.getMethod(itemstackClass, "hasTag");
	private static final Method HAS_KEY = Reflections.getMethod(nbttagcompoundClass, "hasKey", String.class);
	private static final Method GET_TAG = Reflections.getMethod(nbttagcompoundClass, "getTag", String.class);
	private static final Method A = Reflections.getMethod(localelanguageClass, "a", String.class);
	private static final Method GET_NAME = Reflections.getMethod(itemClass, "getName");
	private static final Method GET_ITEM = Reflections.getMethod(itemstackClass, "getItem");
	
	private static final Object LOCALE_LANGUAGE = Reflections.getValue(Reflections.getField(localelanguageClass, "d"), localelanguageClass);
	
	private static final String ERROR_NO_NBT_TAG = "ItemStack has no NBT-Tag";
	private static final String ERROR_FAILED_GET_NBT_TAG = "Cannot get NMS-Copy of item ";
	
	private static final Gson GSON = new Gson();
	
	private ItemStacks() {
		// No need for Util-Class to create an Object
	}
	
	/**
	 * Encode item stack.
	 *
	 * @param item the item
	 * @return the string
	 */
	@Nullable
	public static String encodeBase64(ItemStack item) {
		try {
			ByteArrayOutputStream str = new ByteArrayOutputStream();
			try(BukkitObjectOutputStream data = new BukkitObjectOutputStream(str)) {
				data.writeObject(item);
			}
			return Base64.getEncoder().encodeToString(str.toByteArray());
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not create String", e);
		}
		return null;
	}
	
	/**
	 * Decode item stack.
	 *
	 * @param base64 the base 64
	 * @return the item stack
	 */
	@Nullable
	public static ItemStack decodeBase64(String base64) {
		try {
			ByteArrayInputStream str = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
			try(BukkitObjectInputStream data = new BukkitObjectInputStream(str)) {
				return (ItemStack) data.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not create Object", e);
		}
		return null;
	}
	
	/**
	 * Encodes an ItemStack into a JSON-Object
	 * @param item the itemstack you want to encode
	 * @return the json object of the itemstack. Cannot be null
	 */
	@Nonnull
	public static JsonObject encodeJson(ItemStack item) {
		return new JsonParser().parse(GSON.toJson(item.serialize())).getAsJsonObject();
	}
	
	/**
	 * Decodes an JsonObject of an ItemStack into the ItemStack
	 * @param data the json data of the ItemStack
	 * @return the ItemStack
	 */
	@Nonnull
	public static ItemStack decodeJson(JsonObject data) {
		return ItemStack.deserialize(GSON.fromJson(new JsonTreeReader(data), Map.class));
	}
	
	@Nonnull
	public static String getCustomizedName(ItemStack itemStack) {
		// return displayname if item has one
		if(!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) {
			// get nmsItem
			Object nmsItem = getNMSItem(itemStack);
			
			// only continue if the item could be found
			if(nmsItem != null) {
				try {
					return (String) A.invoke(LOCALE_LANGUAGE, GET_NAME.invoke(nmsItem));
				} catch (IllegalAccessException | InvocationTargetException e) {
					Bukkit.getLogger().log(Level.WARNING, "Unable to get name of itemstack. Continue with normal name");
				}
			}
			
			// return ItemStack name if no name could be found
			return WordUtils.capitalize(itemStack.getType().toString());
		} else return itemStack.getItemMeta().getDisplayName();
		

		
	}
	
	/**
	 * Returns an NMS-Copy of the itemstack as object
	 * @param item the item you want to copy
	 * @return the nms itemstack as object type
	 */
	@Nullable
	public static Object asNMSCopy(ItemStack item) {
		try {
			return AS_NMS_COPY.invoke(craftitemstackClass, item);
		} catch (IllegalAccessException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Unable to create NMS-Copy of an itemstack: ", e);
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Invalid argument format: ", e);
		} catch (InvocationTargetException e) {
			Bukkit.getLogger().log(Level.WARNING, "Invocated target: ", e);
		} catch (SecurityException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Security error while accessing with reflections: ", e);
		}
		
		return null;
	}
	
	@Nullable
	public static Object getNMSItem(ItemStack item) {
		// get nms item
		try {
			return GET_ITEM.invoke(asNMSCopy(item));
		} catch (IllegalAccessException e) {
			Bukkit.getLogger().log(Level.WARNING, "Unable to access ItemStack#getItem in NMS ItemStack", e);
		} catch (InvocationTargetException e) {
			Bukkit.getLogger().log(Level.WARNING, "Unable to access ItemStack#getItem. No such target", e);
		}
		
		return null;
	}
	
	@Nullable
	public static ItemStack asBukkitCopy(Object nmsItem) {
		try {
			return (ItemStack) craftitemstackClass.getMethod("asBukkitCopy", itemstackClass).invoke(craftitemstackClass, nmsItem);
		} catch (IllegalAccessException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Unable to create Bukkit-Copy of an itemstack: ", e);
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Invalid argument format: ", e);
		} catch (InvocationTargetException e) {
			Bukkit.getLogger().log(Level.WARNING, "Invocated target: ", e);
		} catch (NoSuchMethodException e) {
			Bukkit.getLogger().log(Level.WARNING, "Method does not exist: ", e);
		} catch (SecurityException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Security error while accessing with reflections: ", e);
		}
		
		return null;
	}
	
	@Nullable
	public static Object getNBTTagCompound(ItemStack item) {
		// create NMS itemstack
		Object nms = asNMSCopy(item);
		
		// return null if itemstack is null
		try {
			return (boolean) HAS_TAG.invoke(nms) ? GET_TAG.invoke(nms) : null;
		} catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
			Bukkit.getLogger().log(Level.WARNING, "Unable to check up NBT-TagCompound", e);
		}
		
		return null;
	}
	
	public static boolean hasNBTValue(ItemStack item, String key) {
		// get Compound
		Object compound = getNBTTagCompound(item);
		
		// only search if compound exists!
		if(compound != null) {
			try {		
				// return if key exist
				return (boolean) HAS_KEY.invoke(compound, key);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				Bukkit.getLogger().log(Level.SEVERE, ERROR_FAILED_GET_NBT_TAG + key, e);
			}
		}
			
		return false;
	}
	
	public static int getNBTIntValue(ItemStack item, String key) {
		return (int) getNBTValue(item, key, "getInt");
	}
	
	@Nullable
	public static String getNBTStringValue(ItemStack item, String key) {
		return (String) getNBTValue(item, key, "getString");
	}
	
	public static boolean getNBTBooleanValue(ItemStack item, String key) {
		return (boolean) getNBTValue(item, key, "getBoolean");
	}
	
	protected static Object getNBTValue(ItemStack item, String key, String methodName) {
		// create NMSCopy
		Object nms = asNMSCopy(item);
		
		try {
			// validate
			Validate.isTrue((boolean) HAS_TAG.invoke(nms), ERROR_NO_NBT_TAG);
			// get compound
			Object compound = GET_TAG.invoke(nms);
			
			// return value
			return nbttagcompoundClass.getMethod(methodName, String.class).invoke(compound, key);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			Bukkit.getLogger().log(Level.SEVERE, ERROR_FAILED_GET_NBT_TAG + key);
		}
		
		return null;
	}
}
