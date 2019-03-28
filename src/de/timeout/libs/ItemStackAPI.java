package de.timeout.libs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.common.collect.Lists;

public final class ItemStackAPI {
	
	private static final String AMOUNT = "amount";
	
	/**
	 * Instantiates a new item stack API.
	 */
	private ItemStackAPI() {}
	
	/**
	 * Creates the item stack.
	 *
	 * @param material the material
	 * @param amount the amount
	 * @return the item stack
	 */
	public static ItemStack createItemStack(Materials material, int amount) {
		return createItemStack(material, amount, null);
	}
	
	/**
	 * Creates the item stack.
	 *
	 * @param material the material
	 * @param amount the amount
	 * @param name the name
	 * @return the item stack
	 */
	public static ItemStack createItemStack(Materials material, int amount, String name) {
		return createItemStack(material, (short) 0, amount, name);
	}

	/**
	 * Creates the item stack.
	 *
	 * @param material the material
	 * @param subid the subid
	 * @param amount the amount
	 * @param name the name
	 * @return the item stack
	 */
	public static ItemStack createItemStack(Materials material, short subid, int amount, String name) {
		ItemStack item = new ItemStack(material.material(), amount, subid);
		if	(name != null && material != Materials.AIR) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta(meta);
		}
		return item;
	}
	
	/**
	 * Enchant item.
	 *
	 * @param item the item
	 * @param ench the ench
	 * @param level the level
	 * @param removeEnchantment the remove enchantment
	 */
	public static void addEnchantment(ItemStack item, Enchantment ench, int level) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(ench, level, false);
		item.setItemMeta(meta);
	}
	
	/**
	 * Hide Enchantments on ItemStack
	 * @param item the item
	 */
	public static void hideEnchantments(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
	}
	
	/**
	 * Read item stack.
	 *
	 * @param section the section
	 * @return the item stack
	 */
	public static ItemStack readItemStack(ConfigurationSection section) {
		return readItemStack(section, null);
	}
	
	/**
	 * Read item stack.
	 *
	 * @param section the section
	 * @param lore the lore
	 * @return the item stack
	 */
	public static ItemStack readItemStack(ConfigurationSection section, List<String> lore) {
		return readItemStack(null, section, lore);
	}
	
	/**
	 * Read item stack.
	 *
	 * @param name the name
	 * @param section the section
	 * @param lore the lore
	 * @return the item stack
	 */
	public static ItemStack readItemStack(String name, ConfigurationSection section, List<String> lore) {
		ItemStack item = null;
		Materials material = Materials.valueOf(section.getString("material"));
		short subID = (short) section.getInt("subid");
		
		if((material != Materials.SKULL_ITEM && subID != 3) || name == null) 
			item = createItemStack(material, subID, section.getInt(AMOUNT) > 0 ? section.getInt(AMOUNT) : 1, ChatColor.translateAlternateColorCodes('&', section.getString("name")));
		else
			try {
				item = Skull.getSkull(name, ChatColor.translateAlternateColorCodes('&', section.getString("name")));
			} catch (IllegalAccessException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Cannot generate Skull", e);
			}
		
		if(item != null && lore != null && !lore.isEmpty()) {
			for(int i = 0; i < lore.size(); i++) lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
			
			ItemMeta meta = item.getItemMeta();
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		return item;
	}
	
	/**
	 * Removes enchantments.
	 *
	 * @param item the item
	 */
	public static void removeEnchantments(ItemStack item) {
		item.getEnchantments().keySet().forEach(item::removeEnchantment);
	}
	
	/**
	 * Sets the lore.
	 *
	 * @param item the item
	 * @param lorelines the lines of the lore
	 */
	public static void setLore(ItemStack item, String... lorelines) {
		List<String> list = Lists.newArrayList(lorelines);
		
		ItemMeta meta = item.getItemMeta();
		meta.setLore(list);
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
}
