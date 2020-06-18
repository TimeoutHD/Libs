package de.timeout.libs.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import de.timeout.libs.gui.event.ButtonClickEvent;
import de.timeout.libs.gui.event.GUICloseEvent;
import de.timeout.libs.gui.event.GUIOpenEvent;
import de.timeout.libs.items.ItemStackAPI;
import net.md_5.bungee.api.ChatColor;

public class GUI {
	
	private static final GUIHandler handler = new GUIHandler();

	protected final List<InventoryView> viewers = new ArrayList<>();
	protected final List<GUIInteractable<?>> interactors;
	
	protected UUID uuid;
	protected Material background;
	protected ItemStack[] design;
	protected Consumer<GUICloseEvent> closeAction;
	
	/**
	 * This constructor creates a new gui with a certain design. Note that every itemstack is not a button.
	 * You must initialize your buttons first with the Method 
	 * @param design
	 */
	public GUI(@Nonnull Inventory design) {
		this(design, Material.GRAY_STAINED_GLASS_PANE);
	}
	
	public GUI(@Nonnull Inventory design, Material background) {
		this(design, background, null);
	}

	
	public GUI(@Nonnull Inventory design, Material background, Consumer<GUICloseEvent> event) {
		// Validate
		Validate.notNull(design, "Inventory-Design cannot be null");
		
		// initialize design and slot for Buttons
		this.background = Optional.ofNullable(background).orElse(Material.GRAY_STAINED_GLASS_PANE);
		this.design = new ItemStack[design.getSize()];
		this.interactors = Arrays.asList(new GUIInteractable[design.getSize()]);
		
		// apply design
		for(int i = 0; i < design.getSize(); i++) {
			setItem(i, design.getItem(i));
		}
	}
	
	/**
	 * Sets an item into the gui. Works also with Buttons and Levers
	 * @param slot the slot of the item
	 * @param item the item or button itself
	 */
	public void setItem(@Nonnegative int slot, ItemStack item) {
		// check if slot is valid
		if(slot >= 0 && slot < design.length) {
			// insert background if item is null
			if(item != null) {
				// add interact function if exists. Else delete old one
				if(item instanceof GUIInteractable) {
					// add function 
					this.interactors.set(slot, (GUIInteractable<?>) item);
				} else this.interactors.set(slot, null);
				
				// add item
				this.design[slot] = item;
			} else this.design[slot] = ItemStackAPI.createItemStack(background, 1, ChatColor.translateAlternateColorCodes('&', "&7"));
		} else throw new IndexOutOfBoundsException(String.format("Slot index out of range: %d", slot));
	}
	
	/**
	 * Returns the itemstack of a certain position
	 * @param slot the position itself
	 * @return the itemstack on that position
	 */
	public ItemStack getItem(int slot) {
		// check if slot is valid
		if(slot >= 0 && slot < design.length) {
			// returns the itemstack itself
			return design[slot].clone();
		} else throw new IndexOutOfBoundsException(String.format("Slot index out of range: %d", slot));
	}
	
	/**
	 * This method creates a new button on a certain position without changing the design at this position.
	 * @param slot the slot of this button
	 * @param click the action what will happen when a player clicks on it.
	 * @throws IllegalArgumentException if the slot is out of range
	 * 
	 * An example will be
	 * <code>
	 * GUI.registerButton(0, click -> {
	 *     TODO: What happen when a player click the button
	 * });
	 * </code>
	 * 
	 * @deprecated Use GUI#setItem(slot, button) instead 
	 */
	@Deprecated
	public void registerButton(@Nonnegative int slot, Consumer<ButtonClickEvent> click) {
		setItem(slot, new Button(getItem(slot), click));
	}
	
	/**
	 * This Method creates a new Button in this GUI.
	 * The ItemStack represents the design of the button.
	 * 
	 * @param slot the slot where the button should be
	 * @param item the ItemStack-Design of the Button
	 * @param click what happens when a player clicks the button
	 * @throws IllegalArgumentException if the design is null or the slot is out of range
	 * 
	 * An example would be
	 * <code>
	 * registerButton(0, ItemStackAPI.createItemStack(Material.GRASS), click -> {
	 *     TODO: What happens if the button is clicked
	 * });
	 * </code>
	 * 
	 * @deprecated Use GUI#setItem(slot, button) instead 
	 */
	@Deprecated
	public void registerButton(@Nonnegative int slot, ItemStack design, Consumer<ButtonClickEvent> click) {
		setItem(slot, new Button(design, click));
	}
	
	/**
	 * This method links an already existing button on a certain slot
	 * @param slot the slot where the button should be
	 * @param button the button itself
	 * @throws IllegalArgumentException if the button is null or the slot is out of range
	 * 
	 * @deprecated Use GUI#setItem(slot, button) instead 
	 */
	@Deprecated
	public void registerButton(@Nonnegative int slot, Button button) {
		// call method
		this.registerButton(slot, button.clone(), button.getClickFunction());
	}
	
	/**
	 * This method updates the gui for every player
	 */
	public void updateGUI() {
		viewers.forEach(viewer -> this.openGUI(viewer.getPlayer(), viewer.getTitle()));
	}
	
	/**
	 * This method opens this gui for a player
	 * @param player the player who wants to open this gui
	 * @throws IllegalArgumentException if the player is null
	 */
	public void openGUI(HumanEntity player, String name) {
		// check if event is not cancelled
		GUIOpenEvent event = handler.onGUIOpen(player, this, name);
		if(!event.isCancelled()) {
			// open inventory
			InventoryView view = event.getPlayer().openInventory(createGUI(event.getName()));
			
			// if view is not null
			if(view != null) {		
				// add to viewers if all succeed 
				this.viewers.add(view);
			} else handler.onGUIOpenAbort(event.getPlayer());
		}
	}
	
	/**
	 * This method returns the uniqueID of this GUI
	 * @return
	 */
	public UUID getUniqueID() {
		return uuid;
	}
	
	/**
	 * This method returns the design of the gui
	 * @return
	 */
	public Inventory getDesign() {
		return createGUI(null);
	}
	
	/**
	 * Returns the name of the gui in view of the player
	 * 
	 * @param viewer the original viewer of the gui
	 * @return the name you want to get. Returns null if the viewer does not have this gui open
	 * @throws IllegalArgumentException if the viewer is null
	 * 
	 */
	public String getName(@Nonnull HumanEntity viewer) {
		// Validate
		Validate.notNull(viewer, "Viewer cannot be null");
		
		// check if player sees this gui
		return viewers.contains(viewer.getOpenInventory()) ? viewer.getOpenInventory().getTitle() : null;
	}
	
	/**
	 * Creates a new gui of this type with a new name
	 * @param name the name of the gui
	 * @return the gui itself
	 */
	private Inventory createGUI(String name) {
		// Create new inventory
		Inventory inv = Bukkit.createInventory(null, design.length, name);
		// add all designs to inv
		for(int i = 0; i < design.length; i++) inv.setItem(i, getItem(i));
		
		return inv;
	}
	
	/**
	 * This method sets the close function of the gui. null deletes the close function
	 * @param event the new closefunction or null
	 */
	public void setCloseFunction(Consumer<GUICloseEvent> event) {
		this.closeAction = event;
	}
}
