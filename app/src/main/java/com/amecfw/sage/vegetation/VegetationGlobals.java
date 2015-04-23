package com.amecfw.sage.vegetation;

import com.amecfw.sage.model.Owner;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.OwnerService;

public class VegetationGlobals {

	/** the meta name value for project sites for rare plant survey */
	public static final String SURVEY_RARE_PLANT = "RARE PLANT";
	/** the meta value name to identify project sites for vegetation surveys */
	public static final String DESCRIMINATOR_VEGETATION = "VEGETATION";
	/** the station type to identity substation for vegetation canopy*/
	public static final String STATION_TYPE_VEGETATION_CANOPY = "Vegetation Canopy";
	/** the station type to identify rareplant category surveys */
	public static final String STATION_TYPE_VEGETATION_RAREPLANT_CATEGORY = "Vegetation Rare Site Survey";
	
	private static Owner vegOwner;
	/**
	 * the default owner for the vegetation component
	 * @return Owner
	 */
	public static Owner getVegetationOwner(){
		if(vegOwner ==  null){
			vegOwner = new OwnerService(SageApplication.getInstance().getDaoSession()).add(DESCRIMINATOR_VEGETATION);
		}
		return vegOwner;
	}
	
}
