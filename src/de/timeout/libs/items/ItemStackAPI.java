package de.timeout.libs.items;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import de.timeout.libs.Reflections;
import net.md_5.bungee.api.ChatColor;

public final class ItemStackAPI {
	
	private static final Class<?> itemClass = Reflections.getNMSClass("Item");
	private static final Class<?> itemstackClass = Reflections.getNMSClass("ItemStack");
	private static final Class<?> craftitemstackClass = Reflections.getCraftBukkitClass("inventory.CraftItemStack");
	
	private ItemStackAPI() {
		// No need for Util-Class to create an Object
	}

	/**
	 * This method creates an ItemStack.
	 * @param material the material of your ItemStack
	 * @param amount the amount of your ItemStack. Can be negative, so be careful with negative values
	 * @param displayName the name of the ItemStack. ColorCodes are written with '&'
	 * @return the itemstack
	 */
	public static ItemStack createItemStack(Material material, int amount, String displayName) {
		return createItemStack(material, amount, (short) 0, displayName);
	}
	
	/**
	 * This method creates an ItemStack
	 * @param material the material of your ItemStack
	 * @param amount the amount of your itemstack. Can be negative, so be carefull with negative values
	 * @param damage the Damage / SubID of your ItemStack
	 * @param displayName the name of the ItemStack. ColorCodes are written with '&'
	 * @return the itemstack
	 */
	public static ItemStack createItemStack(Material material, int amount, short damage, String displayName) {
		// Create ItemStack
		ItemStack item = new ItemStack(material, amount, damage);
		
		// Set DisplayName with ColorCode if displayName is not null
		if(displayName != null) {
			// Get ItemMeta
			ItemMeta meta = item.getItemMeta();
			
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

			// Overwrite ItemMeta in item
			item.setItemMeta(meta);
		}
		return item;
	}
	
	/**
	 * This method creates an ItemStack with the amount of 1 and without a displayname
	 * @param material the material of an ItemStack
	 * @return the material of the itemstack
	 */
	public static ItemStack createItemStack(Material material) {
		return createItemStack(material, 1, null);
	}
	
	/**
	 * This method creates an ItemStack without a displayname
	 * @param material the material of the itemstack
	 * @param amount the amount 
	 * @return the itemstack
	 */
	public static ItemStack createItemStack(Material material, int amount) {
		return createItemStack(material, amount, null);
	}
	
	/**
	 * This method creates an ItemStack without a displayname
	 * @param material the material of the itemstack
	 * @param amount the amount
	 * @param damage the Damage / SubID of your ItemStack
	 * @return the itemstack
	 */
	public static ItemStack createItemStack(Material material, int amount, short damage) {
		return createItemStack(material, amount, damage, null);
	}

	/**
	 * This method adds an Enchantment to an ItemStack
	 * @param item the ItemStack
	 * @param enchantment the enchantment
	 * @param level the level
	 * @throws IllegalArgumentException if the level is negative or 0
	 */
	public static void addEnchantment(ItemStack item, Enchantment enchantment, int level) {
		// level must be positive
		if(level > 0) {
			// get ItemMeta
			ItemMeta meta = item.getItemMeta();
			// add Enchantment
			meta.addEnchant(enchantment, level, true);
			// set new ItemMeta
			item.setItemMeta(meta);
		} else throw new IllegalArgumentException("Level must be a positive integer");
	}
	
	/**
	 * This method hides all Enchantments on an ItemStack
	 * @param item the itemstack
	 */
	public static void hideEnchantments(ItemStack item) {
		// get ItemMeta
		ItemMeta meta = item.getItemMeta();
		// hide enchantments
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		// set itemMeta
		item.setItemMeta(meta);
	}
	
	/**
	 * Set the lore of an ItemStack
	 * @param item the itemstack
	 * @param lines the lore as Strings
	 */
	public static void setLore(ItemStack item, String... lines) {
		setLore(item, Arrays.asList(lines));
	}
	
	/**
	 * Set the lore of an ItemStack
	 * @param item the itemstack
	 * @param lore the lore as list
	 */
	public static void setLore(ItemStack item, List<String> lore) {
		// Apply ColorCode
		for(int i = 0; i < lore.size(); i++)
			if(lore.get(i) != null) lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
		
		// Get Meta
		ItemMeta meta = item.getItemMeta();
		// Set Lore
		meta.setLore(lore);
		// Set ItemMeta
		item.setItemMeta(meta);
	}
	
	/**
	 * Encode item stack.
	 *
	 * @param item the item
	 * @return the string
	 */
	public static String encodeItemStack(ItemStack item) {
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
	public static ItemStack decodeItemStack(String base64) {
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
	
	public static String getCustomizedName(ItemStack itemStack) {
		// return displayname if item has one
		if(itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) return itemStack.getItemMeta().getDisplayName();
		try {
			// else get right name
			Object nmsItemStack = craftitemstackClass.getMethod("asNMSCopy", ItemStack.class);
			Object nmsItem = craftitemstackClass.getMethod("getItem").invoke(nmsItemStack);
			// return name
			return (String) itemClass.getMethod("a", itemstackClass).invoke(nmsItem, nmsItemStack);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot get Item");
		}
		return itemStack.getType().toString();
	}
}
