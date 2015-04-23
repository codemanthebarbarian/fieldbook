package com.amecfw.sage.model.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.util.Log;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.ObservationDescriptor;
import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.Observation;
import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.ObservationType;
import com.amecfw.sage.model.Owner;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.ObservationDao;
import com.amecfw.sage.persistence.ObservationDao.Properties;
import com.amecfw.sage.persistence.ObservationTypeDao;
import com.amecfw.sage.persistence.ObservationGroupDao;
import com.amecfw.sage.persistence.GroupObservationDao;
import com.amecfw.sage.persistence.OwnerDao;
import com.amecfw.sage.util.Validation;

public class ObservationService {
	
	private DaoSession session;
	
	public ObservationService(DaoSession session){
		this.session = session;
	}
	
	//OBSERVATIONS
	
	public List<Observation> findObservations(Station station){
		return session.getObservationDao().queryBuilder().where(ObservationDao.Properties.StationID.eq(station.getId())).list();
	}
	
	public void save(Observation observation){
		if(observation.getRowGuid() == null) observation.setRowGuid();
		session.getObservationDao().insert(observation);
		MetaDataService.save(observation, session.getObservationMetaDao());
	}
	
	public void update(Observation observation){
		session.getObservationDao().update(observation);
		MetaDataService.update(observation, session.getObservationMetaDao());
	}
	
	public void delete(Observation observation){
		new PhotoService(session).delete(observation);
		MetaDataService.delete(observation, session.getObservationMetaDao());
		session.getObservationDao().delete(observation);
	}
	
	/**
	 * Deletes all the observations for the provided station
	 * @param station
	 */
	public void delete(Station station){
		List<Observation> obs = findObservations(station);
		if(obs != null && obs.size() > 0) for (Observation o : obs) delete(o);
	}
	
	/**
	 * Create a list of observations from the source using the annotated fields
	 * @param source
	 * @return
	 */
	public <Tsource> List<Observation> fromAnnotations(Tsource source){
		List<Observation> results = new ArrayList<Observation>();
		Field[] fields = source.getClass().getDeclaredFields();
		for (Field field : fields) {
			ObservationDescriptor descriptor = field.getAnnotation(ObservationDescriptor.class);
			if (descriptor != null){
				field.setAccessible(true);
				ObservationType obsType = getObservationTypeByName(descriptor.observationType());
				if(obsType == null){
					obsType = buildObservationTypeByName(descriptor.observationType());
					addObservationType(obsType);
				}
				Observation observation = new Observation();
				observation.setObservationType(obsType);
				observation.setRowGuid();
				try {
					observation.setObserved(((String)field.get(source)));
					results.add(observation);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					Log.e("ObservationService", e.getMessage());
				}
				//if(!observation.getDateObservered().equals("")) 
			}
		}
		return results;
	}
	
	/**
	 * update the annotated fields in the source from the observations
	 * @param annotatedSource
	 * @param survey
	 */
	public <Tsource> void updateAnnotations(Tsource annotatedSource, List<Observation> observations){
		if(observations == null || observations.size() == 0) return;
		Field[] fields = annotatedSource.getClass().getDeclaredFields();
		ObservationByTypeComparer comparator = new ObservationByTypeComparer();
		for (Field field : fields){
			ObservationDescriptor descriptor = field.getAnnotation(ObservationDescriptor.class);
			if (descriptor != null){
				field.setAccessible(true);
				ObservationType obsType = getObservationTypeByName(descriptor.observationType());
				if(obsType == null){
					obsType = buildObservationTypeByName(descriptor.observationType());
					addObservationType(obsType);
				}
				Observation tmp = new Observation();
				tmp.setObservationType(obsType);
				tmp.setComparator(comparator);
				int index = observations.indexOf(tmp);
				if(index > -1 ){
					String observed = observations.get(index).getObserved();
					try {
						field.set(annotatedSource, observed);
					} catch (IllegalAccessException | IllegalArgumentException e) {
						Log.e("ObservationService", e.getMessage());
					}
				}
			}
		}
	}
	
	public void saveOrUpdate(List<Observation> observations, Station source){
		ObservationTDTComparer comparator = new ObservationTDTComparer();
		ObservationDao dao = session.getObservationDao();
		List<Observation> persisted = dao.queryBuilder().where(Properties.StationID.eq(source.getId())).list();
		for (Observation observation : persisted) {
			observation.setComparator(comparator);
			int index = observations.indexOf(observation);
			if(index > -1){//found matching observation
				Observation trans = observations.get(index);
				//observation.setObservationType(trans.getObservationType());
				observation.setObserved(trans.getObserved());
				observation.resetMetaData();
				trans.setId(observation.getId());//need to set just in case of no metadata then greenDao will check database
				trans.setRowGuid(observation.getRowGuid());
				MetaDataService.MetaSupportExtensionMethods.replace(observation, trans);
				this.update(observation);
			}else{
				this.delete(observation);				
			}
		}
		for (Observation observation : observations) {
			if(observation.getId() == null || observation.getId() < 1){
				observation.setStation(source);
				this.save(observation);
			}
		}
	}
	
	//END OBSERVATION
	
	//OBSERVATION TYPE
	
	public List<ObservationType> getObservationTypes(){
		return session.getObservationTypeDao().loadAll();
	}
	
	public ObservationType tryAddObservationType(String typeName){
		if(typeName == null) return null;
		ObservationType result = getObservationTypeByName(typeName);
		if(result == null){
			result = new ObservationType();
			result.setName(typeName);
		}
		return result;
	}
	
	public ObservationType tryAdd(ObservationType observationType){
		if(observationType == null || observationType.getName() == null) return null;
		ObservationType result = getObservationTypeByName(observationType.getName());
		if(result == null){
			save(observationType);
			result = observationType;
		}
		return result;
	}
	
	public void save(ObservationType observationType){
		if(observationType.getRowGuid() == null) observationType.setRowGuid();
		session.getObservationTypeDao().insert(observationType);
	}
	
	public ObservationType getByFieldAnnotation(Field field){
		ObservationDescriptor descriptor = field.getAnnotation(ObservationDescriptor.class);
		if(descriptor == null) return null;
		return getObservationTypeByName(descriptor.observationType());
	}
	
	public ObservationType getObservationTypeByName(String name){
		ObservationTypeDao dao = session.getObservationTypeDao();
		ObservationType type = dao.queryBuilder().where(com.amecfw.sage.persistence.ObservationTypeDao.Properties.Name.eq(name)).unique();
		return type;
	}
	
	public long addObservationType(ObservationType obType){
		ObservationTypeDao dao = session.getObservationTypeDao();
		return dao.insert(obType);
	}
	
	public ObservationType buildObservationTypeByName(String name){
		ObservationType obType = new ObservationType();
		obType.setName(name);
		obType.setRowGuid();
		return obType;
	}
	
	//END OBSERVATION TYPE
	
	//OBSERVATION GROUP
	
	public List<ObservationGroup> getAllGroups(){
		return session.getObservationGroupDao().loadAll();
	}
	
	public ObservationGroup getObservationGroup(long id){
		return session.getObservationGroupDao().load(id);
	}
	
	/**
	 * Finds the observation groups for the provided owner (using ownerid). If owner is null, finds all the observation groups
	 * where the owner is null.
	 * @param owner
	 * @return
	 */
	public List<ObservationGroup> findGroups(Owner owner){
		if(owner == null) 
			return session.getObservationGroupDao().queryBuilder().where(ObservationGroupDao.Properties.OwnerID.isNull()).list();
		else
			return session.getObservationGroupDao().queryBuilder().where(ObservationGroupDao.Properties.OwnerID.eq(owner.getId())).list();
	}
	
	public List<ObservationGroup> findGroup(String groupName, Owner owner){
		return session.getObservationGroupDao().queryBuilder().where(com.amecfw.sage.persistence.ObservationGroupDao.Properties.Name.eq(groupName)
				, com.amecfw.sage.persistence.ObservationGroupDao.Properties.OwnerID.eq(owner.getId())).list();
	}
	
	//END OBSERVATION GROUP
	
	//GROUP OBSERVATION
	
	public List<GroupObservation> findByGroup(String groupName, String owner){
//		ObservationGroup og = session.getObservationGroupDao().queryBuilder().where(ObservationGroupDao.Properties.Name.eq(groupName)).unique();
//		if(og == null) return null;
//		List<GroupObservation> results = session.getGroupObservationDao().queryBuilder().where(GroupObservationDao.Properties.ObservationGroupID.eq(og.getId())).list();
		List<GroupObservation> results = session.getGroupObservationDao()
				.queryRaw("inner join " + ObservationGroupDao.TABLENAME + " OG"
						+ " on T." + GroupObservationDao.Properties.ObservationGroupID.columnName + " = OG._id"
						+ " inner join " + OwnerDao.TABLENAME + " OWN"
						+ " on OG." + ObservationGroupDao.Properties.OwnerID.columnName + " = OWN._id " 
						+ " where OG." + ObservationGroupDao.Properties.Name.columnName + " = ? and OWN." 
						+ OwnerDao.Properties.Name.columnName + " = ?"  , groupName, owner);
		return results;
	}

	/**
	 * Finds the GroupObservation using the the ObservationDescriptor observationType name from the provided field.
	 * If a GroupObservation with a observation type of the name is not found, null is returned.
	 * @param annotatedField
	 * @param group
	 * @return the group observation or null if no match is found
	 */
	public static GroupObservation get(Field annotatedField, ObservationGroup group){
		ObservationDescriptor descriptor = annotatedField.getAnnotation(ObservationDescriptor.class);
		if(descriptor == null) return null;
		GroupObservation temp = new GroupObservation();
		temp.setObservationType(new ObservationType());
		temp.getObservationType().setName(descriptor.observationType());
		temp.setComparator(new GroupObservationTypeNameComparer());
		int index = group.getGroupObservations().indexOf(temp);
		if(index < 0) return null;
		return group.getGroupObservations().get(index);
	}
	
	public static String getAllowableValues(String[] allowableValues, char delimiter , boolean trim){
		if(allowableValues == null || allowableValues.length < 1) return null;
		StringBuilder sb = new StringBuilder();
		for(String s : allowableValues){
			if(! Validation.isNullOrEmpty(s)) {
				if(sb.length() > 0) sb.append(delimiter);
				sb.append(s);
			}
		}
		return sb.toString();
	}
	
	public static String getAllowableValues(List<String> allowableValues, char delimiter, boolean trim){
		return getAllowableValues(allowableValues.toArray(new String[allowableValues.size()]), delimiter, trim);
	}
	
	/**
	 * 
	 * @param go
	 * @param regExSplitExpression
	 * @param trim
	 * @return
	 */
	public static String[] parseAllowableValues(GroupObservation go, String regExSplitExpression, boolean trim){
		String av = go.getAllowableValues();
		if(av == null || av == new String()) return null;
		String[] allowableValues = av.split(regExSplitExpression);
		if(trim){
			for (int i = 0; i < allowableValues.length; i++) {
				allowableValues[i] = allowableValues[i].trim();
			}
		}
		return allowableValues;
	}
	
	//END GROUP OBSERVATION
	
	/**
	 * gets the observation type name using the descriptor from the provided class.
	 * @param source
	 * @param fieldName
	 * @return
	 */
	public static String getGroupNameFromDescriptor(Class<?> source, String fieldName){
		try {			
			Field field = source.getDeclaredField(fieldName);
			ObservationDescriptor descriptor = field.getAnnotation(ObservationDescriptor.class);
			if(descriptor == null) return null;
			return descriptor.observationType();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static class ObservationTypeComparer implements EqualityComparator{
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof ObservationType)) return false;
			if(!(objB instanceof ObservationType)) return false;
			ObservationType a = (ObservationType) objA;
			ObservationType b = (ObservationType) objB;
			return a.getName().equalsIgnoreCase(b.getName());
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof ObservationType)) return obj.hashCode();
			return ((ObservationType)obj).getName().toUpperCase().hashCode();
		}
	}
	
	public static class ObservationTypeLikeComparer implements EqualityComparator{
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof ObservationType)) return false;
			if(!(objB instanceof ObservationType)) return false;
			ObservationType a = (ObservationType) objA;
			ObservationType b = (ObservationType) objB;
			return a.getName().toUpperCase().contains(b.getName().toUpperCase());
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof ObservationType)) return obj.hashCode();
			return ((ObservationType)obj).getName().toUpperCase().hashCode();
		}
	}
	
	public static class GroupObservationTypeNameComparer implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof GroupObservation)) return false;
			if(!(objB instanceof GroupObservation)) return false;
			GroupObservation a = (GroupObservation) objA;
			GroupObservation b = (GroupObservation) objB;
			return a.getObservationType().getName().equalsIgnoreCase(b.getObservationType().getName());
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof GroupObservation)) return obj.hashCode();
			return ((GroupObservation)obj).getObservationType().getName().toUpperCase().hashCode();
		}
		
	}
	
	public static class ObservationByTypeComparer implements EqualityComparator{
		
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Observation)) return false;
			if(!(objB instanceof Observation)) return false;
			Observation a = (Observation) objA;
			Observation b = (Observation) objB;			
			return a.getObservationTypeID() == b.getObservationTypeID();
		}

		@Override
		public int getHashCode(Object obj) {
			// TODO Auto-generated method stub
			if(obj == null) return 0;
			if(!(obj instanceof Observation)) return obj.hashCode();
			return ((Observation)obj).getObservationType().hashCode();
		}	
	}
	
	/**
	 * A comparer to check if two observations are equal using
	 * TypeID, Date and Time
	 */
	public static class ObservationTDTComparer implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Observation)) return false;
			if(!(objB instanceof Observation)) return false;
			Observation a = (Observation) objA;
			Observation b = (Observation) objB;			
			if(a.getObservationTypeID() != b.getObservationTypeID()) return false;
			if(a.getDateObservered() != b.getDateObservered()) return false;
			if(a.getTimeObserved() != b.getTimeObserved()) return false;
			return true;
		}

		@Override
		public int getHashCode(Object obj) {
			// TODO Auto-generated method stub
			if(obj == null) return 0;
			if(!(obj instanceof Observation)) return obj.hashCode();
			return ((Observation)obj).getObservationType().hashCode();
		}		
	}
	
}
