package src.de.timeout.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.ChatColor;

public class ItemStackAPI {
	
	private ItemStackAPI() {}
	
	public static ItemStack createItemStack(Materials material, int amount) {
		return createItemStack(material, amount, null);
	}
	
	public static ItemStack createItemStack(Materials material, int amount, String name) {
		return createItemStack(material, (short) 0, amount, name);
	}

	public static ItemStack createItemStack(Materials material, short subid, int amount, String name) {
		ItemStack item = new ItemStack(material.material(), amount, subid);
		if(name != null && material != Materials.AIR) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta(meta);
		}
		return item;
	}
	
	public static void enchantItem(ItemStack item, Enchantment ench, int level, boolean removeEnchantment) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(ench, level, false);
		if(removeEnchantment)meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
	}
	
	public static ItemStack readItemStack(ConfigurationSection section) {
		return readItemStack(section, null);
	}
	
	public static ItemStack readItemStack(ConfigurationSection section, List<String> lore) {
		return readItemStack(null, section, lore);
	}
	
	public static ItemStack readItemStack(String name, ConfigurationSection section, List<String> lore) {
		ItemStack item;
		Materials material = Materials.valueOf(section.getString("material"));
		short subID = (short) section.getInt("subid");
		
		if((material != Materials.SKULL_ITEM && subID != 3) || name == null) 
			item = createItemStack(material, subID, section.getInt("amount") > 0 ? section.getInt("amount") : 1, ChatColor.translateAlternateColorCodes('&', section.getString("name")));
		else item = Skull.getSkull(name, ChatColor.translateAlternateColorCodes('&', section.getString("name")));
		
		if(lore != null && !lore.isEmpty()) {
			for(int i = 0; i < lore.size(); i++) lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
			
			ItemMeta meta = item.getItemMeta();
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		return item;
	}
	
	public static void removeEnchantments(ItemStack item) {
		item.getEnchantments().keySet().forEach(item::removeEnchantment);
	}
	
	public static void setLore(ItemStack item, String... lorelines) {
		List<String> list = Lists.newArrayList(lorelines);
		
		ItemMeta meta = item.getItemMeta();
		meta.setLore(list);
		item.setItemMeta(meta);
	}
	
	@SuppressWarnings("resource")
	public static String encodeItemStack(ItemStack item) {
		try {
			ByteArrayOutputStream str = new ByteArrayOutputStream();
			BukkitObjectOutputStream data = new BukkitObjectOutputStream(str);
			data.writeObject(item);
			return Base64.getEncoder().encodeToString(str.toByteArray());
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not create String", e);
		}
		return null;
	}
	
	@SuppressWarnings("resource")
	public static ItemStack decodeItemStack(String base64) {
		try {
			ByteArrayInputStream str = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
			BukkitObjectInputStream data = new BukkitObjectInputStream(str);
			return (ItemStack) data.readObject();
		} catch (IOException | ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not create Object", e);
		}
		return null;
	}
}

