package de.timeout.libs.gui.event;

import javax.annotation.NotNull;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

import de.timeout.libs.gui.GUI;

/**
 * Event which will be triggered if a player interacts 
 * @author Timeout
 *
 */
public class GUIInteractEvent extends InventoryClickEvent {

	private static final HandlerList handlers = new HandlerList();
	
	private GUI gui;
	
	public GUIInteractEvent(@NotNull InventoryClickEvent event, @NotNull GUI gui) {
		super(event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction());
		// Validate
		Validate.notNull(gui, "GUI cannot be null");
		this.gui = gui;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return getHandlerList();
	}
	
	/**
	 * Returns the clicked gui
	 * @return the clicked gui
	 */
	@NotNull
	public GUI getGUI() {
		return gui;
	}

}
