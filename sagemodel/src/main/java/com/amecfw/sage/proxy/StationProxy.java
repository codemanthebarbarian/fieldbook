package com.amecfw.sage.proxy;

import java.util.List;

import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.Measurement;
import com.amecfw.sage.model.Observation;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.Station;

public class StationProxy extends Proxy<Station> {
	private android.location.Location gpsLocation;
	private List<Observation> observations;
	private List<Measurement> measurements;
	private Location location;
	private ProjectSite projectSite;
	private StationProxy root;
	private LocationProxy locationProxy;
	private List<PhotoProxy> photos;
	
	public android.location.Location getGpsLocation() {
		return gpsLocation;
	}
	public void setGpsLocation(android.location.Location gpsLocation) {
		this.gpsLocation = gpsLocation;
	}
	public List<Observation> getObservations() {
		return observations;
	}
	public void setObservations(List<Observation> observations) {
		this.observations = observations;
	}
	public List<Measurement> getMeasurements() {
		return measurements;
	}
	public void setMeasurements(List<Measurement> measurements) {
		this.measurements = measurements;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public ProjectSite getProjectSite() {
		return projectSite;
	}
	public void setProjectSite(ProjectSite projectSite) {
		this.projectSite = projectSite;
	}
	public StationProxy getRoot() {
		return root;
	}
	public void setRoot(StationProxy root) {
		this.root = root;
	}
	public LocationProxy getLocationProxy() {
		return locationProxy;
	}
	public void setLocationProxy(LocationProxy locationProxy) {
		this.locationProxy = locationProxy;
	}
	public List<PhotoProxy> getPhotos() {
		return photos;
	}
	public void setPhotos(List<PhotoProxy> photos) {
		this.photos = photos;
	}
}
