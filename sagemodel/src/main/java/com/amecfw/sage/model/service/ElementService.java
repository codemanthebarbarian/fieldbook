package com.amecfw.sage.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.Element;
import com.amecfw.sage.model.ElementGroup;
import com.amecfw.sage.model.EqualityComparatorOf;
import com.amecfw.sage.model.GroupElement;
import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.StationElement;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.ElementDao;
import com.amecfw.sage.persistence.ElementGroupDao;
import com.amecfw.sage.persistence.GroupElementDao;
import com.amecfw.sage.persistence.OwnerDao;
import com.amecfw.sage.persistence.StationElementDao;
import com.amecfw.sage.proxy.StationElementProxy;
import com.amecfw.sage.util.CollectionOperations;

public class ElementService {
	
	DaoSession session;
	public static final String GROUP_NAME_ALL = "ALL";
	
	public ElementService(DaoSession session){
		this.session = session;
	}
	
	/**
	 * returns the first occurrence of a group matching a provided group name and owner name.
	 * If no match is found, returns null.
	 * @param groupName
	 * @param ownerName
	 * @return the first occurrence of a match or null
	 */
	public ElementGroup findByNameOwner(String groupName, String ownerName){
		if(ownerName == null) return findGroupByNameNullOwner(groupName);
		ElementGroupDao groupDao = session.getElementGroupDao();
		List<ElementGroup> groups = groupDao.queryRaw("inner join " 
				+ OwnerDao.TABLENAME + " O" 
				+ " on T." + ElementGroupDao.Properties.OwnerID.columnName 
				+ " = O._id where T." + ElementGroupDao.Properties.Name 
				+ " = ? and O." + OwnerDao.Properties.Name + " = ?", groupName, ownerName);
		if(groups == null || groups.size() < 1)  return null;
		return groups.get(0);
	}
	
	public List<ElementGroup> findByOwner(String ownerName){
		if(ownerName == null) return getGroupsNullOwner();
		ElementGroupDao groupDao = session.getElementGroupDao();
		List<ElementGroup> groups = groupDao.queryRaw("inner join " 
				+ OwnerDao.TABLENAME + " O" 
				+ " on T." + ElementGroupDao.Properties.OwnerID.columnName 
				+ " = O._id where O." + OwnerDao.Properties.Name + " = ? ORDER BY " + ElementGroupDao.Properties.Name.columnName, ownerName);
		if(groups == null || groups.size() < 1)  return null;
		return groups;
	}
	
	/** 
	 * finds the groups 
	 * @param groupName
	 * @return
	 */
	public ElementGroup findGroupByNameNullOwner(String groupName){
		ElementGroupDao groupDao = session.getElementGroupDao();
		List<ElementGroup> groups = groupDao.queryBuilder().where(ElementGroupDao.Properties.Name.eq(groupName)
				, ElementGroupDao.Properties.OwnerID.isNull()).list();
		if(groups == null || groups.size() < 1)  return null;
		return groups.get(0);
	}
	
	/** Gets the default group containing all the group Elements. No owner is associated with this group. 
	 * If the group doesn't exist, it is created. If the elements in the group are out of sync, they are 
	 * updated to contain all the elements in the database */
	public ElementGroup getAllGroup(){
		ElementGroup all = findGroupByNameNullOwner(GROUP_NAME_ALL);
		if(all == null) return buildAllGroup();
		Long countElements = session.getElementDao().count();
		if(all.getGroupElements().size() != countElements) updateAllGroup(all);
		return all;
	}
	
	public List<ElementGroup> getGroupsNullOwner(){
		ElementGroupDao groupDao = session.getElementGroupDao();		
		return groupDao.queryBuilder().where(ElementGroupDao.Properties.OwnerID.isNull())
				.orderAsc(ElementGroupDao.Properties.Name).list();
	}
	
	public void update(ElementGroup group){
		session.getElementGroupDao().update(group);
		if(group.getMetaData() != null){
			MetaDataService.update(group, session.getElementGroupMetaDao());
		}
		updateGroupElements(group);
	}
	
	/**
	 * Saves a new element group to the database, assumes there is no existing group. This could possibly result in 
	 * duplicate groups being created.
	 * see OwnerService
	 * @param group
	 */
	public void save(ElementGroup group){
		if(group.getRowGuid() == null || group.getRowGuid() == new String()) group.setRowGuid();
		group.setId(null);
		session.getElementGroupDao().insert(group);
		MetaDataService.save(group, session.getElementGroupMetaDao());
		if(group.getGroupElements() != null && group.getGroupElements().size() > 0){
			for (GroupElement ge : group.getGroupElements()) {
				ge.setElementGroup(group);
				ge.setRowGuid();
				session.getGroupElementDao().insert(ge);
			}
		}
	}
	
	private void updateGroupElements(ElementGroup group){
		List<GroupElement> source = group.getGroupElements() == null ? new ArrayList<GroupElement>() : group.getGroupElements();
		GroupElementElementComparor comparator = new GroupElementElementComparor();
		List<GroupElement> persisted = session.getGroupElementDao().queryBuilder().where(GroupElementDao.Properties.ElementGroupID.eq(group.getId())).list();
		for(GroupElement p : persisted){
			p.setComparator(comparator);
			int index = source.indexOf(p);
			if(index > -1){
				GroupElement trans = source.get(index);
				p.setFlags(trans.getFlags());
				session.getGroupElementDao().update(p);
				source.set(index, p);
			}else{
				session.getGroupElementDao().delete(p);
			}
		}
		for(GroupElement e : group.getGroupElements()){
			if(e.getId() == null || e.getId() < 1){
				e.setElementGroup(group);
				if(e.getRowGuid() == null) e.setRowGuid();
				session.getGroupElementDao().insert(e);
			}
		}
	}
	
	public List<GroupElement> findGroupElementsByGroup(ElementGroup group){
		if(group == null) return null;
		GroupElementDao dao = session.getGroupElementDao();
		return dao.queryBuilder().where(GroupElementDao.Properties.ElementGroupID.eq(group.getId())).list();
	}
	
	public List<Element> findElementsByGroup(ElementGroup group){
		if(group == null) return null;
		ElementDao dao = session.getElementDao();
		List<Element> elements = dao.queryRaw("inner join " 
				+ GroupElementDao.TABLENAME + " GE" 
				+ " on T." + ElementDao.Properties.Id.columnName 
				+ " = GE." + GroupElementDao.Properties.ElementID.columnName
				+ " where GE." + GroupElementDao.Properties.ElementGroupID.columnName + " = ?", Long.toString(group.getId()));
		return elements;
	}
	
	public void save(Element element){
		element.setId(null);
		session.getElementDao().insert(element);
		MetaDataService.save(element, session.getElementMetaDao());
	}
	
	public List<Element> getElements(){
		return session.getElementDao().loadAll();
	}
	
	public void update(Element element){
		session.getElementDao().update(element);
		if(element.getMetaData() != null){
			MetaDataService.update(element, session.getElementMetaDao());
		}
	}
	
	public Element findByScode(String scode){
		return session.getElementDao().queryBuilder().where(ElementDao.Properties.Scode.eq(scode)).unique();
	}
	
	private ElementGroup buildAllGroup(){
		ElementGroup all = new ElementGroup();
		all.setName(GROUP_NAME_ALL);
		all.setRowGuid();
		List<Element> elements = session.getElementDao().loadAll();
		List<GroupElement> groupElements = new ArrayList<GroupElement>(elements.size());
		for(Element element: elements){
			groupElements.add(build(element, all));
		}
		all.setGroupElements(groupElements);
		save(all);
		return all;
	}
	
	private void updateAllGroup(ElementGroup group){
		List<Element> elements = session.getElementDao().loadAll();
		List<GroupElement> groupElements = new ArrayList<GroupElement>(elements.size());
		for(Element element: elements){
			groupElements.add(build(element, group));
		}
		group.setGroupElements(groupElements);
		updateGroupElements(group);
	}
	
	public GroupElement build(Element element, ElementGroup group){
		if(element == null || group == null) return null;
		GroupElement ge = new GroupElement();
		ge.setElement(element);
		ge.setElementGroup(group);
		return ge;
	}
	
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // STATION ELEMENT ////////////////////////////////////////////////////////////////////////////
	
	public List<StationElement> findStationElements(Station station){
		return session.getStationElementDao().queryBuilder().where(StationElementDao.Properties.StationID.eq(station.getId())).list();
	}
	
	public void update(List<StationElement> elements, Station source){
		StationElementComparer comparator = new StationElementComparer();
		StationElementDao dao = session.getStationElementDao();
		List<StationElement> persited = dao.queryBuilder().where(StationElementDao.Properties.StationID.eq(source.getId())).list();
		for (StationElement stationElement : persited) {
			stationElement.setComparator(comparator);
			int index = elements.indexOf(stationElement);
			if(index > -1){
				//found, just need to update metadata as comparator uses count
				StationElement trans = elements.get(index);
				stationElement.resetMetaData();
				stationElement.setMetaData(trans.getMetaData());
				this.update(stationElement);
			}else{
				this.delete(stationElement);
			}
		}
		for (StationElement stationElement : elements) {
			if(stationElement.getId() == null || stationElement.getId() < 1){
				stationElement.setStation(source);
				this.save(stationElement);
			}
		}
	}
	
	public void save(StationElement stationElement){
		stationElement.setId(null);
		session.getStationElementDao().insert(stationElement);
		MetaDataService.save(stationElement, session.getStationElementMetaDao());
	}
	
	public void update(StationElement stationElement){
		session.getStationElementDao().update(stationElement);
		MetaDataService.update(stationElement, session.getStationElementMetaDao());
	}
	
	public void delete(StationElement stationElement){
		new PhotoService(session).delete(stationElement);
		LocationService ls = new LocationService(session);
		List<Location> locations = ls.findByName(stationElement.getRowGuid());
		if(locations != null) for(Location loc: locations) ls.delete(loc);
 		MetaDataService.delete(stationElement, session.getStationElementMetaDao());
		session.getStationElementDao().delete(stationElement);
	}
	
	/**
	 * Deletes the StationElements for the provided station
	 * @param station
	 */
	public void delete(Station station){
		if(station == null || station.getId() == null) return;
		List<StationElement> elms = findStationElements(station);
		if(elms != null && elms.size() > 0) for(StationElement se : elms) delete(se);
	}
	
	private void updatePersisted(StationElement persisted, StationElement trans){
		persisted.setCount(trans.getCount());
		persisted.setElement(trans.getElement());
		if(trans.hasMetaData()) persisted.setMetaData(trans.getMetaData());
		else persisted.resetMetaData();
	}
	
	// END STATION ELEMENT ////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// StationElementProxy ////////////////////////////////////////////////////////////////////////
	
	public static List<StationElementProxy> convertFromElements(List<Element> elements){
		if(elements == null || elements.size() == 0) return new ArrayList<StationElementProxy>();
		List<StationElementProxy> results = new ArrayList<StationElementProxy>(elements.size());
		for(Element element : elements) results.add(convertFromElement(element));
		return results;
	}
	
	public static StationElementProxy convertFromElement(Element element){
		if(element == null) return null;
		StationElement se = new StationElement();
		se.setElement(element);
		se.setRowGuid();
		se.setId(0L);
		StationElementProxy proxy = new StationElementProxy();
		proxy.setModel(se);
		return proxy;
	}
	
	public static List<StationElementProxy> convertFromStationElements(List<StationElement> stationElements){
		if(stationElements == null || stationElements.size() == 0) return new ArrayList<StationElementProxy>();
		List<StationElementProxy> results = new ArrayList<StationElementProxy>(stationElements.size());
		for(StationElement se: stationElements) results.add(convertFromStationElement(se));
		return results;
	}
	
	public static StationElementProxy convertFromStationElement(StationElement stationElement){
		StationElementProxy proxy = new StationElementProxy();
		proxy.setModel(stationElement);
		return proxy;
	}
	
	public void saveOrUpdate(List<StationElementProxy> proxies, Station station){
		List<StationElementProxy> persisted = convertFromStationElements(findStationElements(station));
		StationElementProxyElementComparator comparator = new StationElementProxyElementComparator();
		List<StationElementProxy> forDelete = CollectionOperations.except(persisted, proxies, comparator);
		List<StationElementProxy> forAdd = CollectionOperations.except(proxies, persisted, comparator);
		List<StationElementProxy> forUpdate = CollectionOperations.except(proxies, forAdd, comparator);
		for(StationElementProxy proxy: forAdd){
			proxy.getModel().setStation(station);
			save(proxy);
		}
		for(StationElementProxy proxy: forUpdate){
			StationElementProxy p = CollectionOperations.first(persisted, proxy, comparator);
			if(p != null){
				updatePersisted(p.getModel(), proxy.getModel());
			}
			proxy.setModel(p.getModel());
			update(proxy);
		}
		for(StationElementProxy proxy: forDelete){
			delete(proxy);
		}
	}
	
	public void save(StationElementProxy proxy){
		save(proxy.getModel());
		new PhotoService(session).saveOrUpdate(proxy.getPhotos(), proxy.getModel());
		new LocationService(session).saveOrUpdateProxy(proxy.getLocation());
	}
	
	public void update(StationElementProxy proxy){
		update(proxy.getModel());
		new PhotoService(session).saveOrUpdate(proxy.getPhotos(), proxy.getModel());
		new LocationService(session).saveOrUpdateProxy(proxy.getLocation());
	}
	
	public void delete(StationElementProxy proxy){
		new PhotoService(session).delete(proxy.getPhotos());
		new LocationService(session).delete(proxy.getLocation());
		delete(proxy.getModel());
	}
	
	// END StationElementProxy ////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class StationElementProxyElementComparator implements EqualityComparatorOf<StationElementProxy>{
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof StationElementProxy) || !(objB instanceof StationElementProxy)) return false;
			return equals((StationElementProxy) objA, (StationElementProxy) objB);
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof StationElementProxy)) return 0;
			return getHash((StationElementProxy)obj);
		}

		@Override
		public boolean equals(StationElementProxy a, StationElementProxy b) {
			if(a.getModel() == null || b.getModel() == null) return false;
			if(a.getModel().getElement() == null || b.getModel().getElement() == null) return false;
			return a.getModel().getElement().getId().equals(b.getModel().getElement().getId());
		}

		@Override
		public int getHash(StationElementProxy obj) {
			if(obj == null) return 0;
			if(obj.getModel() == null || obj.getModel().getElement() == null) return 0;
			return obj.getModel().getElement().getId().hashCode();
		}
		
	}
	
	/**
	 * Compares elements by their RowGuid's
	 */
	public static class ElementRowGuidComparator implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Element)) return false;
			if(!(objB instanceof Element)) return false;
			Element a = (Element) objA;
			Element b = (Element) objB;
			return a.getUUID().equals(b.getUUID());
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Element)) return obj.hashCode();
			return ((Element)obj).getUUID().hashCode();
		}
		
	}
	
	public static class GroupElementElementComparor implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof GroupElement)) return false;
			if(!(objB instanceof GroupElement)) return false;
			GroupElement a = (GroupElement) objA;
			GroupElement b = (GroupElement) objB;
			return a.getElement().getId() == b.getElement().getId();
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof GroupElement)) return obj.hashCode();
			return ((GroupElement) obj).getElement().hashCode();
		}
		
	}

	/**
	 * A comparer to check if two station canopyElements are equal using 
	 * element id and count
	 */
	public static class StationElementComparer implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof StationElement)) return false;
			if(!(objB instanceof StationElement)) return false;
			StationElement a = (StationElement) objA;
			StationElement b = (StationElement) objB;
			if(a.getElementID() != b.getElementID()) return false;
			if(a.getCount() == null && b.getCount() == null) return true;
			if(a.getCount() == null || b.getCount() == null) return false;
			return a.getCount().equalsIgnoreCase(b.getCount());
		}

		@Override
		public int getHashCode(Object obj) {
			if(!(obj instanceof StationElement)) return obj.hashCode();
			StationElement o = (StationElement) obj;
			return o.getElement().getScode().toUpperCase(Locale.getDefault()).hashCode()
					* (o.getCount() == null ? new String().hashCode() : o.getCount().toUpperCase(Locale.getDefault()).hashCode());	
		}
		
	}
}
