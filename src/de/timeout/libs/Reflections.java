package de.timeout.libs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Reflections {
			
	protected Reflections() {}
	
	/**
	 * Searches through a bundle of field until it founds the correct field
	 * @param clazz the class you want to search
	 * @param names a bundle of names. NMS-Fieldnames may changes in different versions
	 * @return the searched field or null if the field cannot be found.
	 */
	@Nullable
	public static Field getField(Class<?> clazz, String... names) {
		// if names is not empty
		if(names.length != 0) {
			try {
				// get Field and set executable
				Field field = clazz.getField(names[0]);
				field.setAccessible(true);
				
				// return field
				return field;
			} catch (NoSuchFieldException e) {
				// Field not found recursive execute without first element
				return getField(clazz, (String[]) ArrayUtils.subarray(names, 1, names.length));
			}
		} else return null;
	}
	
	/**
	 * Searches for a field which has different names in different versions. 
	 * @param clazz the class which stores this field. Cannot be null
	 * @param fieldType the type of data of the field. Cannot be null
	 * @param names the different names of the field
	 * @return the field or null if the field could not be found
	 */
	@Nullable
	public static Field getField(@NotNull Class<?> clazz, @NotNull Class<?> fieldType, String... names) {
		// Validate
		Validate.notNull(clazz, "Class cannot be null");
		Validate.notNull(fieldType, "FieldType cannot be null");
		
		// return null if the names length is null
		if(names.length > 0) {
			// try to get first Field
			Field field = getField(clazz, fieldType, names[0]);
			
			// recursive search if field could not be found. Otherwise return field
			return field == null ? getField(clazz, fieldType, (String[]) ArrayUtils.subarray(names, 1, names.length)) : field;
		} else return null;
	}
	
	@Nullable
	private static Field getField(Class<?> clazz, Class<?> fieldType, String name) {
		Field field = null;
		
		// try to get Field by name
		try {
			field = clazz.getField(name);
			field.setAccessible(true);
		} catch (NoSuchFieldException e) {
			Logger.getGlobal().log(Level.WARNING, String.format("Unable to find field %s in class %s", name, clazz.getCanonicalName()));
		}
		
		return field != null && field.getType().equals(fieldType) ? field : null;
	}

	/**
	 * This method creates a Field which is linked to the fieldname in your class. The field is modifiable.
	 * @param clazz the class, which contains the field
	 * @param name the fieldname
	 * @return the field itself
	 */
	@Nullable
	public static Field getField(Class<?> clazz, String name) {
			try {
				Field field = clazz.getField(name);
			    field.setAccessible(true);
			      
			    return field;
			} catch (NoSuchFieldException | SecurityException e) {
				Logger.getGlobal().log(Level.SEVERE, String.format("Cannot get Field %s in Class %s", name, clazz.getSimpleName()), e);
			}
		return null;
	}
	
	/**
	 * This method creates a Field from an object. The field is modifiable
	 * @param obj the object
	 * @param name the name of the field
	 * @return the field itself
	 */
	@Nullable
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
		} catch (IllegalAccessException e) {
			Logger.getGlobal().log(Level.SEVERE, String.format("Could not get value from field %s in %s", field.getName(), obj.getClass().getSimpleName()), e);
		}
		return null;
	}
	
	/**
	 * This method gets a SubClass in a class with a certain name
	 * @param overclass the class which contains the class you are searching for
	 * @param classname the name of the class you are searching for
	 * @return the class you are searching for. Null if the class does not exist
	 */
	@Nullable
	public static Class<?> getSubClass(@NotNull Class<?> overclass, @NotNull String classname) {
		Validate.notNull(overclass, "OverClass cannot be null");
		Validate.notEmpty(classname, "Name of SubClass can neither be null nor empty!");
		
		return Arrays.stream(overclass.getClasses())
			.filter(underclass -> underclass.getName().equalsIgnoreCase(overclass.getName() + "$" + classname))
			.findAny()
			.orElse(null);
	}
	
	/**
	 * This method returns a class-object from its name
	 * @param classpath the name of the class
	 * @return the class itself
	 */
	@Nullable
	public static Class<?> getClass(String classpath) {
		try {
			return Class.forName(classpath);
		} catch (ClassNotFoundException e) {
			Logger.getGlobal().log(Level.SEVERE, e, () -> "Class " + classpath + " not found");
		}
		return null;
	}
	
	/**
	 * This Method set a value into a Field in an Object
	 * @param field the Field
	 * @param obj the Object you want to modifiy
	 * @param value the new value of the field
	 */
	public static void setValue(@NotNull Field field, @NotNull Object obj, @Nullable Object value) {
		// Validate
		Validate.notNull(field, "Field cannot be null");
		Validate.notNull(obj, "Target cannot be null");
		
		try {
			field.setAccessible(true);
			field.set(obj, value);
			field.setAccessible(false);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			Logger.getGlobal().log(Level.SEVERE, e, () -> "Could not set Value in Field " + field.getName() +
					" in Class " + obj.getClass().getName());
		}
	}
	
	/**
	 * This Method set a value into a Field in an Object
	 * @param field the field 
	 * @param target the targeted object
	 * @param value the new value of the field
	 * @deprecated Use {@link Reflections#setValue(Field, Object, Object)} instead.
	 */
	@Deprecated
	public static void setField(@NotNull Field field, @NotNull Object target, @Nullable Object value) {
		setValue(field, target, value);
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
	public static void setValue(@NotNull Object object, @NotNull String fieldName, @Nullable Object value) {
		setValue(Objects.requireNonNull(getField(object, fieldName)), object, value);
	}
	
	/**
	 * Returns the method of a certain object due reflections
	 * @param clazz the class which has the method. Cannot be null
	 * @param name the name of the method. Can neither be null nor empty
	 * @param params the parameters of the method
	 * @return the method or null if the method could not be found
	 */
	@Nullable
	public static Method getMethod(@NotNull Class<?> clazz, @NotNull String name, Class<?>... params) {
		// Validate
		Validate.notNull(clazz, "Class cannot be null");
		Validate.notEmpty(name, "Method-Name can neither be null nor empty!");
		
		try {
			return clazz.getMethod(name, params);
		} catch (NoSuchMethodException e) {
			Logger.getGlobal().log(Level.SEVERE, String.format("Unable to find Method with name %s(%s) in %s!", name, 
					Arrays.toString(Arrays.stream(params).map(Class::getName).toArray()), clazz), e);
		} catch (SecurityException e) {
			Logger.getGlobal().log(Level.SEVERE, String.format("Internal SecurityException while searching method %s(%s) in class %s", name, 
					Arrays.toString(Arrays.stream(params).map(Class::getName).toArray()), clazz), e);
		}
		
		return null;
	}
}
