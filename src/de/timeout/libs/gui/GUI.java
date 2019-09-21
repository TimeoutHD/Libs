package de.timeout.libs.gui;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import de.timeout.libs.items.ItemStackAPI;
import net.md_5.bungee.api.ChatColor;

/**
 * This class represents a gui for user interactions with a plugin
 * @author Timeout
 *
 */
public class GUI {

	private static GUIHandler handler;
	
	protected ItemStack n;
	
	protected UUID uniqueID;
	protected Inventory design;
	
	/**
	 * This constructor creates a new GUI without a design
	 */
	public GUI() {
		this((JavaPlugin) Bukkit.getPluginManager().getPlugins()[0]);
	}
	
	/**
	 * This constructor creates a new GUI without a design
	 * @deprecated Use {@link GUI} instead.
	 * @param main the mainclass of the plugin
	 */
	@Deprecated
	public GUI(JavaPlugin main) {
		// validate
		Validate.notNull(main, "Main cannot be null");
		// initialize final fields
		uniqueID = UUID.randomUUID();
		
		// initialize handlers if handler does not exist
		if(handler == null) handler = new GUIHandler(main);
	}
	
	/**
	 * This constructor creates a new gui with a certain design. Note that every itemstack is not a button.
	 * You must initialize your buttons first with the Method 
	 * @param design
	 */
	public GUI(Inventory design) {
		this(design, (short) 7);
	}
	
	@Deprecated
	public GUI(JavaPlugin main, String name, Inventory design) {
		this(main, name, design, (short) 7);
	}
	
	public GUI(Inventory design, @Nonnegative short nColor) {
		this();
		// Validate
		Validate.notNull(design, "Inventory-Design cannot be null");
		// reinitialize n
		this.n = ItemStackAPI.createItemStack(Material.STAINED_GLASS_PANE, 1, nColor, ChatColor.translateAlternateColorCodes('&', "&7"));
		// initialize design
		this.design = Bukkit.createInventory(null, design.getSize(), design.getTitle());
		// initialize design
		for(int i = 0; i < this.design.getSize(); i++) {
			// get ItemStack
			ItemStack item = this.design.getItem(i);
			// set to n if item is null.
			if(item == null) this.design.setItem(i, n);
		}

	}
	
	@Deprecated
	public GUI(JavaPlugin main, String name, Inventory design, @Nonnegative short nColor) {
		this(main);
		// Validate
		Validate.notNull(design, "Inventory-Design cannot be null");
		// reinitialize n
		this.n = ItemStackAPI.createItemStack(Material.STAINED_GLASS_PANE, 1, nColor, ChatColor.translateAlternateColorCodes('&', "&7"));
		// initialite design
		this.design = Bukkit.createInventory(null, design.getSize(), name != null ? name : design.getTitle());
		// initialize design
		for(int i = 0; i < this.design.getSize(); i++) {
			// get ItemStack
			ItemStack item = this.design.getItem(i);
			// set to n if item is null.
			if(item == null) this.design.setItem(i, n);
		}
	}
	
	@Deprecated
	public GUI(GUI base, JavaPlugin main) {
		this(main, base.getName(), base.getDesign(), base.n.getDurability());
	}
	
	/**
	 * This constructor generates a clone of a certain gui
	 * @param base the original gui 
	 */
	public GUI(GUI base) {
		this(base.design, base.n.getDurability());
	}
	
	/**
	 * This method returns the uniqueID of this GUI
	 * @return
	 */
	public UUID getUniqueID() {
		return uniqueID;
	}
	
	public String getName() {
		return design.getName();
	}
	
	public Inventory getDesign() {
		return design;
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
	 */
	public void registerButton(int slot, Consumer<ButtonClickEvent> click) {
		// Validate
		if(slot < 0 || slot >= this.design.getSize()) throw new IllegalArgumentException("slot must be in range of the gui slots");
		// set new button
		this.design.setItem(slot, new Button(this.design.getItem(slot), click));
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
	 */
	public void registerButton(ItemStack design, int slot, Consumer<ButtonClickEvent> click) {
		// Validate
		Validate.notNull(design, "The design of the button cannot be null");
		if(slot < 0 || slot >= this.design.getSize()) throw new IllegalArgumentException("slot must be in range of the gui slots");
		// set new button
		this.design.setItem(slot, new Button(design, click));
	}
	
	public void removeButton(int slot) {
		// validate
		if(slot < 0 || slot >= this.design.getSize()) throw new IllegalArgumentException("slot is out of range. Maximum is allowed " + (this.design.getSize() -1));
		// remove item
		design.setItem(slot, n);
		// update gui
		updateGUI();
	}
	
	/**
	 * This method returns a list of all viewers of this gui
	 * @return a list of all viewers
	 */
	public List<HumanEntity> getViewers() {
		return this.design.getViewers();
	}
	
	public void destroy() {
		// close Inventory for every viewer
		getViewers().forEach(HumanEntity::closeInventory);
		// delete inventory
		design = null;
		n = null;
		uniqueID = null;
	}
	
	/**
	 * This method updates the gui for every player
	 */
	public void updateGUI() {
		handler.updateGUI(uniqueID);
	}
	
	/**
	 * This method opens this gui for a player
	 * @param player the okayer who wants to open this gui
	 * @throws IllegalArgumentException if the player is null
	 */
	public void openGUI(HumanEntity player) {
		handler.openGUI(player, this);
	}
	
	/**
	 * This method checks if there is a Button on a certain slot
	 * @param slot the slot wou want to check
	 * @return if there is a button or not
	 */
	public boolean isButton(int slot) {
		return design.getItem(slot) instanceof Button;
	}
	
	/**
	 * This class handles every GUI for every player
	 * @author Timeout
	 *
	 */
	private static class GUIHandler implements Listener {
		
		private static final Map<HumanEntity, GUI> activeViewers = new ConcurrentHashMap<>();
		
		public GUIHandler(JavaPlugin main) {
			// Validate
			Validate.notNull(main, "Main Class cannot be null");
			// register Listener in first Plugin
			Bukkit.getPluginManager().registerEvents(this, main);
		}

		@EventHandler(priority = EventPriority.MONITOR)
		public void onInventoryClick(InventoryClickEvent event) {
			// validate
			if(event.getClickedInventory() != null && event.getCurrentItem() != null && activeViewers.containsKey(event.getWhoClicked())) {
				// gui is clicked. Cancel event
				event.setCancelled(true);
				// if item is button
				if(event.getCurrentItem() instanceof Button) {
					// get Button
					Button button = (Button) event.getCurrentItem();
					// call ButtonClickEvent
					ButtonClickEvent buttonClickEvent = new ButtonClickEvent(event, button);
					Bukkit.getPluginManager().callEvent(buttonClickEvent);
					// execute click when event is not cancelled
					if(!buttonClickEvent.isCancelled())
						buttonClickEvent.getButton().click(buttonClickEvent);
				}
			}
		}
		
		@EventHandler(priority = EventPriority.MONITOR)
		public void onInventoryClose(InventoryCloseEvent event) {
			// get gui
			GUI gui = activeViewers.remove(event.getPlayer());
			// if player had an gui open
			if(gui != null) {
				// create and call close event
				GUICloseEvent closeEvent = new GUICloseEvent(gui, event);
				Bukkit.getPluginManager().callEvent(closeEvent);
				// destroy gui when bool is true
				if(closeEvent.isDestroyed()) gui.destroy();
			}
		}
		
		@EventHandler(priority = EventPriority.MONITOR)
		public void onItemDrop(PlayerDropItemEvent event) {
			// cancel drop if player view on a gui
			if(activeViewers.containsKey(event.getPlayer())) event.setCancelled(true);
		}
		
		/**
		 * This method updates a certain gui for all viewers
		 * @param guid the unique id of the gui
		 */
		public void updateGUI(UUID guid) {
			// validate
			Validate.notNull(guid, "UniqueId cannot be null");
			// reopen gui for every viewer.
			activeViewers.entrySet().stream()
				.filter(entry -> entry.getValue().getUniqueID().equals(guid))
				.forEach(entry -> entry.getValue().openGUI(entry.getKey()));
		}
		
		/**
		 * This method opens a gui for a certain player
		 * @param player the player which wants
		 * @param gUI the gui which is going to open by the player
		 * @throws IllegalArgumentException
		 */
		public void openGUI(HumanEntity player, GUI gUI) {
			// validate
			Validate.notNull(player, "Player cannot be null");
			Validate.notNull(gUI, "GUi cannot be null");
			// open gui
			player.openInventory(gUI.design);
			// cache new result
			activeViewers.put(player, gUI);
		}
	}
	
	/**
	 * This class represents a Button which has a clear function
	 * @author Timeout
	 *
	 */
	public static class Button extends ItemStack {
	
		protected Consumer<ButtonClickEvent> consumer;
		
		public Button(Consumer<ButtonClickEvent> click) {
			super();
			consumer = click;
		}
		
		/**
		 * This constructor creates an inheritageable instance of this class
		 */
		protected Button() {
			super();
			consumer = null;
		}
		
		/**
		 * This constructor creates a clone of a button
		 * @param button the button you want to clone
		 * @throws IllegalArgumentException if the Button is null
		 */
		public Button(@Nonnull Button button) {
			this(button.clone(), button.consumer);
		}
		
		/**
		 * This constructor creates a new Button. The design is a copy of the itemstack
		 * @param stack the design of this button
		 * @param click what happens if the player clicks this Item
		 */
		public Button(ItemStack stack, Consumer<ButtonClickEvent> click) {
			super(stack);
			this.consumer = click;
		}

		/**
		 * This constructor creates a new Button
		 * @param type the Material of this Button
		 * @param amount the amount of this Button
		 * @param damage the subID of this Button
		 * @param click what happens if the player clicks this button
		 */
		public Button(Material type, int amount, short damage, Consumer<ButtonClickEvent> click) {
			super(type, amount, damage);
			this.consumer = click;
		}

		/**
		 * This constructor creates a new Button with the subID 0
		 * @param type the Material of this Button
		 * @param amount the amount of this Button
		 * @param click what happens if the player clicks this button
		 */
		public Button(Material type, int amount, Consumer<ButtonClickEvent> click) {
			this(type, amount, (short) 0, click);
		}

		/**
		 * This constructor creates a new Button with the subID 0 and the amount 1
		 * @param type the Material of this Button
		 * @param click what happens if the player clicks this button
		 */
		public Button(Material type, Consumer<ButtonClickEvent> click) {
			this(type, 1, click);
		}

		public void click(ButtonClickEvent event) {
			consumer.accept(event);
		}
		
		/**
		 * This Method returns the design of this Button
		 * @deprecated this Method is not necessary anymore. Button inheritages the ItemStack 
		 * @return the design of this Button
		 */
		@Deprecated
		public ItemStack getDesign() {
			return this;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (!(obj instanceof Button))
				return false;
			Button other = (Button) obj;
			return Objects.equals(consumer, other.consumer);
		}
	}
	
	/**
	 * This event will be triggered when a player clicks on a button
	 * @author Timeout
	 *
	 */
	public static class ButtonClickEvent extends Event implements Cancellable {
		
		private static final HandlerList handlers = new HandlerList();

		private InventoryClickEvent previousEvent;
		private Button button;
		private boolean cancel;
		
		public ButtonClickEvent(InventoryClickEvent previousEvent, Button button) {
			this.previousEvent = previousEvent;
			this.button = button;
		}
		
		public static HandlerList getHandlerList() {
			return handlers;
		}
		
		@Override
		public HandlerList getHandlers() {
			return getHandlerList();
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
			return previousEvent;
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
		
		/**
		 * Get the player who clicked on the button.
		 * @return the player who clicked on the button
		 */
		public HumanEntity getWhoClicked() {
			return previousEvent.getWhoClicked();
		}
		
	}
	
	/**
	 * This event will be triggered when a player close a gui interface
	 * @author Timeout
	 *
	 */
	public static class GUICloseEvent extends Event {
		
		private static final HandlerList handlers = new HandlerList();

		private GUI gui;
		private InventoryCloseEvent closeEvent;
		private boolean destroyGUI;
		
		/**
		 * This constructor creates a new event
		 * @param closedGUI the gui which is closed by the player
		 * @param closeEvent the inventorycloseevent itself
		 */
		public GUICloseEvent(@Nonnull GUI closedGUI, @Nonnull InventoryCloseEvent closeEvent) {
			this.gui = closedGUI;
			this.closeEvent = closeEvent;
		}
		
		public static HandlerList getHandlerList() {
			return handlers;
		}
		
		@Override
		public HandlerList getHandlers() {
			return getHandlerList();
		}
		
		/**
		 * This Method get the player who close the gui
		 * @return the player who close the gui
		 */
		public HumanEntity getPlayer() {
			return closeEvent.getPlayer();
		}
		
		/**
		 * This method returns the inventory close event
		 * @return the inventory close event
		 * @deprecated please use the Method {@link GUICloseEvent#getInventoryCloseEvent()} instead
		 */
		@Deprecated
		public InventoryCloseEvent getCloseEvent() {
			return getInventoryCloseEvent();
		}
		
		/**
		 * This method returns the inventory close event
		 * @return the inventory close event
		 */
		public InventoryCloseEvent getInventoryCloseEvent() {
			return closeEvent;
		}
		
		/**
		 * This method checks, if the gui is going to be destroyed
		 * @return if the gui is going to be destroyed
		 */
		public boolean isDestroyed() {
			return destroyGUI;
		}
		
		/**
		 * This method changes the destruction behaviour of the gui. 
		 * This method decides if the gui will be destroyed or not. 
		 * By default, this gui will not be destroyed.
		 * @param arg0 if the gui will be destroyed or not
		 */
		public void setDestroyed(boolean arg0) {
			this.destroyGUI = arg0;
		}
		
		/**
		 * This method returns the closed GUI
		 * @return the closed GUI
		 */
		public GUI getClosedGUI() {
			return gui;
		}
	}
}
