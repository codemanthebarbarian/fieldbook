package com.amecfw.sage.model;

import java.util.List;
import com.amecfw.sage.persistence.DaoSession;
import de.greenrobot.dao.DaoException;

import com.amecfw.sage.persistence.LocationDao;
import com.amecfw.sage.persistence.ProjectSiteDao;
import com.amecfw.sage.persistence.StationDao;
import com.amecfw.sage.persistence.StationMetaDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.UUID;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
// KEEP INCLUDES END
/**
 * Entity mapped to table STATION.
 */
public class Station extends EntityBase  implements UUIDSupport, MetaDataSupport<StationMeta> {

    private Long id;
    /** Not-null value. */
    private String rowGuid;
    /** Not-null value. */
    private String name;
    private java.util.Date surveyDate;
    private java.util.Date surveyTime;
    private String details;
    private String description;
    /** Not-null value. */
    private String stationType;
    private String timeZone;
    private Long locationID;
    private Long rootID;
    private Long projectSiteID;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient StationDao myDao;

    private Location location;
    private Long location__resolvedKey;

    private Station station;
    private Long station__resolvedKey;

    private ProjectSite projectSite;
    private Long projectSite__resolvedKey;

    private List<StationMeta> metaData;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Station() {
    }

    public Station(Long id) {
        this.id = id;
    }

    public Station(Long id, String rowGuid, String name, java.util.Date surveyDate, java.util.Date surveyTime, String details, String description, String stationType, String timeZone, Long locationID, Long rootID, Long projectSiteID) {
        this.id = id;
        this.rowGuid = rowGuid;
        this.name = name;
        this.surveyDate = surveyDate;
        this.surveyTime = surveyTime;
        this.details = details;
        this.description = description;
        this.stationType = stationType;
        this.timeZone = timeZone;
        this.locationID = locationID;
        this.rootID = rootID;
        this.projectSiteID = projectSiteID;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getStationDao() : null;
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

    public java.util.Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(java.util.Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public java.util.Date getSurveyTime() {
        return surveyTime;
    }

    public void setSurveyTime(java.util.Date surveyTime) {
        this.surveyTime = surveyTime;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** Not-null value. */
    public String getStationType() {
        return stationType;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Long getLocationID() {
        return locationID;
    }

    public void setLocationID(Long locationID) {
        this.locationID = locationID;
    }

    public Long getRootID() {
        return rootID;
    }

    public void setRootID(Long rootID) {
        this.rootID = rootID;
    }

    public Long getProjectSiteID() {
        return projectSiteID;
    }

    public void setProjectSiteID(Long projectSiteID) {
        this.projectSiteID = projectSiteID;
    }

    /** To-one relationship, resolved on first access. */
    public Location getLocation() {
        Long __key = this.locationID;
        if (location__resolvedKey == null || !location__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LocationDao targetDao = daoSession.getLocationDao();
            Location locationNew = targetDao.load(__key);
            synchronized (this) {
                location = locationNew;
            	location__resolvedKey = __key;
            }
        }
        return location;
    }

    public void setLocation(Location location) {
        synchronized (this) {
            this.location = location;
            locationID = location == null ? null : location.getId();
            location__resolvedKey = locationID;
        }
    }

    /** To-one relationship, resolved on first access. */
    public Station getStation() {
        Long __key = this.rootID;
        if (station__resolvedKey == null || !station__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            StationDao targetDao = daoSession.getStationDao();
            Station stationNew = targetDao.load(__key);
            synchronized (this) {
                station = stationNew;
            	station__resolvedKey = __key;
            }
        }
        return station;
    }

    public void setStation(Station station) {
        synchronized (this) {
            this.station = station;
            rootID = station == null ? null : station.getId();
            station__resolvedKey = rootID;
        }
    }

    /** To-one relationship, resolved on first access. */
    public ProjectSite getProjectSite() {
        Long __key = this.projectSiteID;
        if (projectSite__resolvedKey == null || !projectSite__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ProjectSiteDao targetDao = daoSession.getProjectSiteDao();
            ProjectSite projectSiteNew = targetDao.load(__key);
            synchronized (this) {
                projectSite = projectSiteNew;
            	projectSite__resolvedKey = __key;
            }
        }
        return projectSite;
    }

    public void setProjectSite(ProjectSite projectSite) {
        synchronized (this) {
            this.projectSite = projectSite;
            projectSiteID = projectSite == null ? null : projectSite.getId();
            projectSite__resolvedKey = projectSiteID;
        }
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<StationMeta> getMetaData() {
        if (metaData == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            StationMetaDao targetDao = daoSession.getStationMetaDao();
            List<StationMeta> metaDataNew = targetDao._queryStation_MetaData(id);
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
    
    @Override
   	public void setMetaData(List<StationMeta> metaData) {
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