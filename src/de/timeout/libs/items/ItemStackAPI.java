package de.timeout.libs.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public final class ItemStackAPI {
	
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
		// Create Item
		ItemStack item = new ItemStack(material, amount);
		
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
	 * @return
	 */
	public static ItemStack createItemStack(Material material, int amount) {
		return createItemStack(material, amount, null);
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
}
