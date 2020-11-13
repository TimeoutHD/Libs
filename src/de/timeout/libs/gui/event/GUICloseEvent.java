package de.timeout.libs.gui.event;

import javax.annotation.NotNull;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;

import de.timeout.libs.gui.GUI;

public class GUICloseEvent extends InventoryCloseEvent {

	private static final HandlerList handlers = new HandlerList();
	
	private GUI gui;
	
	public GUICloseEvent(@NotNull InventoryCloseEvent parent, @NotNull GUI gui) {
		super(parent.getView());
		
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
	
	@NotNull
	public GUI getGUI() {
		return gui;
	}
}
