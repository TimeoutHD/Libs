package de.timeout.libs.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import de.timeout.libs.config.UTFConfig;
import de.timeout.libs.gui.event.ButtonClickEvent;
import de.timeout.libs.gui.event.GUICloseEvent;
import de.timeout.libs.gui.event.GUIOpenEvent;

class GUIHandler implements Listener {

	private final Map<HumanEntity, GUI> openGUIs = new HashMap<>();
	
	public GUIHandler() {
		try {
			registerListener();
		} catch (ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.SEVERE, "FATAL ERROR IN LIBS. PLEASE REPORT THIS BUG INSTANTLY!", e);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.WARNING, "Unable to read plugins plugin.yml. Please check if your plugin.yml is correct and your plugin is enabled.");
		}
	}
	
	/**
	 * Registers this listener 
	 * 
	 * @throws IOException if the plugin.yml is unreadable
	 * @throws ClassNotFoundException that should never happen. An enabled plugin must have its main method to run properly
	 */
	private void registerListener() throws IOException, ClassNotFoundException {
		// get Main-Class of plugin
		String main = new UTFConfig(this.getClass().getResourceAsStream("plugin.yml")).getString("main");
		
		JavaPlugin plugin = JavaPlugin.getProvidingPlugin(Class.forName(main));
		
		// register this listener
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		// get player and gui if exists
		HumanEntity player = event.getPlayer();
		GUI gui = openGUIs.remove(player);
		
		// check if player has an gui open
		if(gui != null) {
			// trigger close event
			GUICloseEvent guiEvent = new GUICloseEvent(event, gui);
			Bukkit.getPluginManager().callEvent(guiEvent);
			
			// call close function
			gui.closeAction.accept(guiEvent);
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		// get player and gui
		HumanEntity player = event.getWhoClicked();
		GUI gui = openGUIs.get(player);
		
		// check if player sees gui
		if(gui != null) {
			// check if user clicked on gui
			if(event.getView().getTopInventory().equals(event.getClickedInventory())) {
				// cancel event (Deny result is safer than cancel)
				event.setResult(Result.DENY);
				
				// get item
				ItemStack button = gui.getItem(event.getSlot());
				// check if its a button
				if(button instanceof Button) {
					// call event
					ButtonClickEvent guiEvent = new ButtonClickEvent(event, gui, (Button) button);
					Bukkit.getPluginManager().callEvent(guiEvent);
					
					// check if guiEvent is cancelled
					if(!guiEvent.isCancelled()) {
						// call button accept
						((Button) button).click(guiEvent);
					}
				}
			}
		}
	}
	
	/**
	 * Registers the player in listener.
	 * Is called after {@link GUI#openGUI(HumanEntity)} was called
	 * @param viewer the player
	 * @param gui the gui
	 * 
	 * @return the triggered GUIOpenEvent
	 */
	public GUIOpenEvent onGUIOpen(HumanEntity viewer, GUI gui, String name) {
		// call GUI open event
		GUIOpenEvent event = new GUIOpenEvent(viewer, gui, name);
		Bukkit.getPluginManager().callEvent(event);
		
		// add to list if player opens the gui successfully
		if(!event.isCancelled()) this.openGUIs.put(viewer, gui);
		
		return event;
	}
	
	/**
	 * Aborts the gui open process.
	 * This method is called if InventoryOpenEvent is cancelled by another plugin
	 * 
	 * @param viewer the viewer of the gui
	 */
	public void onGUIOpenAbort(HumanEntity viewer) {
		this.openGUIs.remove(viewer);
	}
}
