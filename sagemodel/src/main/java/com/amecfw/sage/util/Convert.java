package com.amecfw.sage.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

public class Convert {

	public static int[] toPrimitive(List<Integer> list){
		if(list == null) return null;
		return toPrimitive(list.toArray(new Integer[list.size()]));
	}
	
	public static int[] toPrimitive(Integer[] array){
		if(array == null) return null;
		int[] result = new int[array.length];
		for(int i = 0 ; i < array.length ; i++)
			result[i] = array[i];
		return result;
	}
	
	public static Stack<Integer> toStack(int[] array){
		Stack<Integer> result = new Stack<Integer>();
		if(array != null){
			for(int i:array)
				result.push(i);
		}
		return result;
	}
	
	/**
	 * tries to parse a string to a long value, if unable to parse, returns Long.MIN_VALUE
	 * @param longString
	 * @return the parsed number or Long.MIN_VALUE
	 */
	public static Long tryParseLong(String longString){
		if(longString == null) return Long.MIN_VALUE;
		try{
			return Long.parseLong(longString);
		}catch(NumberFormatException nfe){
			return Long.MIN_VALUE;
		}
	}
	
	public static int tryParseInt(String intString, int defaultValue){
		if(intString == null) return defaultValue;
		try{
			return Integer.parseInt(intString);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static String toStringOrNull(TextView textView){
		if(textView == null) return null;
		if(textView.getText() == null) return null;
		String result = textView.getText().toString();
		if(result != null){
			result = result.trim();
			if(result.isEmpty()) return null;
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getTagAs(View v, int key, T defaultValue){
		Object tmp = v.getTag(key);
		if(tmp == null) return defaultValue;
		try{
			return (T) tmp;
		}catch(ClassCastException cce){
			return defaultValue;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getTagAs(View v, T defaultValue){
		Object tmp = v.getTag();
		if(tmp == null) return defaultValue;
		try{
			return (T) tmp;
		}catch(ClassCastException cce){
			return defaultValue;
		}
	}
	
	/**
	 * returns the date using string format %tb %td, %tY
	 * or the provided nullDateOutput value if date is null
	 * @param date
	 * @param nullDateOutput
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static String dateToLongString(Date date, String nullDateOutput){
		return date == null ? nullDateOutput : String.format("%tb %td, %tY", date, date, date);
	}
	
	public static <T extends Parcelable> T[] convert(Parcelable[] items, Class<T[]> clazz){
		return items == null ? null : Arrays.copyOf(items, items.length, clazz);
	}
 	
}
