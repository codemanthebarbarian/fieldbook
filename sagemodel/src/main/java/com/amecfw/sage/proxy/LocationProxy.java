package com.amecfw.sage.proxy;

import java.util.List;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.model.service.LocationService;
import com.amecfw.sage.util.CollectionOperations;

public class LocationProxy extends Proxy<com.amecfw.sage.model.Location> {
	
	private static final int SYNCED = 0;
	private static final int COORD = 1;
	private static final int LOC = 3;
	
	private List<Coordinate> coordinates;
	private List<android.location.Location> locations;
	private Site site;
	private String featureType;
	private int isSynced = SYNCED;
	
	public List<Coordinate> getCoordinates() {
		if(isSynced == LOC) syncFromLocations();
		return coordinates;
	}
	public void setCoordinates(List<Coordinate> coordinates) {
		isSynced = COORD;
		this.coordinates = coordinates;
	}
	public List<android.location.Location> getLocations() {
		if(isSynced == COORD) syncFromCoordinates();
		return locations;
	}
	public void setLocations(List<android.location.Location> locations) {
		isSynced = LOC;
		this.locations = locations;
	}
	public String getFeatureType() {
		return featureType;
	}
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}	
	public Site getSite() {
		return site;
	}
	public void setSite(Site site) {
		this.site = site;
	}
	
	public void syncFromLocations(){
		if(locations == null) {
			coordinates = null;
			return;
		}
		if(coordinates == null){ 
			coordinates = LocationService.convertLocations(locations, featureType);
			return;
		}
		LocationService.CoordinateXYZComparator xyzComparator = new LocationService.CoordinateXYZComparator();
		List<Coordinate> tmp = LocationService.convertLocations(locations, featureType);
		for(Coordinate c : coordinates){
			int index = CollectionOperations.indexOf(tmp, c, xyzComparator);
			if(index > -1) tmp.set(index, c);
		}
		coordinates = tmp;
		isSynced = SYNCED;
	}
	
	public void syncFromCoordinates(){
		if(coordinates == null) {
			locations = null;
			return;
		}
		if(locations == null){ 
			locations = LocationService.convertCoordinates(coordinates);
			return;
		}
		LocationService.AndroidLocationXYZComparator xyzComparator = new LocationService.AndroidLocationXYZComparator();
		List<android.location.Location> tmp = LocationService.convertCoordinates(coordinates);
		for(android.location.Location l : locations){
			int index = CollectionOperations.indexOfT(tmp, l, xyzComparator);
			if(index > -1) tmp.set(index, l);
		}
		locations = tmp;
		isSynced = SYNCED;
	}
	
	public boolean isMultiFeature(){
		if(coordinates == null || coordinates.size() == 0) return false;
		if(featureType != null) return coordinates.size() == get(featureType).size();
		String type = coordinates.get(0).getFeatureType();
		for(Coordinate coord : coordinates){
			if(!type.equalsIgnoreCase(coord.getFeatureType())) return true;
		}
		return false;
	}
	
	/**
	 * It could be possible for a location to have different feature types. If isMultiFeature is true. 
	 * Then use this to get a specific type of coordinate.
	 * @param featureType
	 * @return
	 */
	public List<Coordinate> get(String featureType){
		if(coordinates == null) return null;
		Coordinate tmp = new Coordinate();
		tmp.setFeatureType(featureType);
		List<Coordinate> results = CollectionOperations.match(coordinates, tmp, new LocationService.CoordinateTypeComparator());
		return results;
	}
	
}
