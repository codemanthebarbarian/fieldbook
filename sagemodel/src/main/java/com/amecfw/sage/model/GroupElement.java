package com.amecfw.sage.model;

import com.amecfw.sage.persistence.DaoSession;
import de.greenrobot.dao.DaoException;

import com.amecfw.sage.persistence.ElementDao;
import com.amecfw.sage.persistence.ElementGroupDao;
import com.amecfw.sage.persistence.GroupElementDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.UUID;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
// KEEP INCLUDES END
/**
 * Entity mapped to table GROUP_ELEMENT.
 */
public class GroupElement extends EntityBase  implements UUIDSupport {

    private Long id;
    /** Not-null value. */
    private String rowGuid;
    private String flags;
    private Long elementGroupID;
    private Long elementID;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient GroupElementDao myDao;

    private ElementGroup elementGroup;
    private Long elementGroup__resolvedKey;

    private Element element;
    private Long element__resolvedKey;


    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public GroupElement() {
    }

    public GroupElement(Long id) {
        this.id = id;
    }

    public GroupElement(Long id, String rowGuid, String flags, Long elementGroupID, Long elementID) {
        this.id = id;
        this.rowGuid = rowGuid;
        this.flags = flags;
        this.elementGroupID = elementGroupID;
        this.elementID = elementID;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getGroupElementDao() : null;
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

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public Long getElementGroupID() {
        return elementGroupID;
    }

    public void setElementGroupID(Long elementGroupID) {
        this.elementGroupID = elementGroupID;
    }

    public Long getElementID() {
        return elementID;
    }

    public void setElementID(Long elementID) {
        this.elementID = elementID;
    }

    /** To-one relationship, resolved on first access. */
    public ElementGroup getElementGroup() {
        Long __key = this.elementGroupID;
        if (elementGroup__resolvedKey == null || !elementGroup__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ElementGroupDao targetDao = daoSession.getElementGroupDao();
            ElementGroup elementGroupNew = targetDao.load(__key);
            synchronized (this) {
                elementGroup = elementGroupNew;
            	elementGroup__resolvedKey = __key;
            }
        }
        return elementGroup;
    }

    public void setElementGroup(ElementGroup elementGroup) {
        synchronized (this) {
            this.elementGroup = elementGroup;
            elementGroupID = elementGroup == null ? null : elementGroup.getId();
            elementGroup__resolvedKey = elementGroupID;
        }
    }

    /** To-one relationship, resolved on first access. */
    public Element getElement() {
        Long __key = this.elementID;
        if (element__resolvedKey == null || !element__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ElementDao targetDao = daoSession.getElementDao();
            Element elementNew = targetDao.load(__key);
            synchronized (this) {
                element = elementNew;
            	element__resolvedKey = __key;
            }
        }
        return element;
    }

    public void setElement(Element element) {
        synchronized (this) {
            this.element = element;
            elementID = element == null ? null : element.getId();
            element__resolvedKey = elementID;
        }
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
