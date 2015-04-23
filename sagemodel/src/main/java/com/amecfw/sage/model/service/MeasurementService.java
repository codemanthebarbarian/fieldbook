package com.amecfw.sage.model.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.MeasurementDescriptor;
import com.amecfw.sage.model.Measurement;
import com.amecfw.sage.model.MeasurementMeta;
import com.amecfw.sage.model.Parameter;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.MeasurementDao;
import com.amecfw.sage.persistence.ParameterDao;

public class MeasurementService {
	
	DaoSession session;
	
	public MeasurementService(DaoSession session){
		this.session = session;
	}
	
	public void save(Measurement measurement){
		session.getMeasurementDao().insert(measurement);
		if(measurement.getMetaData() != null){
			for (MeasurementMeta metaElement : measurement.getMetaData()) {
				metaElement.setMeasurement(measurement);
				session.getMeasurementMetaDao().insert(metaElement);
			}
		}
	}
	
	public void update(Measurement measurement){
		session.getMeasurementDao().update(measurement);
		MetaDataService.update(measurement, session.getMeasurementMetaDao());
	}
	
	public void delete(Measurement measurement){
		new PhotoService(session).delete(measurement);
		MetaDataService.delete(measurement, session.getMeasurementMetaDao());
		session.getMeasurementDao().delete(measurement);
	}
	
	public void saveOrUpdate(List<Measurement> measurements, Station station){
		if(measurements == null || measurements.size() == 0) {
			delete(station);
			return;
		}
		MeasurementPMUComparer comparator = new MeasurementPMUComparer();
		MeasurementDao dao = session.getMeasurementDao();
		List<Measurement> persisted = dao.queryBuilder().where(MeasurementDao.Properties.StationID.eq(station.getId())).list();
		if(persisted != null && persisted.size() > 0){
			for(Measurement measurement : persisted){
				measurement.setComparator(comparator);
				int index = measurements.indexOf(measurement);
				if(index > -1){
					Measurement trans = measurements.get(index);
					measurement.resetMetaData();
					measurement.setDateMeasured(trans.getDateMeasured());
					measurement.setTimeMeasured(trans.getTimeMeasured());
					measurement.setMeasurementText(trans.getMeasurementText());
					trans.setId(measurement.getId());
					trans.setRowGuid(measurement.getRowGuid());
					update(measurement);
				} else{
					delete(measurement);
				}
			}
		}
		for(Measurement measurement : measurements){
			if(measurement.getId() == null || measurement.getId() <1){
				measurement.setStation(station);
				save(measurement);
			}
		}
	}
	
	/**
	 * deletes all the measurements for the provided station
	 * @param station
	 */
	public void delete(Station station){
		List<Measurement> mes = find(station);
		if(mes != null && mes.size() > 0) for(Measurement m : mes) delete(m);
	}
	
	public <Tsource> List<Measurement> fromAnnotations(Tsource source){
		List<Measurement> results = new ArrayList<Measurement>();
		Field[] fields = source.getClass().getDeclaredFields();
		for (Field field : fields) {
			MeasurementDescriptor descriptor = field.getAnnotation(MeasurementDescriptor.class);
			if (descriptor != null){
				field.setAccessible(true);
				Parameter parameter = getByName(descriptor.parameterName());
				if(parameter == null){
					parameter = buildParameterByName(descriptor.parameterName());
					addParameter(parameter);
				}
				Measurement measurement = new Measurement();
				measurement.setParameter(parameter);
				measurement.setMatrix(descriptor.matrix());
				measurement.setUnit(descriptor.unit());
				try {
					measurement.setMeasurementText(((String)field.get(source)));
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//if(!observation.getDateObservered().equals("")) 
					results.add(measurement);
			}
		}
		return results;
	}
	
	public List<Measurement> find(Station station){
		return session.getMeasurementDao().queryBuilder().where(MeasurementDao.Properties.StationID.eq(station.getId())).list();
	}
	
	public Parameter getByName(String name){
		ParameterDao dao = session.getParameterDao();
		return dao.queryBuilder().where(com.amecfw.sage.persistence.ParameterDao.Properties.Name.eq(name)).unique();
	}
	
	public Parameter buildParameterByName(String name){
		Parameter result = new Parameter();
		result.setName(name);
		result.setRowGuid();
		return result;
	}
	
	public long addParameter(Parameter parameter){
		ParameterDao dao = session.getParameterDao();
		return dao.insert(parameter);
	}
	
	/**
	 * A measurement comparer based on parameter, matrix, and unit
	 *
	 */
	public static class MeasurementPMUComparer implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Measurement)) return false;
			if(!(objB instanceof Measurement)) return false;
			Measurement a = (Measurement) objA;
			Measurement b = (Measurement) objB;
			if(a.getParameterID() != b.getParameterID()) return false;
			if(!a.getMatrix().equalsIgnoreCase(b.getMatrix())) return false;
			if(!a.getUnit().equalsIgnoreCase(b.getUnit())) return false;
			return true;
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Measurement)) return obj.hashCode();
			Measurement m = (Measurement) obj;
			return m.getParameter().hashCode() * m.getMatrix().hashCode() * m.getUnit().hashCode() * 13;
		}
		
	}

}
