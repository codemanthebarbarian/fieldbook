package com.amecfw.sage.model;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public abstract class EntityBase implements Entity, UUIDSupport {
	
	private EqualityComparator comparator;
		
	public EqualityComparator getComparator() {
		return comparator;
	}

	public void setComparator(EqualityComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(comparator != null) return comparator.equalsTo(this, o);
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		if(comparator != null) return comparator.getHashCode(this);
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ getUUID().hashCode();
		return result;
	}
	
	protected static UUID uuidFromString(String rowGuid){
		try{
    		return rowGuid == null ? null : UUID.fromString(rowGuid);
    	}catch(IllegalArgumentException ife){
    		return null;
    	}
	}
	
	protected static String uuidFromUUID(UUID rowGuid){
		return rowGuid == null ? null : rowGuid.toString(); 
	}
	
	////////////////////////////////////////////////
	// XML Serialization for the EntityBase class //
	////////////////////////////////////////////////

	public abstract void fromXml(XmlPullParser parser) throws XmlPullParserException, IOException; 
	
	public abstract void toXml(XmlSerializer serializer) throws XmlPullParserException, IOException;
	
	public String toXml()throws XmlPullParserException {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
		} catch (IOException ioe){
			Log.e(this.getClass().getName(), ioe.getMessage());
			return null;
		}
		return writer.toString();
	}
	
	protected static final String rowGuidTag = "rowGuid";
	
	protected void checkTag(String[] allowedTagNames, String tagName) throws XmlPullParserException{
		if(allowedTagNames == null) throw new NullPointerException();
		if(tagName == null) throw new NullPointerException();
		if(tagName.equals(EntityBase.rowGuidTag)) return;
		for(String tag : allowedTagNames){
			if(tagName.equals(tag)) return;
		}
		throw new XmlPullParserException(String.format("Unexpected tag: '%s'", tagName));
	}
	
	protected void checkStartTag(String expectedStartTag, XmlPullParser parser) throws XmlPullParserException, IllegalStateException{
		int eventType = parser.getEventType();
		if(eventType != XmlPullParser.START_TAG) throw new IllegalStateException("parser not on a start tag");
		String startTag = parser.getName();
		if(!startTag.equals(expectedStartTag))throw new IllegalStateException(String.format("not on a '%s' start tag", expectedStartTag));
		if(parser.isEmptyElementTag()) throw new IllegalStateException(String.format("empty '%s' tag", expectedStartTag));
	}	
	
	protected static Date parseDate(String date) throws XmlPullParserException{
		try {
			return DateFormat.getDateInstance(DateFormat.LONG).parse(date);
		} catch (ParseException pe) {
			throw new XmlPullParserException(String.format("Invalid date format %s", date), null, pe);
		}
	}
	
	protected static void serializeString(XmlSerializer serializer, String tagName, String data) throws IllegalArgumentException, IllegalStateException, IOException{
		if(data == null) return;
		serializer.startTag("", tagName);
		serializer.text(data);
		serializer.endTag("", tagName);
	}
	
	protected static void serializeDate(XmlSerializer serializer, String tagName, Date date) throws IllegalArgumentException, IllegalStateException, IOException{
		if(date == null) return;
		serializeString(serializer, tagName, DateFormat.getDateInstance(DateFormat.LONG).format(date));
	}
	
	protected static Date parseTime(String date) throws XmlPullParserException{
		try {
			return DateFormat.getTimeInstance(DateFormat.LONG).parse(date);
		} catch (ParseException pe) {
			throw new XmlPullParserException(String.format("Invalid time format %s", date), null, pe);
		}
	}
	
	protected static void serializeInt(XmlSerializer serializer, String tagName, Integer _int) throws IllegalArgumentException, IllegalStateException, IOException{
		if(_int == null) return;
		serializeString(serializer, tagName, _int.toString());
	}
	
	protected static void serializeDouble(XmlSerializer serializer, String tagName, Double _double) throws IllegalArgumentException, IllegalStateException, IOException{
		if(_double == null) return;
		serializeString(serializer, tagName, _double.toString());
	}
	
	protected static void serializeTime(XmlSerializer serializer, String tagName,Date time) throws IllegalArgumentException, IllegalStateException, IOException{
		if(time == null) return;
		serializeString(serializer, tagName,DateFormat.getTimeInstance(DateFormat.LONG).format(time));
	}
	
}
