package de.timeout.libs;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public final class BukkitReflections {
	
	private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		
	/**
	 * This method return an NMS-Class, which has a certain name
	 * @param nmsClass the name of the NMS-Class
	 * @return the CLass itself. Null if the class cannot be found.
	 */
	public static Class<?> getNMSClass(String nmsClass) {
		return loadClass("net.minecraft.server.%s.%s", nmsClass);
	}
	
	/**
	 * This method returns the Array type of an NMS-Class.
	 * For example it will return PacketPlayOutNamedEntitySpawn[].class instead of PacketPlayOutNamedEntitySpawn.class
	 * 
	 * For single types, please use {@link BukkitReflections#getNMSClass(String)}
	 * 
	 * @param nmsClass the name of the NMS-Class
	 * @return the Array Type of the NMS-Class. Null if the class cannot be found
	 */
	public static Class<?> getNMSArrayTypeClass(String nmsClass) {
		return loadClass("[Lnet.minecraft.server.%s.%s;", nmsClass);
	}

	/**
	 * This method returns a CraftBukkit-Class 
	 * @param clazz the Craftbukkit-Class
	 * @return the CraftBukkit-Class. Null if the class cannot be found
	 */
	public static Class<?> getCraftBukkitClass(String clazz) {
		return loadClass("org.bukkit.craftbukkit.%s.%s", clazz);
	}
	
	/**
	 * This method returns the array type of a certain CraftBukkit-Class
	 * For example: it will returns CraftPlayer[].class instead of CraftPlayer.class
	 * 
	 * For single types please use {@link BukkitReflections#getCraftBukkitClass(String)}
	 * 
	 * @param clazz the path of the class after the version package. Split them with an '.'
	 * @return the array type of the CraftBukkit-Class. Null if the class cannot be found
	 */
	public static Class<?> getCraftBukkitArrayTypeClass(String clazz) {
		return loadClass("[Lorg.bukkit.craftbukkit.%s.%s;", clazz);
	}
	
	private static Class<?> loadClass(String packagename, String clazz) {
		String name = String.format(packagename, VERSION, clazz);
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			Logger.getGlobal().log(Level.WARNING, e, () ->"Could not find Class " + name);
		}
		return null;
	}
}
