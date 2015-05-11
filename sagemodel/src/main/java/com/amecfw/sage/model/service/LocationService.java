package com.amecfw.sage.model.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.EqualityComparatorOf;
import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.R;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.persistence.CoordinateDao;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.LocationDao;
import com.amecfw.sage.proxy.LocationProxy;
import com.amecfw.sage.util.CollectionOperations;

import de.greenrobot.dao.query.WhereCondition;

public class LocationService {

	private DaoSession session;
	
	public static final String FEATURE_TYPE_POINT = "POINT";
	public static final String FEATURE_TYPE_LINE = "LINE";
	public static final String FEATURE_TYPE_AREA = "AREA";
	
	public LocationService(DaoSession session){
		this.session = session;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Location Methods
	
	/**
	 * returns all the locations for the provided site or null if no locations exist for the site. The site must
	 * exist in the database (have an id > 0) or null is returned.
	 * @param site the site to find the location for
	 * @return the list of locations for the site or null if none exist or the site id is < 1
	 */
	public List<Location> findLocation(Site site){
		if(site.getId() < 1) return null;
		LocationDao dao = session.getLocationDao();
		List<Location> results = dao.queryBuilder().where(LocationDao.Properties.SiteID.eq(site.getId())).list();
		if(results != null && (! results.isEmpty())) return results;
		return null;
	}
	
	public List<Location> findByName(String name){
		return session.getLocationDao().queryBuilder().where(LocationDao.Properties.Name.eq(name)).list();
	}
	
	public void save(Location location){
		location.setId(null);
		if(location.getRowGuid() == null) location.setRowGuid();
		session.getLocationDao().insert(location);
		MetaDataService.save(location, session.getLocationMetaDao());
	}
	
	public void update(Location location){
		session.getLocationDao().update(location);
		MetaDataService.save(location, session.getLocationMetaDao());
	}
	
	public void delete(Location location){
		new PhotoService(session).delete(location);
		MetaDataService.delete(location, session.getLocationMetaDao());
		deleteCoordinates(location);
		session.getLocationDao().delete(location);
	}
	
	/**
	 * Sets the locate information in the location from the provided coordinate if the coordinate is null, the
	 * locate information in the location is cleared (elevation, latitude, longitude set to null)
	 * @param location
	 * @param coordinate
	 */
	public static void setFromCoordinate(Location location, Coordinate coordinate){
		if(coordinate == null){
			location.setElevation(null);
			location.setLatitude(null);
			location.setLongitude(null);
			return;
		}
		location.setElevation(coordinate.getElevation() == null ? null : Double.toString(coordinate.getElevation()));
		location.setLatitude(coordinate.getLatitude() == null ? null : Double.toString(coordinate.getLatitude()));
		location.setLongitude(coordinate.getLongitude() == null ? null : Double.toString(coordinate.getLongitude()));
	}
	
	// END Location Methods
	///////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////
	// LocationProxy Methods
	
	public void saveOrUpdateProxy(LocationProxy proxy){
		if(proxy == null || proxy.getModel() == null) return;
		if(proxy.getModel().getId() == null || proxy.getModel().getId() < 1) save(proxy);
		else update(proxy);
	}
	
	public void save(LocationProxy proxy){
		proxy.getModel().setSite(proxy.getSite());
		save(proxy.getModel());		
		insertCoordinates(proxy.getCoordinates(), proxy.getModel(), proxy.getFeatureType());
	}
	
	public void update(LocationProxy proxy){
		proxy.getModel().setSite(proxy.getSite());
		update(proxy.getModel());
		saveOrUpdateCoordinates(proxy.getCoordinates(), proxy.getModel(), proxy.getFeatureType());
	}
	
	public LocationProxy getProxy(Location location){
		if(location.getId() == null || location.getId() < 1) return null;
		LocationProxy proxy = new LocationProxy();
		proxy.setModel(location);
		List<Coordinate> coordinates = findCoordinates(location);
		if(coordinates != null && coordinates.size() > 0){
			proxy.setFeatureType(coordinates.get(0).getFeatureType());
			proxy.setCoordinates(coordinates);
			proxy.setLocations(convertCoordinates(coordinates));
		}
		return proxy;
	}
	
	public void delete(LocationProxy proxy){
		if(proxy == null) return;
		delete(proxy.getModel());
	}
	
	public static LocationProxy createProxyFromLocations(List<android.location.Location> coordinates, Location location, String featureType){
		LocationProxy proxy = new LocationProxy();
		proxy.setFeatureType(featureType);
		if(coordinates != null){
			List<Coordinate> coords = convertLocations(coordinates.toArray(new android.location.Location[coordinates.size()]), proxy.getFeatureType());
			proxy.setCoordinates(coords);
			proxy.setLocations(coordinates);
		}
		proxy.setModel(location);
		return proxy;
	}
	
	public static LocationProxy createPointFromLocation(android.location.Location coordinate, Location location){
		return createPointFromLocations(Arrays.asList(new android.location.Location[]{coordinate}), location);
	}
	
	public static LocationProxy createPointFromLocations(List<android.location.Location> coordinates, Location location){
		return createProxyFromLocations(coordinates, location, FEATURE_TYPE_POINT);
	}
	
	public static LocationProxy createLineFromLocations(List<android.location.Location> coordinates, Location location){
		return createProxyFromLocations(coordinates, location, FEATURE_TYPE_LINE);
	}
	
	public static LocationProxy createAreaFromLocations(List<android.location.Location> coordinates, Location location){
		return createProxyFromLocations(coordinates, location, FEATURE_TYPE_AREA);
	}
	
	// End LocationProxy Methods
	///////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////
	// Coordinate methods
	
	/**
	 * returns all associated coordinates for the provided location or null if no coordinates exist for the location. The location
	 * must exist in the database ( have an id > 0) or null is returned.
	 * @param location
	 * @return the list of coordinates for the location or null if none exist or the location id is < 1
	 */
	public List<Coordinate> findCoordinates(Location location){
		if(location.getId() < 1) return null;
		CoordinateDao dao = session.getCoordinateDao();
		List<Coordinate> results = dao.queryBuilder().where(CoordinateDao.Properties.LocationID.eq(location.getId()))
													 .orderAsc(CoordinateDao.Properties.FeatureType, CoordinateDao.Properties.Id)
													 .list();
		if(results != null && (! results.isEmpty())) return results;
		return null;
	}
	
	/**
	 * returns all associated coordinate for the provided loaction by feature type or null if no coordinate exists for that
	 * type and location.
	 * @param location
	 * @param featureType
	 * @return
	 */
	public List<Coordinate> findCoordinates(Location location, String featureType){
		if(location.getId() < 1) return null;
		CoordinateDao dao = session.getCoordinateDao();
		List<Coordinate> results = dao.queryBuilder().where(CoordinateDao.Properties.LocationID.eq(location.getId())
															, CoordinateDao.Properties.FeatureType.eq(featureType))
													 .orderAsc(CoordinateDao.Properties.Id)
													 .list();
		if(results != null && (! results.isEmpty())) return results;
		return null;
	}
	
	/**
	 * @param coordinates
	 * @param location
	 * @param featureType if not null
	 */
	public void saveOrUpdateCoordinates(List<Coordinate> coordinates, Location location, String featureType){
		if(coordinates == null || coordinates.size() ==0){ 
			deleteAllCoordinates(location); 
			return;
		}
		List<Coordinate> persisted = findCoordinates(location);
		if(persisted == null || persisted.size() == 0){
			insertCoordinates(coordinates, location, featureType);
			return;
		}
		for(Coordinate coord : coordinates) if(coord.getFeatureType() == null) coord.setFeatureType(featureType);
		CollectionOperations.RowGuidComparator comparator = new CollectionOperations.RowGuidComparator();
		List<Coordinate> forDelete = CollectionOperations.except(persisted, coordinates, comparator);
		List<Coordinate> forAdd = CollectionOperations.except(coordinates, persisted, comparator);
		List<Coordinate> forUpdate = CollectionOperations.except(persisted, forDelete, comparator);
		deleteCoordinates(forDelete);
		insertCoordinates(forAdd, location, featureType);
		updateCoordinates(forUpdate, coordinates, comparator);
	}
	
	public void updateCoordinates(List<Coordinate> persisted, List<Coordinate> transients, EqualityComparator comparator){
		if(persisted == null || persisted.size() == 0) return;
		if(transients == null || transients.size() == 0) return;
		CoordinateDao dao = session.getCoordinateDao();
		for(Coordinate per: persisted){
			Coordinate trans = CollectionOperations.find(transients, per, comparator);
			if(trans != null){
				copyCoordinate(per, trans);
				dao.update(per);
			}
		}
	}
	
	public void deleteCoordinates(Location location){
		session.getCoordinateDao().queryBuilder()
			.where(CoordinateDao.Properties.LocationID.eq(location.getId()))
			.buildDelete()
			.executeDeleteWithoutDetachingEntities();
	}
	
	public void deleteCoordinates(List<Coordinate> coordinates){
		if(coordinates == null || coordinates.size() == 0) return;
		CoordinateDao dao = session.getCoordinateDao();
		for(Coordinate coord : coordinates) dao.delete(coord);
	}
	
	public void updateCoordinates(List<Coordinate> coordinates){
		if(coordinates == null || coordinates.size() == 0) return;
		CoordinateDao dao = session.getCoordinateDao();
		for(Coordinate coord : coordinates) dao.update(coord);
	}
	
	public void copyCoordinate(Coordinate persisted, Coordinate _transient){
		_transient.setId(persisted.getId());
		_transient.setRowGuid(persisted.getRowGuid());
		persisted.setBearing(_transient.getBearing());
		persisted.setAccuracy(_transient.getAccuracy());
		persisted.setElevation(_transient.getElevation());
		persisted.setFeatureType(_transient.getFeatureType());
		persisted.setLatitude(_transient.getLatitude());
		persisted.setLongitude(_transient.getLongitude());
		persisted.setSpeed(_transient.getSpeed());
		persisted.setTime(_transient.getTime());
		persisted.setLocation(_transient.getLocation());
	}
	
	/**
	 * @param coordinates
	 * @param location
	 * @param featureType if not null, will set the feature type for all coordinates in the list to the
	 * provided type
	 */
	public void insertCoordinates(List<Coordinate> coordinates, Location location, String featureType){
		if(coordinates == null || coordinates.size() == 0) return;
		for(Coordinate coordinate : coordinates){
			coordinate.setLocation(location);
			if(featureType == null) coordinate.setFeatureType(featureType);
			save(coordinate);
		}
	}
	
	public void save(Coordinate coordinate){
		coordinate.setId(null);
		if(coordinate.getRowGuid() == null) coordinate.setRowGuid();
		session.getCoordinateDao().insert(coordinate);
	}
	
	public void deleteAllCoordinates(Location location){
		session.getCoordinateDao().queryBuilder()
			.where(new WhereCondition.StringCondition(String.format(" %s = ?", CoordinateDao.Properties.LocationID.columnName), location.getId()))
			.buildDelete()
			.executeDeleteWithoutDetachingEntities();
	}
	
	// END Coordinate methods
	///////////////////////////////////////////////////////////////////////////

	/**
	 * formats a location based on Lat: %f Long: %f Elev: %f Acc: %f
	 * @param location the location
	 * @return the fomatted text of null if the location is null
	 */
	public static String formatLocationText(android.location.Location location){
		String result = null;
		if(location != null) {
			String format = SageApplication.getInstance().getContext().getString(R.string.gps_format);
			result = String.format(format
					, location.getLatitude()
					, location.getLongitude()
					, location.getAltitude()
					, location.getAccuracy());
		}
		return result;
	}
	
	/**
	 * Converts the provided coordinate to an android.location.Location
	 * @param coordinate
	 * @return the coordinate converted to a location or null if the coordinate is null
	 */
	public static android.location.Location convert(Coordinate coordinate){
		if(coordinate == null) return null;
		android.location.Location location = new android.location.Location(LocationService.class.getName());
		if(coordinate.getLatitude() != null) location.setLatitude(coordinate.getLatitude());
		if(coordinate.getLongitude() != null) location.setLongitude(coordinate.getLongitude());
		if(coordinate.getElevation() != null) location.setAltitude(coordinate.getElevation());
		if(coordinate.getTime() != null) location.setTime(coordinate.getTime());
		if(coordinate.getAccuracy() != null) location.setAccuracy(coordinate.getAccuracy());
		return location;		
	}
	
	public static List<android.location.Location> convertCoordinates(List<Coordinate> coordinates){
		if(coordinates == null) return null;
		List<android.location.Location> locations = new ArrayList<android.location.Location>(coordinates.size());
		for (Coordinate coord : coordinates){
			android.location.Location temp = convert(coord);
			if(temp != null) locations.add(temp);
		}
		return locations;
	}
	
	/**
	 * Converts the provided android.location.Location to a coordinate
	 * @param location
	 * @return the location converted to a coordinate or null if the location is null
	 */
	public static Coordinate convert(android.location.Location location){
		Coordinate coord = new Coordinate();
		coord.setAccuracy(location.getAccuracy());
		coord.setBearing(location.getBearing());
		coord.setElevation(location.getAltitude());
		coord.setLatitude(location.getLatitude());
		coord.setLongitude(location.getLongitude());
		coord.setSpeed(location.getSpeed());
		coord.setTime(location.getTime());
		coord.setRowGuid();
		return coord;
	}
	
	public static List<Coordinate> convertLocations(Collection<android.location.Location> locations, String featureType){
		if(locations == null) return null;
		List<Coordinate> results = new ArrayList<Coordinate>(locations.size());
		for(android.location.Location location: locations){
			Coordinate temp = convert(location);
			if(temp != null){
				temp.setFeatureType(featureType);
				results.add(temp);
			}
		}
		return results;
	}
	
	public static List<Coordinate> convertLocations(android.location.Location[] locations, String featureType){
		if(locations == null) return null;
		List<Coordinate> results = new ArrayList<Coordinate>(locations.length);
		for(android.location.Location location: locations){
			Coordinate temp = convert(location);
			if(temp != null){
				temp.setFeatureType(featureType);
				results.add(temp);
			}
		}
		return results;
	}
	
	/**
	 * converted the provided array of android.location.Location to an array of coordinates with the provided feature type
	 * @param locations
	 * @param featureType
	 * @return the converted locations or null is the array is null
	 */
	public static Coordinate[] build(android.location.Location[] locations, String featureType){
		if(locations == null) return null;
		Coordinate[] coords = new Coordinate[locations.length];
		for(int i = 0 ; i < locations.length ; i++){
			Coordinate temp = convert(locations[i]);
			if(temp != null) temp.setFeatureType(featureType);
			coords[i] = temp;
		}
		return coords;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Comparators
		
	public static class CoordinateTypeComparator implements EqualityComparator {
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Coordinate)) return false;
			if(!(objB instanceof Coordinate)) return false;
			Coordinate a = (Coordinate) objA;
			Coordinate b = (Coordinate) objB;			
			return a.getFeatureType().equalsIgnoreCase(b.getFeatureType());
		}
		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Coordinate)) return obj.hashCode();
			String featureType = ((Coordinate)obj).getFeatureType();
			return featureType == null ? 0 : featureType.toUpperCase(Locale.getDefault()).hashCode();
		}		
	}
	
	public static class CoordinateXYZComparator implements EqualityComparator {
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Coordinate)) return false;
			if(!(objB instanceof Coordinate)) return false;
			Coordinate a = (Coordinate) objA;
			Coordinate b = (Coordinate) objB;
			if(! a.getLatitude().equals(b.getLatitude())) return false;
			if(! a.getLongitude().equals(b.getLongitude())) return false;
			return a.getElevation().equals(b.getElevation());
		}
		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Coordinate)) return obj.hashCode();
			Coordinate o = (Coordinate) obj;
			int result = o.getLatitude() == null ? 1 : o.getLatitude().hashCode();
			result = result * (o.getLongitude() == null ? 1 : o.getLongitude().hashCode());
			result = result * (o.getElevation() == null ? 1 : o.getElevation().hashCode());
			return result;
		}		
	}
	
	public static class AndroidLocationXYZComparator implements EqualityComparatorOf<android.location.Location> {
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof android.location.Location)) return false;
			if(!(objB instanceof android.location.Location)) return false;
			android.location.Location a = (android.location.Location) objA;
			android.location.Location b = (android.location.Location) objB;
			return equals(a, b);
		}
		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof android.location.Location)) return obj.hashCode();
			android.location.Location o = (android.location.Location) obj;
			return getHash(o);
		}
		@Override
		public boolean equals(android.location.Location a,
				android.location.Location b) {
			if(a.getLatitude() != b.getLatitude()) return false;
			if(a.getLongitude() != b.getLongitude()) return false;
			return a.getAltitude() == b.getAltitude();
		}
		@Override
		public int getHash(android.location.Location obj) {
			int result = (obj.getLatitude() == 0 ? 1 : Double.valueOf(obj.getLatitude()).hashCode());
			result = result * (obj.getLongitude() == 0 ? 1 : Double.valueOf(obj.getLongitude()).hashCode());
			result = result * (obj.getAltitude() == 0 ? 1 : Double.valueOf(obj.getAltitude()).hashCode());
			return result;
		}		
	}
	
	// END Comparators
	///////////////////////////////////////////////////////////////////////////
}
