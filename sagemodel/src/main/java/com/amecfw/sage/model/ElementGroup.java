package com.amecfw.sage.model;

import java.util.List;
import com.amecfw.sage.persistence.DaoSession;
import de.greenrobot.dao.DaoException;

import com.amecfw.sage.persistence.ElementGroupDao;
import com.amecfw.sage.persistence.ElementGroupMetaDao;
import com.amecfw.sage.persistence.GroupElementDao;
import com.amecfw.sage.persistence.OwnerDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.UUID;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
// KEEP INCLUDES END
/**
 * Entity mapped to table ELEMENT_GROUP.
 */
public class ElementGroup extends EntityBase  implements UUIDSupport, MetaDataSupport<ElementGroupMeta>, Ownership {

    private Long id;
    /** Not-null value. */
    private String rowGuid;
    /** Not-null value. */
    private String name;
    private Long ownerID;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient ElementGroupDao myDao;

    private Owner owner;
    private Long owner__resolvedKey;

    private List<ElementGroupMeta> metaData;
    private List<GroupElement> groupElements;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public ElementGroup() {
    }

    public ElementGroup(Long id) {
        this.id = id;
    }

    public ElementGroup(Long id, String rowGuid, String name, Long ownerID) {
        this.id = id;
        this.rowGuid = rowGuid;
        this.name = name;
        this.ownerID = ownerID;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getElementGroupDao() : null;
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

    public Long getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(Long ownerID) {
        this.ownerID = ownerID;
    }

    /** To-one relationship, resolved on first access. */
    public Owner getOwner() {
        Long __key = this.ownerID;
        if (owner__resolvedKey == null || !owner__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OwnerDao targetDao = daoSession.getOwnerDao();
            Owner ownerNew = targetDao.load(__key);
            synchronized (this) {
                owner = ownerNew;
            	owner__resolvedKey = __key;
            }
        }
        return owner;
    }

    public void setOwner(Owner owner) {
        synchronized (this) {
            this.owner = owner;
            ownerID = owner == null ? null : owner.getId();
            owner__resolvedKey = ownerID;
        }
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<ElementGroupMeta> getMetaData() {
        if (metaData == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ElementGroupMetaDao targetDao = daoSession.getElementGroupMetaDao();
            List<ElementGroupMeta> metaDataNew = targetDao._queryElementGroup_MetaData(id);
            synchronized (this) {
                if(metaData == null) {
                    metaData = metaDataNew;
                }
            }
        }
        return metaData;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetMetaData() {
        metaData = null;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<GroupElement> getGroupElements() {
        if (groupElements == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            GroupElementDao targetDao = daoSession.getGroupElementDao();
            List<GroupElement> groupElementsNew = targetDao._queryElementGroup_GroupElements(id);
            synchronized (this) {
                if(groupElements == null) {
                    groupElements = groupElementsNew;
                }
            }
        }
        return groupElements;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetGroupElements() {
        groupElements = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    @Override
    public boolean hasOwner(){
    	if(daoSession == null){//is disconnected or new
    		return owner != null;
    	}
    	try{
    		return getOwner() != null;
    	}catch(DaoException e){
    		return owner != null;
    	}
    }
    
    @Override
    public boolean hasMetaData(){
    	if(id == null || id < 1){
    		if(metaData == null || metaData.size() < 1) return false;
    		return true;
    	}else{
    		try{
    			getMetaData();
    			if(metaData == null || metaData.size() < 1) return false;
        		return true;
    		}catch(DaoException e){
    			return false;
    		}
    	}
    }
    
    public void setGroupElements(List<GroupElement> groupElements) {
  		this.groupElements = groupElements;		
  	}
      
      @Override
  	public void setMetaData(List<ElementGroupMeta> metaData) {
  		this.metaData = metaData;		
  	}
      
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
    // KEEP METHODS END

}
