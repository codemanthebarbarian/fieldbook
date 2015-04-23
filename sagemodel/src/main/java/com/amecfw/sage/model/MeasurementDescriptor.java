package com.amecfw.sage.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MeasurementDescriptor {
	public String parameterName();
	public String unit();
	public String matrix();	
}
