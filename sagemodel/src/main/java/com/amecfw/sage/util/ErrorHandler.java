package com.amecfw.sage.util;

/**
 * an interface to allow for error notifications.
 * For example, allowing a fragment to communicate an error to the hosting activity.
 */
public interface ErrorHandler {
	/**
	 * the method to handle the an error
	 * @param an error message or null
	 * @param the exception causing the error or null
	 * @param the type of source where the error occurred or null
	 */
	void onError(String message, Exception exception, Class<?> source);
}
