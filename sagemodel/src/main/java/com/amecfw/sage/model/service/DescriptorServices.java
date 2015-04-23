package com.amecfw.sage.model.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import android.util.Log;

import com.amecfw.sage.model.FieldDescriptor;

public class DescriptorServices {
	
	public static final String TYPE_STRING = "String";
	public static final String TYPE_DATE = "Date";
	public static final String TYPE_LONG = "Long";
	public static final String TYPE_UUID = "UUID";
	public static final String TYPE_INT = "Int";

	/**
	 * Sets the fields in the provided model using the FieldDescriptor tags in the provided source.
	 * @param annotatedSource The object containing the field descriptor tags
	 * @param model The model object to populate
	 * @return the number of fields set or -1 if there was a processing error
	 */
	public static <Tsource, Tmodel> int setByFieldDescriptor(Tsource annotatedSource, Tmodel model){
		int result = 0;
		Field[] fields = annotatedSource.getClass().getDeclaredFields();
		for (Field field : fields) {
			FieldDescriptor descriptor = field.getAnnotation(FieldDescriptor.class);
			if (descriptor != null && model.getClass().equals(descriptor.clazz())){
				field.setAccessible(true);
				try {
					Method destination = getMethod(model.getClass() ,descriptor.targetSetter(), descriptor.type());				
					destination.invoke(model, getValueOrDefault(annotatedSource, field, descriptor));
					result++;
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ParseException | NoSuchMethodException e) {
					Log.e("DescriptorServices", String.format("%s.setByFieldDescriptor : %s", e.getClass().getSimpleName(), e.getMessage()));
					return -1;
				}
			}
		}	
		return result;
	}
	
	private static Method getMethod(Class<?> source, String name, String type) throws NoSuchMethodException{
		if(type.equals(TYPE_STRING)) return source.getMethod(name, String.class);
		if(type.equals(TYPE_DATE)) return source.getMethod(name, Date.class);
		if(type.equals(TYPE_LONG)) return source.getMethod(name, Long.class);
		if(type.equals(TYPE_INT)) return source.getMethod(name, int.class);
		if(type.equals(TYPE_UUID)) return source.getMethod(name, UUID.class);
		throw new NoSuchMethodException();
	}
	
	private static <Tsource> Object getValueOrDefault(Tsource source, Field field, FieldDescriptor descriptor) 
			throws IllegalAccessException, IllegalArgumentException, ParseException{
		Object result = field.get(source);
		if(result != null) return result;
		String defaultValue = descriptor.defaultValue();
		if(defaultValue.equals("")) return null;
		return parseDefault(defaultValue, descriptor.type());
	}
	
	private static Object parseDefault(String name, String type) throws ParseException{
		if(type.equals(TYPE_STRING)) return name;
		if(type.equals(TYPE_DATE)) return DateFormat.getDateTimeInstance().parse(name);
		if(type.equals(TYPE_LONG)) return Long.parseLong(name);
		if(type.equals(TYPE_INT)) return Integer.parseInt(name);
		if(type.equals(TYPE_UUID)) return UUID.fromString(name);
		return null;
	}
	
	/**
	 * Gets the values in the provided model and updates the fields in the annotatedSource using the FieldDescriptor tags in the provided source.
	 * @param annotatedSource the object containing the field descriptor tags to populate
	 * @param model the source data
	 * @return the number of updated fields or -1 if there was a processing error
	 */
	public static <Tsource, Tmodel> int getByFieldDescriptor(Tsource annotatedSource, Tmodel model){
		int result = 0;
		Field[] fields = annotatedSource.getClass().getDeclaredFields();
		for (Field field : fields) {
			FieldDescriptor descriptor = field.getAnnotation(FieldDescriptor.class);
			if (descriptor != null && model.getClass().equals(descriptor.clazz())){
				field.setAccessible(true);
				try {
					Method getter = model.getClass().getMethod(descriptor.targetGetter(), new Class<?>[0]);				
					field.set(annotatedSource, getter.invoke(model, new Object[]{}));
					result++;
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException e) {
					Log.e("DescriptorServices", String.format("%s.getByFieldDescriptor : %s : %s", e.getClass().getSimpleName(), model.getClass().getSimpleName(), e.getMessage()));
					return -1;
				}
			}
		}
		return result;
	}
	
}
