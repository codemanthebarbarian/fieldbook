package com.amecfw.sage.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldDescriptor {
	public String targetGetter();
	public String targetSetter();
	public Class<?> clazz();
	public String defaultValue() default "";
	public String type() default "String";
}
