package com.amecfw.sage.proxy;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.Owner;
import com.amecfw.sage.model.service.OwnerService;
import com.amecfw.sage.ui.GroupObservationEditFragment;
import com.amecfw.sage.ui.ObservationGroupEditFragment;
import com.amecfw.sage.util.CollectionOperations;

public class ObservationGroupProxy extends Proxy<ObservationGroup>  {

	private List<GroupObservationProxy> groupObservations;
	private Owner owner;

	public List<GroupObservationProxy> getGroupObservations() {
		return groupObservations;
	}

	public void setGroupObservations(List<GroupObservationProxy> groupObservations) {
		this.groupObservations = groupObservations;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}
	
	public void update(ObservationGroupEditFragment.ViewModel viewModel){
		if(this.model == null) model = new ObservationGroup();
		if(viewModel == null) return;
		model.setName(viewModel.getName());
		Owner tmp = new Owner();
		owner.setName(viewModel.getOwnerName());
		owner.setType(viewModel.getOwnerType());
		tmp.setComparator(new OwnerService.OwnerNameTypeComparator());
		if(! tmp.equals(owner)) owner = tmp;
	}
	
	public void addGroupObservation(GroupObservationEditFragment.ViewModel viewModel){
		if(groupObservations == null) groupObservations = new ArrayList<GroupObservationProxy>();
		GroupObservationProxy groupObservationProxy = new GroupObservationProxy();
		groupObservationProxy.update(viewModel);
		CollectionOperations.addOrReplace(groupObservations, groupObservationProxy, new GroupObservationProxy.Comparator());
	}	
}
