package com.amecfw.sage.model;

import com.amecfw.sage.persistence.DaoSession;
import de.greenrobot.dao.DaoException;

import com.amecfw.sage.persistence.ObservationDao;
import com.amecfw.sage.persistence.ObservationMetaDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.UUID;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
// KEEP INCLUDES END
/**
 * Entity mapped to table OBSERVATION_META.
 */
public class ObservationMeta extends EntityBase  implements MetaElement, UUIDSupport {

    private Long id;
    /** Not-null value. */
    private String rowGuid;
    private String name;
    private String value;
    private long parentID;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient ObservationMetaDao myDao;

    private Observation observation;
    private Long observation__resolvedKey;


    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public ObservationMeta() {
    }

    public ObservationMeta(Long id) {
        this.id = id;
    }

    public ObservationMeta(Long id, String rowGuid, String name, String value, long parentID) {
        this.id = id;
        this.rowGuid = rowGuid;
        this.name = name;
        this.value = value;
        this.parentID = parentID;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getObservationMetaDao() : null;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getParentID() {
        return parentID;
    }

    public void setParentID(long parentID) {
        this.parentID = parentID;
    }

    /** To-one relationship, resolved on first access. */
    public Observation getObservation() {
        long __key = this.parentID;
        if (observation__resolvedKey == null || !observation__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ObservationDao targetDao = daoSession.getObservationDao();
            Observation observationNew = targetDao.load(__key);
            synchronized (this) {
                observation = observationNew;
            	observation__resolvedKey = __key;
            }
        }
        return observation;
    }

    public void setObservation(Observation observation) {
        if (observation == null) {
            throw new DaoException("To-one property 'parentID' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.observation = observation;
            parentID = observation.getId();
            observation__resolvedKey = parentID;
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
