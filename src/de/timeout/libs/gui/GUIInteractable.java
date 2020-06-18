package de.timeout.libs.gui;


/**
 * Functional interface for an interactable object inside a gui
 * @author Timeout
 *
 */
@FunctionalInterface
public interface GUIInteractable<C> {

	/**
	 * Method which will be called when a player interacts with this object
	 * @param consumer the consumer of the function
	 */
	public void click(C consumer);
}
