package de.timeout.libs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

public final class Reflections {
		
	private static final Field modifiers = getField(Field.class, "modifiers");
	
	private static final Class<?> packetClass = getNMSClass("Packet");
	
	private Reflections() {}
	
	public static Field getField(Class<?> clazz, String... names) {
		// if names is not empty
		if(names.length != 0) {
			try {
				// get Field and set executable
				Field field = clazz.getDeclaredField(names[0]);
				field.setAccessible(true);
				
				// change modifier fields
				if(Modifier.isFinal(field.getModifiers())) modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
				// return field
				return field;
			} catch (NoSuchFieldException e) {
				// Field not found recursive execute without first element
				return getField(clazz, ArrayUtils.subarray(names, 1, names.length));
			} catch(IllegalArgumentException | SecurityException | IllegalAccessException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Cannot get checked fields " + Arrays.toString(names) + " in Class " + clazz.getName(), e);
			}
			return null;
		} else return null;
	}

	/**
	 * This method creates a Field which is linked to the fieldname in your class. The field is modifiable.
	 * @param clazz the class, which contains the field
	 * @param name the fieldname
	 * @return the field itself
	 */
	public static Field getField(Class<?> clazz, String name) {
			try {
				Field field = clazz.getDeclaredField(name);
			    field.setAccessible(true);
			      
			    if (Modifier.isFinal(field.getModifiers()))modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
			    return field;
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Cannot get Field " + name + " in Class " + clazz.getName(), e);
			}
		return null;
	}
	
	/**
	 * This method creates a Field from an object. The field is modifiable
	 * @param obj the object
	 * @param name the name of the field
	 * @return the field itself
	 */
	public static Field getField(Object obj, String name) {
		return getField(obj.getClass(), name);
	}
	
	/**
	 * This method returns the value of the Field in your obj
	 * @param field the field which you want to read
	 * @param obj the object you want to read
	 * @return the value, which you are looking for. null if there were an error
	 */
	public static Object getValue(Field field, Object obj) {
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not get Value from Field " + field.getName() + " in " + obj, e);
		}
		return null;
	}
	
	/**
	 * This method gets a SubClass in a class with a certain name
	 * @param overclass the class which contains the class you are searching for
	 * @param classname the name of the class you are searching for
	 * @return the class you are searching for. Null if the class does not exist
	 */
	public static Class<?> getSubClass(Class<?> overclass, String classname) {
		Class<?>[] underclasses = overclass.getClasses();
		for(Class<?> underclass : underclasses) {
			if(underclass.getName().equalsIgnoreCase(overclass.getName() + "$" + classname))return underclass;
		}
		return null;
	}
	
	/**
	 * This method return an NMS-Class, which has a certain name
	 * @param nmsClass the name of the NMS-Class
	 * @return the CLass itself. Null if the class cannot be found.
	 */
	public static Class<?> getNMSClass(String nmsClass) {
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
			String name = "net.minecraft.server." + version + nmsClass;
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not find NMS-Class " + nmsClass, e);
		}
		return null;
	}
	
	/**
	 * This method returns a class-object from its name
	 * @param classpath the name of the class
	 * @return the class itself
	 */
	public static Class<?> getClass(String classpath) {
		try {
			return Class.forName(classpath);
		} catch (ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Class " + classpath + " not found", e);
		}
		return null;
	}
	
	/**
	 * This method returns a CraftBukkit-Class 
	 * @param clazz the Craftbukkit-Class
	 * @return the CraftBukkit-Class. Null if the class cannot be found
	 */
	public static Class<?> getCraftBukkitClass(String clazz) {
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
			String name = "org.bukkit.craftbukkit." + version + clazz;
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.WARNING, "Could not find CraftBukkit-Class " + clazz, e);
		}
		return null;
	}
	
	/**
	 * This method returns an EntityPlayer-Object of a player
	 * @param player the player
	 * @return the EntityPlayer as Object
	 * @throws ReflectiveOperationException if there was an error
	 */
	public static Object getEntityPlayer(Player player) throws ReflectiveOperationException {
		Method getHandle = player.getClass().getMethod("getHandle");
		return getHandle.invoke(player);
	}
	
	/**
	 * This method returns the PlayerConnection as an Object
	 * @param player the owner of the player connection
	 * @return the PlayerConnection as Object
	 * @throws ReflectiveOperationException if there was an error
	 */
	public static Object getPlayerConnection(Player player) throws ReflectiveOperationException {
		Object nmsp = getEntityPlayer(player);
		Field con = nmsp.getClass().getField("playerConnection");
		return con.get(nmsp);
	}
	
	/**
	 * This method sends a Packet to a Player
	 * @param player the Player
	 * @param packet the packet
	 * @throws ReflectiveOperationException if the object is not a packet
	 */
	public static void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
		Object playerConnection = getPlayerConnection(player);
		playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
	}
	
	/**
	 * This Method set a value into a Field in an Object
	 * @param field the Field
	 * @param obj the Object you want to modifiy
	 * @param value the new value of the field
	 */
	public static void setField(Field field, Object obj, Object value) {
		try {
			field.setAccessible(true);
			field.set(obj, value);
			field.setAccessible(false);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not set Value " + value.getClass().getName() + " in Field " + field.getName() + " in Class " + obj.getClass().getName(), e);
		}
	}
	
	/**
	 * This Method returns the player's GameProfile
	 * @param player the owner of the GameProfile
	 * @return the Gameprofile
	 */
	public static GameProfile getGameProfile(Player player) {
		 try {
			Class<?> craftplayerClass = getCraftBukkitClass("entity.CraftPlayer");
			return craftplayerClass != null ? (GameProfile) craftplayerClass.getMethod("getProfile").invoke(player) : null;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			Bukkit.getLogger().log(Level.INFO, "Could not get GameProfile from Player " + player.getName(), e);
		}
		 return new GameProfile(player.getUniqueId(), player.getName());
	}
	
	/**
	 * This Method returns a Field in a class with a specific fieldtype
	 * @param target the class
	 * @param name the name of the field
	 * @param fieldtype the datatype of the Field
	 * @return the Field itself. Null if the field cannot be found
	 */
	public static <T> Field getField(Class<?> target, String name, Class<T> fieldtype) {
		for(Field field : target.getDeclaredFields()) {
			if((name == null || field.getName().equals(name)) && fieldtype.isAssignableFrom(field.getType())) {
				return getField(target, name);
			}
		}
		return null;
	}
	
	/**
	 * This method modifies a field with a certain name for a certain object
	 * @param object the object you want to modify
	 * @param fieldName the name of the Field
	 * @param value the Value you want to insert at this Field
	 */
	public static void setValue(Object object, String fieldName, Object value) {
		Field field = getField(object, fieldName);
		Reflections.setField(field, object, value);
	}
}
