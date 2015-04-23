package com.amecfw.sage.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetaDataDescriptor {
	public Class<?> clazz();
	public String metaDataName();
	public String defaultValue() default "";
}
