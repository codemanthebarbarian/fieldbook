package com.amecfw.sage.util;

/**
 * Identifies an application that support caching. The cache can be used to pass objects
 * between fragments, services, or Activities
 */
public interface ApplicationCache {
	/**
	 * Save an object to the cache for the specified key. Any existing item with the
	 * key will be replace and returned, if there was no item null will be returned.
	 * @param key
	 * @param item
	 * @return an old item of for the key or null
	 */
	public Object setItem(String key, Object item);
	
	/**
	 * Get an object from the cache for the specified key or null if it doesn't exist.
	 * @param key
	 * @return the cached item or null
	 */
	public Object getObject(String key);
	
	/**
	 * Get an object from the cache for the specified key, if it doesn't exist or is not the provided type null is returned
	 * @param key
	 * @return the cached item or null
	 */
	public <T> T getItem(String key);
	
	/**
	 * Removed the item from the cache, the item or null is returned.
	 * @param key
	 * @return a cached item or null
	 */
	public Object remove(String key);
	
	/**
	 * Removed the item from the cache, the item or null is returned.
	 * If the item is not of the specified type, null is returned but it is removed anyway.
	 * @param key
	 * @return a cached item or null
	 */
	public <T> T removeItem(String key);
}
