package de.timeout.libs.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.timeout.libs.config.UTFConfig;
import de.timeout.libs.gui.event.GUIOpenEvent;

class GUIHandler implements Listener {

	private final Map<HumanEntity, GUI2> openGUIs = new HashMap<>();
	
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

	}
	
	/**
	 * Registers the player in listener.
	 * Is called after {@link GUI#openGUI(HumanEntity)} was called
	 * @param viewer the player
	 * @param gui the gui
	 * 
	 * @return the triggered GUIOpenEvent
	 */
	public GUIOpenEvent onGUIOpen(HumanEntity viewer, GUI2 gui, String name) {
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
