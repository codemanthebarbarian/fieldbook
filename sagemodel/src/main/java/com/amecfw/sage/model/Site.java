package com.amecfw.sage.model;

import com.amecfw.sage.persistence.DaoSession;
import de.greenrobot.dao.DaoException;

import com.amecfw.sage.persistence.SiteDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.UUID;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
// KEEP INCLUDES END
/**
 * Entity mapped to table SITE.
 */
public class Site extends EntityBase  implements UUIDSupport {

    private Long id;
    /** Not-null value. */
    private String rowGuid;
    private String name;
    private String node;
    private Long rootID;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient SiteDao myDao;

    private Site root;
    private Long root__resolvedKey;


    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Site() {
    }

    public Site(Long id) {
        this.id = id;
    }

    public Site(Long id, String rowGuid, String name, String node, Long rootID) {
        this.id = id;
        this.rowGuid = rowGuid;
        this.name = name;
        this.node = node;
        this.rootID = rootID;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getSiteDao() : null;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public Long getRootID() {
        return rootID;
    }

    public void setRootID(Long rootID) {
        this.rootID = rootID;
    }

    /** To-one relationship, resolved on first access. */
    public Site getRoot() {
        Long __key = this.rootID;
        if (root__resolvedKey == null || !root__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SiteDao targetDao = daoSession.getSiteDao();
            Site rootNew = targetDao.load(__key);
            synchronized (this) {
                root = rootNew;
            	root__resolvedKey = __key;
            }
        }
        return root;
    }

    public void setRoot(Site root) {
        synchronized (this) {
            this.root = root;
            rootID = root == null ? null : root.getId();
            root__resolvedKey = rootID;
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
    public boolean hasRoot(){
    	if(daoSession == null){//is disconnected or new
    		return root != null;
    	}
    	try{
    		return getRoot() != null;
    	}catch(DaoException e){
    		return root != null;
    	}
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
