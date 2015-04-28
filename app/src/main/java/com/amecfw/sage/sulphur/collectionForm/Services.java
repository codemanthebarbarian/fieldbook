package com.amecfw.sage.sulphur.collectionForm;

import java.lang.reflect.Field;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.model.service.MeasurementService;
import com.amecfw.sage.model.service.ObservationService;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.persistence.DaoSession;

public class Services {

	private DaoSession session;
	
	public Services(DaoSession session){
		this.session = session;
	}
	
	public void buildFromViewModel(Create.CollectionProxy proxy){
		if(proxy.getViewModel() == null) return;
		if(proxy.getModel() == null){ 
			proxy.setModel(new Station());
		}
		DescriptorServices.setByFieldDescriptor(proxy.getViewModel(), proxy.getModel());
		ObservationService obsService = new ObservationService(session);
		proxy.setObservations(obsService.fromAnnotations(proxy.getViewModel()));
		if(proxy.getModel().getRootID() == null) proxy.getModel().setRowGuid();
	}
	
	public void buildFromModel(Create.CollectionProxy proxy){
		if(proxy.getViewModel() == null) proxy.setViewModel(new Create.ViewModel());
		if(proxy.getModel() != null){
			int updatedFields = DescriptorServices.getByFieldDescriptor(proxy.getViewModel(), proxy.getModel());
			Log.d("sulphur", "Fields updated: " + updatedFields);
		}
		if(proxy.getObservations() != null || proxy.getObservations().size() > 0){
			ObservationService obsService = new ObservationService(session);
			obsService.updateAnnotations(proxy.getViewModel(), proxy.getObservations());
		}
	}
	
	/**
	 * Hydrate the proxy from the provided station. Inflates all model classes and the viewmodel.
	 * @param proxy
	 * @param station
	 */
	public void hydrateProxy(Create.CollectionProxy proxy, Station station){
		proxy.setModel(station);
		proxy.setObservations(new ObservationService(session).findObservations(station));
		proxy.setMeasurements(new MeasurementService(session).find(station));
		//proxy.setPhotos(photos); TODO:create photo service and get station photos
		//proxy.setGpsLocation(gpsLocation);
	}
	
	public boolean save(Create.CollectionProxy proxy, Location location){
		boolean result = false;
		SQLiteDatabase db = session.getDatabase();
		db.beginTransaction();
		try{
			//Save the station
			new StationService(session).save(proxy.getModel(), location);
			//Save coordinate
			//Save observations
			if(proxy.getObservations() != null) new ObservationService(session).saveOrUpdate(proxy.getObservations(), proxy.getModel());
			//Save measurements
			if(proxy.getMeasurements() != null) new MeasurementService(session).saveOrUpdate(proxy.getMeasurements(), proxy.getModel());
			//Save photos
			//Everything persisted OK
			db.setTransactionSuccessful(); 
			result = true;
		} catch(Exception ex){
			Log.e("sulphur",ex.getMessage());
			result = false;
		}finally{
			db.endTransaction();
		}
		return result;
	}
	
	public boolean update(Create.CollectionProxy proxy, Location location){
		boolean result = false;
		SQLiteDatabase db = session.getDatabase();
		db.beginTransaction();
		try{
			//Save the station
			new StationService(session).update(proxy.getModel(), location);
			//Save coordinate
			//Save observations
			if(proxy.getObservations() != null) new ObservationService(session).saveOrUpdate(proxy.getObservations(), proxy.getModel());
			//Save measurements
			if(proxy.getMeasurements() != null) new MeasurementService(session).saveOrUpdate(proxy.getMeasurements(), proxy.getModel());
			//Save photos
			//Everything persisted OK
			db.setTransactionSuccessful(); 
			result = true;
		} catch(Exception ex){
			Log.e("sulphur",ex.getMessage());
			result = false;
		}finally{
			db.endTransaction();
		}
		return result;
	}
	
	public GroupObservation getGroupObservation(String fieldName, ObservationGroup group){
		Field field;
		try {
			field = Create.ViewModel.class.getField(fieldName);
		} catch (NoSuchFieldException e) {
			Log.e("sulphur", "Field not found in VeiwModel: " + fieldName);
			return null;
		}
		return ObservationService.get(field, group);
	}
	
}
