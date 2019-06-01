package de.timeout.libs.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public final class ItemStackAPI {

	public static ItemStack createItemStack(Material material, int amount, String displayName) {
		// Create Item
		ItemStack item = new ItemStack(material, amount);
		
		// Get ItemMeta
		ItemMeta meta = item.getItemMeta();
		// Set DisplayName with ColorCode
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		// Overwrite ItemMeta in item
		item.setItemMeta(meta);
		
		return item;
	}
}
