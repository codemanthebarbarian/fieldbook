package com.amecfw.sage.proxy;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.ObservationType;
import com.amecfw.sage.model.service.ObservationService;
import com.amecfw.sage.ui.GroupObservationEditFragment;

public class GroupObservationProxy extends Proxy<GroupObservation> {
	private ObservationType observationType;
	
	public GroupObservationProxy(){}
	
	public GroupObservationProxy(GroupObservation groupObservation){
		if(groupObservation != null){
			model = groupObservation;
			observationType = groupObservation.getObservationType();
		}
	}

	public ObservationType getObservationType() {
		return observationType;
	}

	public void setObservationType(ObservationType observationType) {
		this.observationType = observationType;
	}
	
	public void update(GroupObservationEditFragment.ViewModel viewModel){
		if(model == null) model = new GroupObservation();
		if(viewModel == null) return;
		model.setAllowableValues(ObservationService.getAllowableValues(viewModel.getAllowableValues(), ',', true));
		ObservationType type = new ObservationType();
		type.setName(viewModel.getTypeName());
		type.setComparator(new ObservationService.ObservationTypeComparer());
		if(! type.equals(observationType)) observationType = type;
	}
	
	public static List<GroupObservationProxy> convert(List<GroupObservation> groupObservations){
		if(groupObservations == null) return null;
		List<GroupObservationProxy> results = new ArrayList<GroupObservationProxy>(groupObservations.size());
		for(GroupObservation go : groupObservations){
			results.add(new GroupObservationProxy(go));
		}
		return results;
	}
	
	public static class Comparator implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof GroupObservationProxy)) return false;
			if(!(objB instanceof GroupObservationProxy)) return false;
			GroupObservationProxy a = (GroupObservationProxy) objA;
			GroupObservationProxy b = (GroupObservationProxy) objB;
			if(a.observationType == null || b.observationType == null) return false;
			a.observationType.setComparator(new ObservationService.ObservationTypeComparer());
			return a.observationType.equals(b.observationType);
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof GroupObservationProxy)) return obj.hashCode();
			GroupObservationProxy proxy = (GroupObservationProxy) obj;
			if(proxy.observationType == null) return 0;
			proxy.observationType.setComparator(new ObservationService.ObservationTypeComparer());
			return proxy.observationType.hashCode();
		}
		
	}
}
