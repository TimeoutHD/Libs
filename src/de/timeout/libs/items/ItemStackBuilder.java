package de.timeout.libs.items;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackBuilder {

	private ItemStack currentBuilding;
	
	public ItemStackBuilder() {
		this.currentBuilding = ItemStackAPI.createItemStack(Material.STONE);
	}
	
	public ItemStackBuilder(ItemStack base) {
		// validate
		Validate.notNull(base, "ItemStack cannot be null");
		this.currentBuilding = base.clone();
	}
	
	/**
	 * This method converts the builder into an ItemStack and returns it
	 * @return the itemstack
	 */
	public ItemStack toItemStack() {
		return currentBuilding;
	}
	
	/**
	 * This method 
	 * @param displayName
	 * @return
	 */
	public ItemStackBuilder setDisplayName(String displayName) {
		// set DisplayName
		ItemMeta meta = currentBuilding.getItemMeta();
		meta.setDisplayName(displayName);
		currentBuilding.setItemMeta(meta);
		// return this to continue
		return this;
	}
	
	public ItemStackBuilder addEnchantment(Enchantment enchantment, int level) {
		// set enchantment
		ItemStackAPI.addEnchantment(currentBuilding, enchantment, level);
		// return this to continue
		return this;
	}
	
	public ItemStackBuilder removeEnchantment(Enchantment enchantment) {
		// remove enchantment
		ItemMeta meta = currentBuilding.getItemMeta();
		meta.removeEnchant(enchantment);
		currentBuilding.setItemMeta(meta);
		// return this to continue
		return this;
	}
}
