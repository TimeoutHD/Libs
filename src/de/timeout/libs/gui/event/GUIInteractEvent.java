package de.timeout.libs.gui.event;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.event.inventory.InventoryClickEvent;

import de.timeout.libs.gui.GUI;

/**
 * Event which will be triggered if a player interacts 
 * @author Timeout
 *
 */
public class GUIInteractEvent extends InventoryClickEvent {

	private GUI gui;
	
	public GUIInteractEvent(@Nonnull InventoryClickEvent event, @Nonnull GUI gui) {
		super(event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction());
		// Validate
		Validate.notNull(gui, "GUI cannot be null");
		this.gui = gui;
	}
	
	/**
	 * Returns the clicked gui
	 * @return the clicked gui
	 */
	@Nonnull
	public GUI getGUI() {
		return gui;
	}

}
