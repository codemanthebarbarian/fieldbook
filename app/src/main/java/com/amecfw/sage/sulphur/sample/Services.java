package com.amecfw.sage.sulphur.sample;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.LocationMeta;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.LocationService;
import com.amecfw.sage.model.service.MetaDataService;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.persistence.DaoSession;

public class Services {

	private DaoSession session;
	
	public static String LOCATION_DEPTHS_META = "Sulphur Sample Depths";
	
	public Services(DaoSession session){
		this.session = session;
	}
	
	public List<SampleList.LocationProxy> getLocations(ProjectSite projectSite){
		List<Location> locations = new LocationService(session).findLocation(projectSite.getSite());
		if(locations == null || locations.isEmpty()) return null;
		List<SampleList.LocationProxy> results = new LinkedList<SampleList.LocationProxy>();
		for(Location location : locations){
			SampleList.LocationProxy temp = build(location, projectSite);
			if(temp != null) results.add(temp);
		}
		return results;
	}
	
	public static String[] getDepths(){
		return new String[] {"Litter", "0-5", "0-15", "5-15", "15-30", "30-45", "45-60"};
	}
 	
	/**
	 * Saves a new proxy (Location) to the database
	 * @param proxy
	 */
	public boolean save(SampleList.LocationProxy proxy, Site site){
		boolean result = false;
		SQLiteDatabase db = session.getDatabase();
		db.beginTransaction();
		try{
			proxy.getModel().setSite(site);
			if (proxy.getModel().getRowGuid() == null) proxy.getModel().setRowGuid();
			setLatLong(proxy);
			session.getLocationDao().insert(proxy.getModel());
			if(proxy.getDepths() != null){
				if(proxy.getDepths().getRowGuid() == null) proxy.getDepths().setRowGuid();
				proxy.getDepths().setLocation(proxy.getModel());
				session.getLocationMetaDao().insert(proxy.getDepths());
			}
			if(proxy.getCoordinates() != null && proxy.getCoordinates().size() > 0){
				if(proxy.getCoordinates().get(0) != null){
					Coordinate temp = proxy.getCoordinates().get(0);
					temp.setLocation(proxy.getModel());
					temp.setFeatureType(LocationService.FEATURE_TYPE_POINT);
					session.insert(temp);
				}
			}
			//Everything persisted OK
			db.setTransactionSuccessful();
			result = true;
		}catch(Exception ex){
			result = false;
		}finally{
			db.endTransaction();
		}
		return result;
	}
	
	/**
	 * updates an exiting proxy (Location) in the database
	 * @param proxy
	 */
	public void update(SampleList.LocationProxy proxy){
		
	}
	
	public void buildFromViewModel(SampleList.LocationProxy proxy){
		if(proxy == null || proxy.getViewModel() == null) return;
		if(proxy.getModel() == null) proxy.setModel(new Location());
		proxy.getModel().setName(proxy.getViewModel().getName());
		if(proxy.getViewModel().getDepths() != null){
			if(proxy.getDepths() == null) {
				LocationMeta depths = new LocationMeta();
				depths.setName(LOCATION_DEPTHS_META);
				proxy.setDepths(depths);
			}
			proxy.getDepths().setValue(proxy.getViewModel().getDepths());
		}
		if(proxy.getViewModel().getLocation() != null){
			proxy.setGpsLocation(proxy.getViewModel().getLocation());
			proxy.setCoordinates(Arrays.asList(new Coordinate[] { LocationService.convert(proxy.getGpsLocation())}));
		}
	}
	
	public void buildFromModel(SampleList.LocationProxy proxy){
		if(proxy.getViewModel() == null) proxy.setViewModel(new SampleList.ViewModel());
		if(proxy.getModel() != null) proxy.getViewModel().setName(proxy.getModel().getName());
		if(proxy.getDepths() != null) proxy.getViewModel().setDepths(proxy.getDepths().getValue());
		if(proxy.getCoordinates() != null && proxy.getCoordinates().size() > 0)
			proxy.setGpsLocation(LocationService.convert(proxy.getCoordinates().get(0)));
		proxy.getViewModel().setCompleted(proxy.getStation() != null);
	}
	
	/**
	 * Set the lat and long in the location based on the coordinate collection
	 * (currently uses the first coordinate found in the collection)
	 * in the proxy
	 * @param proxy
	 */
	private void setLatLong(SampleList.LocationProxy proxy){
		if (proxy == null) return;
		if (proxy.getModel() == null) return;
		if (proxy.getCoordinates() == null || proxy.getCoordinates().size() < 1) return;
		for(Coordinate coord : proxy.getCoordinates()){
			if(coord != null){
				proxy.getModel().setLatitude(Double.toString(coord.getLatitude()));
				proxy.getModel().setLongitude(Double.toString(coord.getLongitude()));
				proxy.getModel().setElevation(Double.toString(coord.getElevation()));
				return;
			}
		}
	}
	
	private SampleList.LocationProxy build(Location location, ProjectSite projectSite){
		if(location == null) return null;
		LocationService service = new LocationService(session);
		SampleList.LocationProxy proxy = new SampleList.LocationProxy();
		proxy.setModel(location);
		proxy.setCoordinates(service.findCoordinates(location));
		proxy.setDepths(MetaDataService.MetaSupportExtensionMethods.find(location, LOCATION_DEPTHS_META));
		proxy.setStation(get(proxy.getModel(), projectSite));
		buildFromModel(proxy);
		return proxy;
	}
	
	private Station get(Location location, ProjectSite projectSite){
		if(projectSite == null || location == null || location.getId() == null) return null;
		StationService stnService = new StationService(session);
		List<Station> stations = stnService.find(location, projectSite);
		if(stations == null || stations.isEmpty()) return null;		
		return stations.get(0); // return the first
	}
	
}
