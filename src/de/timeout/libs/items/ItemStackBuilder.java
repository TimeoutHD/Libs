package de.timeout.libs.items;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nonnegative;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.timeout.libs.Reflections;

public class ItemStackBuilder {
	
	private static final Class<?> itemstackClass = Reflections.getNMSClass("ItemStack");
	private static final Class<?> nbttagcompoundClass = Reflections.getNMSClass("NBTTagCompound");
	private static final Class<?> craftitemstackClass = Reflections.getCraftBukkitClass("inventory.CraftItemStack");
	
	private static final String AS_BUKKIT_COPY = "asBukkitCopy";
	private static final String AS_NMS_COPY = "asNMSCopy";
	private static final String HAS_TAG = "hasTag";
	private static final String GET_TAG = "getTag";
	private static final String SET_TAG = "setTag";
	
	private static final String NBT_ERROR = "Cannot write NBT-Data in ";

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
	 * This method set the display name of the item
	 * @param displayName the display name
	 * @return the builder to continue
	 */
	public ItemStackBuilder setDisplayName(String displayName) {
		// set DisplayName
		ItemMeta meta = currentBuilding.getItemMeta();
		meta.setDisplayName(displayName);
		currentBuilding.setItemMeta(meta);
		// return this to continue
		return this;
	}
	
	/**
	 * This Method add an Enchantment to the Item with a certain level
	 * @param enchantment the Enchantment
	 * @param level the level
	 * @return the builder to continue
	 */
	public ItemStackBuilder addEnchantment(Enchantment enchantment, int level) {
		// set enchantment
		ItemStackAPI.addEnchantment(currentBuilding, enchantment, level);
		// return this to continue
		return this;
	}
	
	/**
	 * This Method removes an Enchantment from the Item
	 * @param enchantment the Enchant you want to remove
	 * @return the builder to continue
	 */
	public ItemStackBuilder removeEnchantment(Enchantment enchantment) {
		// remove enchantment
		ItemMeta meta = currentBuilding.getItemMeta();
		meta.removeEnchant(enchantment);
		currentBuilding.setItemMeta(meta);
		// return this to continue
		return this;
	}
	
	/**
	 * This Method sets the Lore of the Item
	 * @param lore the Lore you want to set
	 * @return the builder to continue
	 */
	public ItemStackBuilder setLore(List<String> lore) {
		// Set Lore for currentBuilding
		ItemStackAPI.setLore(currentBuilding, lore);
		// return this to continue
		return this;
	}
	
	/**
	 * This Method hides all enchantments in the lore
	 * @param result a bool which answers if you want to hide the enchantments. true means the enchantments will be hidden, false otherwise
	 * @return the builder to continue
	 */
	public ItemStackBuilder hideEnchantments(boolean result) {
		// get Meta
		ItemMeta meta = currentBuilding.getItemMeta();
		// show or hide enchantments
		if(result) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		else meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		// set Meta
		currentBuilding.setItemMeta(meta);
		// return this to continue
		return this;
	}
	
	/**
	 * This Method set the amount of the Item. The amount must be positive. 
	 * @param amount the amount
	 * @return the builder to continue
	 * @throws IllegalArgumentException if the amount is negative
	 */
	public ItemStackBuilder setAmount(@Nonnegative int amount) {
		// set Amount
		currentBuilding.setAmount(amount);
		// returng this to continue
		return this;
	}
	
	/**
	 * This Method writes the NBT-Tag with an Int as value in a certain key
	 * @param key the key of the tag
	 * @param value the value you want to write in this key
	 * @return the builder to continue
	 */
	public ItemStackBuilder writeNBTInt(String key, int value) {
		try {
			// create nms-copy
			Object nmsCopy = craftitemstackClass.getMethod(AS_NMS_COPY, ItemStack.class).invoke(craftitemstackClass, currentBuilding);
			// get tagcompound
			Object compound = (boolean) itemstackClass.getMethod(HAS_TAG).invoke(nmsCopy) ? 
				itemstackClass.getMethod(GET_TAG).invoke(nmsCopy) : nbttagcompoundClass.newInstance();
			// write int in compound
			nbttagcompoundClass.getMethod("setInt", String.class, int.class).invoke(compound, key, value);
			// set TagCompound in Item
			itemstackClass.getMethod(SET_TAG, nbttagcompoundClass).invoke(nmsCopy, compound);
			// safe new itemstack
			currentBuilding = (ItemStack) craftitemstackClass.getMethod(AS_BUKKIT_COPY, itemstackClass).invoke(craftitemstackClass, nmsCopy);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			Bukkit.getLogger().log(Level.SEVERE, NBT_ERROR + key, e);
		}
		// return this to continue
		return this;
	}
	
	/**
	 * This Method writes the NBT-Tag with an Boolean as value in a certain key
	 * @param key the key of the tag
	 * @param value the value you want to write in this key
	 * @return the builder to continue
	 */
	public ItemStackBuilder writeNBTBoolean(String key, boolean value) {
		try {
			// create nms-copy
			Object nmsCopy = craftitemstackClass.getMethod(AS_NMS_COPY, ItemStack.class).invoke(craftitemstackClass, currentBuilding);
			// get tagcompound
			Object compound = (boolean) itemstackClass.getMethod(HAS_TAG).invoke(nmsCopy) ? 
				itemstackClass.getMethod(GET_TAG).invoke(nmsCopy) : nbttagcompoundClass.newInstance();
			// write int in compound
			nbttagcompoundClass.getMethod("setBoolean", String.class, boolean.class).invoke(compound, key, value);
			// set TagCompound in Item
			itemstackClass.getMethod(SET_TAG, nbttagcompoundClass).invoke(nmsCopy, compound);
			// safe new itemstack
			currentBuilding = (ItemStack) craftitemstackClass.getMethod(AS_BUKKIT_COPY, itemstackClass).invoke(craftitemstackClass, nmsCopy);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			Bukkit.getLogger().log(Level.SEVERE, NBT_ERROR + key, e);
		}
		// return this to continue
		return this;
	}
	
	/**
	 * This Method writes the NBT-Tag with an String as value in a certain key
	 * @param key the key of the tag
	 * @param value the value you want to write in this key
	 * @return the builder to continue
	 */
	public ItemStackBuilder writeNBTString(String key, String value) {
		try {
			// create nms-copy
			Object nmsCopy = craftitemstackClass.getMethod(AS_NMS_COPY, ItemStack.class).invoke(craftitemstackClass, currentBuilding);
			// get tagcompound
			Object compound = (boolean) itemstackClass.getMethod(HAS_TAG).invoke(nmsCopy) ? 
				itemstackClass.getMethod(GET_TAG).invoke(nmsCopy) : nbttagcompoundClass.newInstance();
			// write int in compound
			nbttagcompoundClass.getMethod("setString", String.class, boolean.class).invoke(compound, key, value);
			// set TagCompound in Item
			itemstackClass.getMethod(SET_TAG, nbttagcompoundClass).invoke(nmsCopy, compound);
			// safe new itemstack
			currentBuilding = (ItemStack) craftitemstackClass.getMethod(AS_BUKKIT_COPY, itemstackClass).invoke(craftitemstackClass, nmsCopy);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			Bukkit.getLogger().log(Level.SEVERE, NBT_ERROR + key, e);
		}
		// return this to continue
		return this;
	}
}
