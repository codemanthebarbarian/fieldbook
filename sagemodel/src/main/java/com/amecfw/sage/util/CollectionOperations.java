package com.amecfw.sage.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import com.amecfw.sage.model.EntityBase;
import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.EqualityComparatorOf;
import com.amecfw.sage.model.Equatable;
import com.amecfw.sage.model.UUIDSupport;

public class CollectionOperations {

	/**
	 * 
	 * @param to
	 * @param from
	 * @param comparator
	 * @return the number of items merged from -> to
	 */
	public static <T extends Equatable> int Merge(List<T> to, List<T> from, EqualityComparator comparator){
		if(to == null && from == null) return 0;
		if(from == null) return 0;
		if(to == null){
			to = new ArrayList<T>(from);
			return from.size();
		}
		int count = 0;
		for(T obj: from){
			obj.setComparator(comparator);
			int index = to.indexOf(obj);
			if(index < 0){
				to.add(obj);
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Returns the items from the first lists that do not exist in the second list using the provided comparator.
	 * if first is null or empty, an empty list is returned. If second is null or empty a list containing all of the items in
	 * first is returned.
	 * @param first
	 * @param second
	 * @param comparator
	 * @return the items in first that are not in second.
	 */
	public static <T extends Equatable> List<T> except(List<T> first, List<T> second, EqualityComparator comparator){
		if(first == null || first.size() == 0) return new ArrayList<T>(0);
		if(second == null || second.size() == 0) return new ArrayList<T>(first);
		List<T> results = new ArrayList<T>();
		for(T item : first){
			item.setComparator(comparator);
			int index = second.indexOf(item);
			if(index < 0) results.add(item);
		}
		return results;
	}
	
	public static <T extends Equatable> List<T> intersection(Collection<T> list, T object, EqualityComparator comparator){
		List<T> results = new ArrayList<T>();
		object.setComparator(comparator);
		for(T item : list){
			if(object.equals(item)) results.add(item);
		}
		return results;
	}
	
	/**
	 * removes all the items from the first list that match an item in the second list using the provided comparator
	 * @param first
	 * @param second
	 * @param comparator
	 * @return true if the first list was mutated
	 */
	public static <T extends Equatable> boolean removeAll(List<T> first, List<T> second, EqualityComparator comparator){
		if(first == null || second == null || first.size() == 0 || second.size() == 0) return false;
		boolean result = false;
		for(int i = (first.size() - 1) ; i >= 0 ; i--){
			if(indexOf(second, first.get(i), comparator) >= 0 ){
				first.remove(i);
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * Finds the matching items in a collection using the provided comparator. The comparator must be implemented so that 
	 * the collection item can be compared to the provided object
	 * @param list
	 * @param object
	 * @param comparator
	 * @return
	 */
	public static <T extends Equatable> List<T> find(Collection<T> list, Object object, EqualityComparator comparator){
		List<T> results = new ArrayList<T>();
		for(T item : list){
			item.setComparator(comparator);
			if(item.equals(object)) results.add(item);
		}
		return results;
	}
	
	/**
	 * Finds the matching items in a collection using the provided comparator.
	 * @param list
	 * @param object
	 * @param comparator
	 * @return
	 */
	public static <T extends Equatable> List<T> match(Collection<T> list, T object, EqualityComparator comparator){
		List<T> results = new ArrayList<T>();
		object.setComparator(comparator);
		for(T item : list){
			if(object.equals(item)) results.add(item);
		}
		return results;
	}
	
	/**
	 * Finds the first matching item in the provided list. Returns null if no match is found
	 * @param list
	 * @param object
	 * @param comparator
	 * @return
	 */
	public static <T> T first(List<T> list, T object, EqualityComparatorOf<T> comparator){
		for(T item: list) if(comparator.equals(object, item)) return item;
		return null;
	}
	
	/**
	 * find the index of the first item that matches the object using the provided comparator.
	 * if the list is null or no matches are found, returns -1. The items in the list must be Equatable and the 
	 * comparator is set to the list object. The comparator must be implemented correctly so that a match can be 
	 * fount when comparing to the provided object.
	 * @param list
	 * @param object
	 * @param comparator
	 * @return the index of the item or -1 if not found
	 */
	public static <T extends Equatable> int indexOf(List<T> list, Object object, EqualityComparator comparator){
		if(list == null) return -1;
		for(int i = 0 ; i < list.size() ; i++){
			T li = list.get(i);
		    li.setComparator(comparator);
			if(li.equals(object)) return i;
		}
		return -1;
	}
	
	public static <T> int indexOfT(List<T> list, T object, EqualityComparatorOf<T> comparator){
		if(list == null) return -1;
		for(int i = 0 ; i < list.size() ; i++) 
			if(comparator.equals(list.get(i), object)) return i;
		return -1;
	}
	
	/**
	 * Gets a sublist of items based on a compiled regular expression. uses reflection to based on a method
	 * name. Return the items that match or an empty list. 
	 * the list will be empty is anything is null, or there is an issue getting the the string values using
	 * reflection.
	 * @param list
	 * @param methodGetterName (must return type string)
	 * @param pattern a compiled pattern
	 * @return
	 */
	public static <T> List<T> intersection(Collection<T> list, String methodGetterName, Pattern pattern){
		List<T> results = new ArrayList<T>();
		if(list == null || methodGetterName == null || pattern == null) return results;
		for(T item : list){
			try {
				Method method = item.getClass().getMethod(methodGetterName, (Class[]) null);		
				if(pattern.matcher((String)method.invoke(item, (Object[]) null)).matches()) results.add(item);
			} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				return results;
			}
		}
		return results;
	}
	
	public static <T extends Equatable> void addOrReplace(List<T> list, T item, EqualityComparator comparator){
		if(list == null || item == null || comparator == null) return;
		item.setComparator(comparator);
		int i = list.indexOf(item);
		if(i >= 0) list.set(i, item);
		else list.add(item);
	}
	
	public static <T extends Equatable> T find(List<T> list, T item, EqualityComparator comparator){
		if(list == null || item == null || comparator == null) return null;
		item.setComparator(comparator);
		int i = list.indexOf(item);
		if(i >= 0) return list.get(i);
		else return null;
	}
	
	/**
	 * Searches the provided string array for a string.
	 * If not found -1 is returned, it the array is null -1 is returned.
	 * If the item is null, returns the first index of the array that is null or -1 if none are null.
	 * if ignore case is true, case is ignored.
	 * @param array
	 * @param item
	 * @param ignoreCase
	 * @return the index or -1
	 */
	public static int indexOfStringArray(String[] array, String item, Boolean ignoreCase){
		if(array == null) return -1;
		if(item == null){ 
			for (int i = 0 ; i < array.length ; i++){ 
				if(array[i] == null) return i;
			}
		} else if (ignoreCase){
			for (int i = 0 ; i < array.length ; i++){ 
				if(array[i] != null && item.equalsIgnoreCase(array[i])) return i;
			}
		} else {
			for (int i = 0 ; i < array.length ; i++){
				if(array[i] != null && item.equalsIgnoreCase(array[i])) return i;
			}
		}
		return -1;
	}
	
	/**
	 * creates a list of one item (initial capacity is set to 1) with the item provided. If the item is null
	 * an empty list is created.
	 * @param item
	 * @return
	 */
	public static <T> List<T> createList(T item){
		List<T> result = new ArrayList<T>(1);
		if(item != null) result.add(item);
		return result;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Comparators
	
	/**
	 * A generic comparator for UUIDSupport objects. The objects must be of the same
	 * class (objA.getClass().equals(objB.getClass())) before the UUIDs are compared.
	 * If they are not of the same class type, will return false. Instance equality
	 * is checked before any class checking, so if the objects are not UUID types or
	 * of the same class type, but are the same instance objA == objB it will return
	 * true.
	 */
	public static class RowGuidComparator implements EqualityComparator{
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null) return false;
			if(objB == null) return false;
			if(objA == objB) return true;
			if(!(objA instanceof UUIDSupport)) return false;
			if(!(objA instanceof UUIDSupport)) return false;
			if(!objA.getClass().equals(objB.getClass())) return false;
			UUIDSupport a = (UUIDSupport) objA;
			UUIDSupport b = (UUIDSupport) objB;
			if(a.getUUID() == null || b.getUUID() == null) return false;
			return a.getUUID().equals(b.getUUID());
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof UUIDSupport)) return 0;
			UUIDSupport o = (UUIDSupport) obj;
			if(o.getUUID() == null) return 0;
			return o.getUUID().hashCode();
		}
		
	}
	
	/**
	 * compares entities based on their ID
	 */
	public static class IdComparator implements Comparator<EntityBase> {
		@Override
		public int compare(EntityBase lhs, EntityBase rhs) {
			return lhs.getId().compareTo(rhs.getId());
		}		
	}
	
	// END Comparators
	///////////////////////////////////////////////////////////////////////////
	
}
