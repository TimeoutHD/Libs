package de.timeout.libs.gui.event;

import javax.annotation.NotNull;

import org.apache.commons.lang.Validate;
import org.bukkit.event.inventory.InventoryClickEvent;

import de.timeout.libs.gui.Button;
import de.timeout.libs.gui.GUI;

/**
 * An event which will be triggered if a user clicks on a button
 * @author Timeout
 *
 */
public class ButtonClickEvent extends GUIInteractEvent {
	
	private Button button;

	/**
	 * Creates a new ButtonClickEvent
	 * @param event the InventoryClickEvent itself
	 * @param gui the clicked gui
	 * @param button the button which is clicked
	 */
	public ButtonClickEvent(@NotNull InventoryClickEvent event, @NotNull GUI gui, @NotNull Button button) {
		super(event, gui);
		
		Validate.notNull(button, "Button cannot be null");
		this.button = button;
	}
	
	/**
	 * returns the button which was clicked by the player
	 */
	@NotNull
	public Button getButton() {
		return button;
	}

}
