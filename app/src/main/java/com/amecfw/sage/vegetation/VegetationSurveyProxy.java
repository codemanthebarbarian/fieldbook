package com.amecfw.sage.vegetation;

import java.util.List;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.proxy.StationElementProxy;
import com.amecfw.sage.proxy.StationProxy;

public class VegetationSurveyProxy extends StationProxy {
	
	private List<StationElementProxy> stationElements;

	public List<StationElementProxy> getStationElements() {
		return stationElements;
	}

	public void setStationElements(List<StationElementProxy> stationElements) {
		this.stationElements = stationElements;
	}
	
	/**
	 * A comparator for comparing a vegetation proxy's station name to a string
	 */
	public static class StationNameCIStringComparator implements EqualityComparator{
		
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof VegetationSurveyProxy) || !(objB instanceof String)) return false;
			VegetationSurveyProxy p = (VegetationSurveyProxy) objA;
			String n = (String) objB;
			if(p.getModel() ==  null) return false;
			if(p.getModel().getName() == null) return false;
			return p.getModel().getName().equalsIgnoreCase(n);
		}

		@Override
		public int getHashCode(Object obj) {
			// TODO Auto-generated method stub
			if(obj == null) return 0;
			if(!(obj instanceof VegetationSurveyProxy)) return obj.hashCode();
			VegetationSurveyProxy p = (VegetationSurveyProxy) obj;
			if(p.getModel() ==  null ||p.getModel().getName() == null) return 0;
			return p.getModel().getName().toUpperCase().hashCode();
		}
		
	}
	
}
