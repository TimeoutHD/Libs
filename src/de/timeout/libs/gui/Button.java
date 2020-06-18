package de.timeout.libs.gui;

import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.timeout.libs.gui.event.ButtonClickEvent;

/**
 * Represents a button with a certain function. 
 * The function will be executed after the button is clicked
 * @author Timeout
 *
 */
public class Button extends ItemStack implements GUIInteractable<ButtonClickEvent> {
	
	protected Consumer<ButtonClickEvent> consumer;
	
	public Button(Consumer<ButtonClickEvent> click) {
		super();
		consumer = click;
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
	 * @param click what happens if the player clicks this button
	 */
	public Button(Material type, int amount, Consumer<ButtonClickEvent> click) {
		super(type, amount);
		this.consumer = click;
	}

	/**
	 * This constructor creates a new Button with the subID 0 and the amount 1
	 * @param type the Material of this Button
	 * @param click what happens if the player clicks this button
	 */
	public Button(Material type, Consumer<ButtonClickEvent> click) {
		this(type, 1, click);
	}

	/**
	 * This Method returns the click function of the button
	 * @return the click function
	 */
	public Consumer<ButtonClickEvent> getClickFunction() {
		return consumer;
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

	@Override
	public void click(ButtonClickEvent event) {
		// executes function
		consumer.accept(event);
	}

}
