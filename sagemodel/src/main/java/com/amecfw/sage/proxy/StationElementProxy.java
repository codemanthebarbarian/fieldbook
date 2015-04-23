package com.amecfw.sage.proxy;

import java.util.List;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.StationElement;
import com.amecfw.sage.model.service.ElementService;

public class StationElementProxy extends Proxy<StationElement> {

	private List<PhotoProxy> photos;
	private LocationProxy location;
	
	public List<PhotoProxy> getPhotos() {
		return photos;
	}
	public void setPhotos(List<PhotoProxy> photos) {
		this.photos = photos;
	}
	public LocationProxy getLocation() {
		return location;
	}
	public void setLocation(LocationProxy location) {
		this.location = location;
	}
	
	/**
	 * compares StationElementProxies using the RowGuid of the station element
	 * @See ElementService.ElementRowGuidComparator
	 */
	public static class StationElementProxyByRowGuidComparator implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof StationElementProxy)) return false;
			if(!(objB instanceof StationElementProxy)) return false;
			StationElementProxy a = (StationElementProxy) objA;
			StationElementProxy b = (StationElementProxy) objB;
			if(a.getModel() == null || b.getModel() == null) return false;
			if(a.getModel().getRowGuid() == null || b.getModel().getRowGuid() == null) return false;
			return a.getModel().getUUID().equals(b.getModel().getUUID());
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof StationElementProxy)) return obj.hashCode();
			StationElementProxy o = (StationElementProxy) obj;
			if(o.getModel() == null) return 0;
			if(o.getModel().getRowGuid() == null) return 0;
			return o.getModel().getUUID().hashCode();
		}
		
	}
	
	/**
	 * compares StationElementProxy using the RowGuid of the Element
	 * @See ElementService.ElementRowGuidComparator
	 */
	public static class StationElementProxyByElementRowGuidComparator implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof StationElementProxy)) return false;
			if(!(objB instanceof StationElementProxy)) return false;
			StationElementProxy a = (StationElementProxy) objA;
			StationElementProxy b = (StationElementProxy) objB;
			if(a.getModel() == null || b.getModel() == null) return false;
			if(a.getModel().getElement() == null || b.getModel().getElement() == null) return false;
			a.getModel().getElement().setComparator(new ElementService.ElementRowGuidComparator());
			return a.getModel().getElement().equals(b.getModel().getElement());
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof StationElementProxy)) return obj.hashCode();
			StationElementProxy o = (StationElementProxy) obj;
			if(o.getModel() == null) return 0;
			if(o.getModel().getElement() == null) return 0;
			o.getModel().getElement().setComparator(new ElementService.ElementRowGuidComparator());
			return o.getModel().getElement().hashCode();
		}		
	}
	
}
