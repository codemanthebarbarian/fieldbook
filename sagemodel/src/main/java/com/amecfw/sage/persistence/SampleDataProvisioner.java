package com.amecfw.sage.persistence;

import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.ObservationType;
import com.amecfw.sage.model.Owner;
import com.amecfw.sage.persistence.DaoSession;

public class SampleDataProvisioner {

	private DaoSession session;
	
	private Owner system;
	private ObservationGroup rarePlantStationObservationGroup;
	
	
	public SampleDataProvisioner(DaoSession session){
		this.session = session;
	}
	
	public Owner getSystem(){
		if(system == null){
			system = session.getOwnerDao().queryBuilder().where(com.amecfw.sage.persistence.OwnerDao.Properties.Type.eq("TABLET")).unique();
			if(system == null){
				system = new Owner();
				system.setName("TABLET");
				system.setType("TABLET");
				system.setRowGuid();
				session.getOwnerDao().insert(system);
			}
		}
		return system;
	}
	
	public ObservationGroup getRarePlantStationObservationGroup(){
		if(rarePlantStationObservationGroup == null){
			rarePlantStationObservationGroup = session.getObservationGroupDao()
					.queryBuilder().where(com.amecfw.sage.persistence.ObservationGroupDao.Properties.Name.eq("Rare Plant Station"))
					.unique();
			if(rarePlantStationObservationGroup == null){
				rarePlantStationObservationGroup = new ObservationGroup();
				rarePlantStationObservationGroup.setName("Rare Plant Station");
				rarePlantStationObservationGroup.setOwner(getSystem());
				rarePlantStationObservationGroup.setRowGuid();
				session.getObservationGroupDao().insert(rarePlantStationObservationGroup);
			}
		}
		return rarePlantStationObservationGroup;
	}
	
	public void loadEcositePhase(){
		ObservationType ecoPhase = new ObservationType();
		ecoPhase.setName("Ecosite Phase");
		ecoPhase.setRowGuid();
		session.getObservationTypeDao().insert(ecoPhase);
		GroupObservation groupObservaton = new GroupObservation();
		groupObservaton.setObservationType(ecoPhase);
		groupObservaton.setObservationGroup(getRarePlantStationObservationGroup());
		groupObservaton.setRowGuid();
		groupObservaton.setAllowableValues("a1,a2,b1,b2,c1,c2,d1,d2,marsh,bog,wl1,wl2,wl3");
		session.getGroupObservationDao().insert(groupObservaton);
	}
	
	public void loadFieldCrew(){
		ObservationType fieldCrew = new ObservationType();
		fieldCrew.setName("Field Crew");
		fieldCrew.setRowGuid();
		session.getObservationTypeDao().insert(fieldCrew);
		GroupObservation groupObs = new GroupObservation();
		groupObs.setObservationType(fieldCrew);
		groupObs.setObservationGroup(getRarePlantStationObservationGroup());
		groupObs.setRowGuid();
		groupObs.setAllowableValues("ML,WB,DH,BR,SS,CT");
		session.getGroupObservationDao().insert(groupObs);
	}
	
}
