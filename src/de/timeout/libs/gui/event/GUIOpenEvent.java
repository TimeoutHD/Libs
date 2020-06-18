package de.timeout.libs.gui.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.timeout.libs.gui.GUI2;

/**
 * Event which will be triggered if a player opens a gui
 * @author Timeout
 *
 */
public class GUIOpenEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	
	private GUI2 gui;
	private HumanEntity player;
	private String name;
	
	private boolean cancel;

	public GUIOpenEvent(@Nonnull HumanEntity player, @Nonnull GUI2 gui, @Nullable String name) {
		Validate.notNull(gui, "GUI cannot be null");
		Validate.notNull(player, "Player cannot be null");
		
		this.gui = gui;
		this.player = player;
		this.name = name;
	}
	
	/**
	 * Returns the handlerlist of the event
	 * @return the handlerlist of the event
	 */
	@Nonnull
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {	
		return getHandlerList();
	}

	/**
	 * Returns the opened gui
	 * @return the opened gui
	 */
	@Nonnull
	public GUI2 getGUI() {
		return gui;
	}
	
	/**
	 * Returns the player of this event
	 * @return the player. Cannot be null
	 */
	@Nonnull
	public HumanEntity getPlayer() {
		return player;
	}
	
	@Nonnull
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.cancel = arg0;
	}
	
	
}
