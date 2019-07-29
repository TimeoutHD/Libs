package de.timeout.libs.gui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import de.timeout.libs.items.ItemStackAPI;

public class GUI implements Listener {
	
	private static final ItemStack n = ItemStackAPI.createItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7,"&7");
	private static final HashMap<HumanEntity, GUI> openGUIs = new LinkedHashMap<>(128);
	
	protected String name;
	protected Inventory design;
	protected Button[] buttons;
	
	public GUI(JavaPlugin main, String name, Inventory design) {
		// Object params cannot be null
		Validate.notNull(main, "MainClass cannot be null");
		Validate.notNull(name, "Name cannot be null");
		Validate.notNull(design, "Design cannot be null");
		
		this.name = name;
		this.design = design;
		this.buttons = new Button[design.getSize()];
		
		// put on every empty slot a n-item
		for(int i = 0; i < design.getSize(); i++)
			if(design.getItem(i) == null) design.setItem(i, n);
		
		// register Click-Listener
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	/**
	 * This Method add a Button to the GUI and the design of the Inventory.
	 * The ItemStack represents the design of the button in the GUI. It is recommend to use the {@link ItemStackAPI#createItemStack(Materials, int)}
	 * method to create an version independent ItemStack.
	 * 
	 * @param button the created Button
	 * 
	 * An example would be 
	 * <code>
	 * Button button = new Button(0, function -> {
	 *  TODO: What happen if the button is clicked.
	 * });
	 * gui.addButton(button);
	 * </code>
	 * 
	 */
	public void registerButton(int slot, Consumer<ButtonClickEvent> function) {
		buttons[slot] = new Button(slot, design.getItem(slot), function);
	}
	
	/**
	 * This Methods shows, if the Entity has open a GUI right now.
	 * 
	 * @param entity the Entity
	 * @return a bool, which answers, if the entity shows an GUI right now.
	 * true means, the entity shows at a gui right now.
	 * false means, the entity hasn't an open gui right now.
	 */
	public static boolean showsGUI(HumanEntity entity) {
		return openGUIs.containsKey(entity);
	}
	
	@EventHandler
	public void onButtonClick(InventoryClickEvent event) {
		// If there is no null param and player uses a gui right now
		if(event.getClickedInventory() != null && event.getCurrentItem() != null && openGUIs.containsKey(event.getWhoClicked())) {
			// cancel unnecesarry event
			event.setCancelled(true);
			// If Title is Similar to GUI
			if(event.getView().getTitle().equalsIgnoreCase(name)) {
				Button button = buttons[event.getSlot()];
				if(button != null) {
					ButtonClickEvent e = new ButtonClickEvent(event, button);
					// call ButtonClickEvent
					Bukkit.getServer().getPluginManager().callEvent(e);
					// execute function if event is not cancelled
					if(!e.isCancelled())button.click(e);
				}
			}
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if(openGUIs.containsKey(event.getPlayer()))event.setCancelled(true);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		if(openGUIs.containsKey(event.getPlayer()))openGUIs.remove(event.getPlayer());
	}
	
	/**
	 * This Method opend the GUI for one HumanEntity (Player).
	 * The Entity cannot be null.
	 * 
	 * @param player The Entity, which will open the GUI.
	 * 
	 * @throws NullPointerException if the Entity is null.
	 */
	public void openGUI(HumanEntity player) {
		Validate.notNull(player, "The Player cannot be null");
		player.openInventory(design);
		openGUIs.put(player, this);
	}

	public static class ButtonClickEvent extends Event implements Cancellable {

		private static HandlerList handlers = new HandlerList();
		
		private InventoryClickEvent inventoryClickEvent;
		private Button button;
		private boolean cancel;
		
		public ButtonClickEvent(InventoryClickEvent event, Button button) {
			this.button = button;
			this.inventoryClickEvent = event;
		}
		
		@Override
		public HandlerList getHandlers() {
			return handlers;
		}
		
		public static HandlerList getHandlerList() {
			return handlers;
		}

		/**
		 * Returns the result if the Event is Cancelled.
		 * @return if the Result is <b>true</b>,
		 * the Event is cancelled and will not execute the {@link Button#click(ButtonClickEvent)}-Method.
		 * If the result is <b>false</b> the Event is not cancelled and will call this method.
		 * 
		 */
		@Override
		public boolean isCancelled() {
			return cancel;
		}

		/**
		 * Changes the Cancel-Attribute. The attribute will get the same value as the Parameter.
		 * @param arg0 The new Cancel-Attribute as boolean. If the boolean is false the Event is not cancelled, else it's cancelled and
		 * will not call {@link Button#click(ButtonClickEvent)}-Method.
		 */
		@Override
		public void setCancelled(boolean arg0) {
			cancel = arg0;
		}

		/**
		 * Get the InventoryClickEvent. 
		 * 
		 * Every ButtonClickEvent is triggered directed after the InventoryClickEvent. 
		 * The Setter-Methods has no influence to the original Method.
		 * The Getter-Methods might be useful for future plans.
		 * 
		 * @return the original InventoryClickEvent
		 */
		public InventoryClickEvent getInventoryClickEvent() {
			return inventoryClickEvent;
		}

		/**
		 * Get the clicked Button.
		 * 
		 * It's important to get the Button as ButtonEvent.
		 * The Button contains the Design and the Function of future tasks.
		 * @return the clicked Button
		 */
		public Button getButton() {
			return button;
		}
	}
	
	public static class Button {
		
		private int slot;
		private ItemStack item;
		private Consumer<ButtonClickEvent> handler;
		
		public Button(int slot, ItemStack item, Consumer<ButtonClickEvent> function) {
			this.item = item;
			this.handler = function;
			this.slot = slot;
		}
		
		/**
		 * This Method calls the Method for the Function. 
		 * It will execute the functional part of the Button.
		 * This Method can changes while new Buttons will created.
		 * 
		 * @param event the ButtonClickEvent. This might be useful for future-task
		 */
		public void click(ButtonClickEvent event) {
			handler.accept(event);
		}
		
		/**
		 * This Method returns the Design of the Button and the clicked ItemStack.
		 * @return the clicked ItemStack (the design) of the Button.
		 */
		public ItemStack getDesign() {
			return item;
		}
		
		/**
		 * This Method returns the Slot of the Button in the general GUI.
		 * @return the Slot of the Button.
		 */
		public int getSlotInGUI() {
			return slot;
		}
	}
}
