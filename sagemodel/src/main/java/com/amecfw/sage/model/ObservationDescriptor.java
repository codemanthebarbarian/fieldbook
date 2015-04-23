package com.amecfw.sage.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ObservationDescriptor {
	public String fieldName();
	public String observationType();
	public String defaultValue() default "";
}
