package com.amecfw.sage.model.service;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.Photo;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.StationDao;
import com.amecfw.sage.proxy.LocationProxy;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.proxy.ViewModel;

public class StationService {
	
private DaoSession session;
	
	public StationService(DaoSession session){
		this.session = session;
	}
	
	public void save(Station station, Location location){
		if(location != null){//need to save location first
			if(location.getId() == null || location.getId() < 1){
				//New location so need to save it
				location.setSite(station.getProjectSite().getSite());
				location.setId(null);
				session.getLocationDao().insert(location);
			}
			station.setLocation(location);
		}
		save(station);
	}
	
	public void save(Station station){
		station.setId(null);
		if(station.getRowGuid() == null) station.setRowGuid();
		session.getStationDao().insert(station);
		MetaDataService.save(station, session.getStationMetaDao());
	}
	
	public void update(Station station, Location location){
		if(station.getLocation() != null){//need to save or update location first
			if(location.getId() != null || station.getLocation().getId() > 0){
				station.getLocation().setSite(station.getProjectSite().getSite());
				session.getLocationDao().update(station.getLocation());
			}else{ 
				station.getLocation().setSite(station.getProjectSite().getSite());
				station.getLocation().setId(null);
				session.getLocationDao().insert(station.getLocation());
			}
		} else if(location != null){
			location.setSite(station.getProjectSite().getSite());
			location.setId(null);
			session.getLocationDao().insert(location);
			station.setLocation(location);
		}
		session.getStationDao().update(station);
		MetaDataService.update(station, session.getStationMetaDao());
	}
	
	public void update(Station station){
		session.getStationDao().update(station);
		MetaDataService.update(station, session.getStationMetaDao());
	}
	
	public List<Station> find(Location location, ProjectSite projectSite){
		StationDao dao = session.getStationDao();
		List<Station> results = dao.queryBuilder().where(StationDao.Properties.ProjectSiteID.eq(projectSite.getId())
				, StationDao.Properties.LocationID.eq(location.getId())).list();
		if(results == null || results.isEmpty()) return null;
		return results;
	}
	
	public List<Station> find(ProjectSite projectSite, String stationType){
		if(projectSite == null || stationType == null) return null;
		StationDao dao = session.getStationDao();
		List<Station> results = dao.queryBuilder().where(StationDao.Properties.ProjectSiteID.eq(projectSite.getId()), StationDao.Properties.StationType.eq(stationType)).list();
		if(results == null || results.isEmpty()) return null;
		return results;
	}
	
	public List<Station> find(ProjectSite projectSite){
		StationDao dao = session.getStationDao();
		List<Station> results = dao.queryBuilder().where(StationDao.Properties.ProjectSiteID.eq(projectSite.getId())).list();
		if(results == null || results.isEmpty()) return null;
		return results;
	}
	
	public List<Station> getSubstations(Station parent){
		return session.getStationDao().queryBuilder().where(StationDao.Properties.RootID.eq(parent.getId())).list();
	}
	
	public List<Station> getSubstations(Station parent, String type){
		return session.getStationDao().queryBuilder().where(StationDao.Properties.RootID.eq(parent.getId())
				, StationDao.Properties.StationType.eq(type)).list();
	}
	
	public Station getSubstation(Station parent, String type, String name){
		List<Station> subStations =  session.getStationDao().queryBuilder().where(StationDao.Properties.RootID.eq(parent.getId())
				, StationDao.Properties.StationType.eq(type)
				, StationDao.Properties.Name.eq(name)).list();
		if(subStations == null || subStations.size() == 0) return null;
		else return subStations.get(0);
	}
	
	public void deleteCascade(Station station, boolean deleteRootLocation){
		List<Station> children = findChildren(station);
		if(children != null && children.size() > 0) for(Station child: children) deleteCascade(child, true);
		new PhotoService(session).delete(station);
		new ObservationService(session).delete(station);
		new MeasurementService(session).delete(station);
		new ElementService(session).delete(station);
		if(deleteRootLocation){
			Location l = station.getLocation();
			new LocationService(session).delete(l);
		}
	}
	
	public List<Station> findChildren(Station parent){
		return session.getStationDao().queryBuilder().where(StationDao.Properties.RootID.eq(parent.getId())).list();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Station Proxy methods
	
	public StationProxy getStationProxy(Station station){
		StationProxy result = null;
		if(station == null) return result;
		result = new StationProxy();
		result.setModel(station);
		result.setProjectSite(station.getProjectSite());
		result.setLocation(station.getLocation());
		if(station.getLocation() != null){
			LocationService ls = new LocationService(session);
			result.setLocationProxy(ls.getProxy(station.getLocation()));
			if(result.getLocationProxy().getCoordinates() != null && result.getLocationProxy().getLocations().size() > 0){
				result.setGpsLocation(result.getLocationProxy().getLocations().get(0));
			}
		}
		if(station.getStation() != null) result.setRoot(getStationProxy(station.getStation()));
		List<Photo> photos = new PhotoService(session).find(station);
		if(photos != null && photos.size() > 0) result.setPhotos(PhotoService.convertToProxy(photos, station));
		result.setObservations(new ObservationService(session).findObservations(station));
		result.setMeasurements(new MeasurementService(session).find(station));
		return result;
	}
	
	public static void generateLocationProxy(StationProxy proxy, boolean overwrite){
		LocationProxy locationProxy = proxy.getLocationProxy();
		if(locationProxy == null){
			locationProxy = new LocationProxy();
			overwrite = true; //we've created a new proxy, so always overwrite since technically we are not overwriting anything
		}
		if(overwrite){
			locationProxy.setModel(proxy.getLocation());
			locationProxy.setSite(proxy.getProjectSite().getSite());
			if(proxy.getGpsLocation() != null){
				locationProxy.setLocations(new ArrayList<android.location.Location>(1));
				locationProxy.getLocations().add(proxy.getGpsLocation());
				locationProxy.setCoordinates(LocationService.convertLocations(locationProxy.getLocations(), LocationService.FEATURE_TYPE_POINT));
			}
		}
	}
	
	public boolean saveOrUpdateInTransaction(StationProxy proxy){
		boolean result = false;
		SQLiteDatabase db = session.getDatabase();
		db.beginTransaction();
		try{
			saveOrUpdate(proxy);
			//Everything persisted OK
			db.setTransactionSuccessful(); 
			result = true;
		} catch(Exception ex){
			Log.e("sulphurFieldbook.collectionForm.Services",ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
			result = false;
		}finally{
			db.endTransaction();
		}
		return result;
	}
	
	public void saveOrUpdate(StationProxy proxy){
		if(proxy.getModel().getId() == null || proxy.getModel().getId() == 0) save(proxy);
		else update(proxy);
	}
	
	public void save(StationProxy proxy){
		if(proxy.getLocationProxy() != null) {
			new LocationService(session).saveOrUpdateProxy(proxy.getLocationProxy());
			proxy.getModel().setLocation(proxy.getLocationProxy().getModel());
		}
		//Save the station
		save(proxy.getModel());
		//Save observations
		if(proxy.getObservations() != null) new ObservationService(session).saveOrUpdate(proxy.getObservations(), proxy.getModel());
		//Save measurements
		if(proxy.getMeasurements() != null) new MeasurementService(session).saveOrUpdate(proxy.getMeasurements(), proxy.getModel());
		//TODO: Save photos
		if(proxy.getPhotos() != null) new PhotoService(session).saveOrUpdate(proxy.getPhotos(), proxy.getModel());
	}
	
	public void update(StationProxy proxy){
		if(proxy.getLocationProxy() != null){
			new LocationService(session).saveOrUpdateProxy(proxy.getLocationProxy());
			proxy.getModel().setLocation(proxy.getLocationProxy().getModel());
		}
		update(proxy.getModel());
		//Save observations
		if(proxy.getObservations() != null) new ObservationService(session).saveOrUpdate(proxy.getObservations(), proxy.getModel());
		//Save measurements
		if(proxy.getMeasurements() != null) new MeasurementService(session).saveOrUpdate(proxy.getMeasurements(), proxy.getModel());
		//TODO: Save photos
		if(proxy.getPhotos() != null) new PhotoService(session).saveOrUpdate(proxy.getPhotos(), proxy.getModel());
	}
	
	public void updateFromProxy(StationProxy proxy, ViewModel viewModel){
		DescriptorServices.getByFieldDescriptor(viewModel, proxy.getModel());
		new ObservationService(session).updateAnnotations(viewModel, proxy.getObservations());
		//TODO: need to implement methods
		//new MeasurementService(session).updateAnnotations(viewModel, proxy.getMeasurements());
	}
	
	public static void updateFromViewModel(StationProxy stationProxy, ViewModel viewModel, List<PhotoProxy> photos){
		if(stationProxy == null || viewModel == null) return;
		if(stationProxy.getModel() == null){
			stationProxy.setModel(new Station());
			stationProxy.getModel().setRowGuid();
			stationProxy.getModel().setProjectSite(stationProxy.getProjectSite());
		}
		DescriptorServices.setByFieldDescriptor(viewModel, stationProxy.getModel());
		stationProxy.setObservations(new ObservationService(SageApplication.getInstance().getDaoSession()).fromAnnotations(viewModel));
		stationProxy.setMeasurements(new MeasurementService(SageApplication.getInstance().getDaoSession()).fromAnnotations(viewModel));
		stationProxy.setPhotos(photos);
	}
	
	// END Station Proxy Methods
	///////////////////////////////////////////////////////////////////////////

}
