package com.amecfw.sage.model.service;

import java.util.ArrayList;

import com.amecfw.sage.model.Measurement;
import com.amecfw.sage.model.Observation;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.proxy.StationProxy;

public class SurveyService {
	
private DaoSession session;
	
	public SurveyService(DaoSession session){
		this.session = session;
	}
	
	public void saveOrUpdate(StationProxy survey){
		if(survey.getModel().getId() == null || survey.getModel().getId() < 1) save(survey);
		else update(survey);
	}
	
	public void save(StationProxy survey){
		if(survey.getRoot() != null){
			survey.getModel().setStation(survey.getRoot().getModel());
		}
		//Save the survey
		StationService stnService = new StationService(session);
		stnService.save(survey.getModel(), survey.getLocation());
		//Save the observations
		if(survey.getObservations() != null){
			ObservationService obsService = new ObservationService(session);
			for (Observation observation : survey.getObservations()) {
				observation.setStation(survey.getModel());
				obsService.save(observation);
			}
		}
		//Save the measurements
		if(survey.getMeasurements() != null){
			MeasurementService mesService = new MeasurementService(session);
			for(Measurement measurement : survey.getMeasurements()){
				measurement.setStation(survey.getModel());
				mesService.save(measurement);
			}
		}
		
	}
	
	public void update(StationProxy survey){
		if(survey.getRoot() != null){
			survey.getModel().setStation(survey.getRoot().getModel());
		}
		StationService stnService = new StationService(session);
		stnService.update(survey.getModel(), survey.getLocation());
		if(survey.getObservations() == null) survey.setObservations(new ArrayList<Observation>());
		ObservationService obsService = new ObservationService(session);
		obsService.saveOrUpdate(survey.getObservations(), survey.getModel());
		// TODO: need to implement update in measurement service
		//if(survey.getMeasurments() == null) survey.setMeasurments(new ArrayList<Measurement>());
		//MeasurementService mesService = new MeasurementService(session);
		//mesService.update(survey.getMeasurments(), survey.getSurvey());
	}
	
	/**
	 * deletes the proxy and all underlying data (see deleteCascase(Station))
	 * all the items in the proxy are then cleared out.
	 * @param proxy
	 */
	public void deleteCascade(StationProxy proxy, boolean deleteRootLocation){
		if(proxy.getModel() != null && proxy.getModel().getId() != null)
			new StationService(session).deleteCascade(proxy.getModel(), deleteRootLocation);
		proxy.setModel(null);
		proxy.getMeasurements().clear();
		proxy.getObservations().clear();
		proxy.setLocationProxy(null);
		if(deleteRootLocation) proxy.setLocation(null);
		proxy.getPhotos().clear();
		proxy.setGpsLocation(null);
	}
	
	public <Tsurvey extends StationProxy> void fill(Tsurvey survey, Station station){
		survey.setModel(station);
		survey.setLocation(station.getLocation());
		survey.setObservations(session.getObservationDao()
				.queryBuilder()
				.where(com.amecfw.sage.persistence.ObservationDao.Properties.StationID.eq(station.getId()))
				.list());
		survey.setMeasurements(session.getMeasurementDao()
				.queryBuilder()
				.where(com.amecfw.sage.persistence.MeasurementDao.Properties.StationID.eq(station.getId()))
				.list());
	}
	
	public <TviewModel, Tsurvey extends StationProxy> void syncFromSurvey(Tsurvey survey, TviewModel viewModel){
		DescriptorServices.getByFieldDescriptor(viewModel, survey.getModel());
		ObservationService observationService = new ObservationService(session);
		observationService.updateAnnotations(viewModel, survey.getObservations());
	}
	
	public <TviewModel, Tsurvey extends StationProxy> void syncFromViewModel(TviewModel viewModel, Tsurvey survey){
		DescriptorServices.setByFieldDescriptor(viewModel, survey.getModel());
		ObservationService observationService = new ObservationService(session);
		survey.setObservations(observationService.fromAnnotations(viewModel));
		MeasurementService measurementService = new MeasurementService(session);
		survey.setMeasurements(measurementService.fromAnnotations(viewModel));
	}
	
	public <TviewModel, Tsurvey extends StationProxy> void buildSurvey(TviewModel viewModel, Tsurvey survey){
		Station station = new Station();
		DescriptorServices.setByFieldDescriptor(viewModel, station);
		survey.setModel(station);
		if(station.getRowGuid()==null) station.setRowGuid();
		ObservationService observationService = new ObservationService(session);
		survey.setObservations(observationService.fromAnnotations(viewModel));
		MeasurementService measurementService = new MeasurementService(session);
		survey.setMeasurements(measurementService.fromAnnotations(viewModel));
	}
	
}
