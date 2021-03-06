package com.amecfw.sage.model;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.UUID;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.os.Parcel;
import android.os.Parcelable;
// KEEP INCLUDES END
/**
 * Entity mapped to table OWNER.
 */
public class Owner extends EntityBase  implements UUIDSupport, Parcelable {

    private Long id;
    /** Not-null value. */
    private String rowGuid;
    /** Not-null value. */
    private String name;
    /** Not-null value. */
    private String type;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Owner() {
    }

    public Owner(Long id) {
        this.id = id;
    }

    public Owner(Long id, String rowGuid, String name, String type) {
        this.id = id;
        this.rowGuid = rowGuid;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getRowGuid() {
        return rowGuid;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setRowGuid(String rowGuid) {
        this.rowGuid = rowGuid;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    /** Not-null value. */
    public String getType() {
        return type;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setType(String type) {
        this.type = type;
    }

    // KEEP METHODS - put your custom methods here
    @Override
   	public UUID getUUID() {
       	return uuidFromString(rowGuid);
   	}

   	@Override
   	public void setUUID(UUID rowGuid) {
   		this.rowGuid = uuidFromUUID(rowGuid); 
   		
   	}

   	@Override
   	public void generateUUID() {
   		rowGuid = UUID.randomUUID().toString();
   	}
   	
   	/**
   	 * Generates a new id by calling generateUUID()
   	 * @see com.amecfw.sage.vegapp.model.UUIDSupport#setRowGuid()
   	 */
   	@Override
   	public void setRowGuid(){
   		generateUUID();
   	}
       
       @Override
   	public void fromXml(XmlPullParser parser) throws XmlPullParserException,
   			IOException {
   		// TODO Auto-generated method stub
   		
   	}
       
       @Override
      	public void toXml(XmlSerializer serializer) throws XmlPullParserException,
      			IOException {
      		// TODO Auto-generated method stub
      		
      	}
       
    public static final Parcelable.Creator<Owner> CREATOR = 
    		new Parcelable.Creator<Owner>() {
    	@Override
    	public Owner createFromParcel(Parcel in) { return new Owner(in); }
    	@Override
    	public Owner[] newArray(int size) { return new Owner[size]; }
			};
			
	public Owner(Parcel in){
		this.id = in.readLong();
		this.setRowGuid(in.readString());
		this.name = in.readString();
		this.type = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags){
		dest.writeLong(this.id);
		dest.writeString(this.getRowGuid());
		dest.writeString(this.name);
		dest.writeString(this.type);
	}
   	
   	@Override
   	public int describeContents(){
   		return 0;
   	}
    // KEEP METHODS END

}
