package com.amecfw.sage.sulphur.project;

import java.util.Arrays;
import java.util.List;

import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.Project;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.ProjectSiteMeta;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.model.service.MetaDataService;
import com.amecfw.sage.model.service.ObservationService;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.ProjectSiteMetaDao;
import com.amecfw.sage.sulphur.Constants;
import com.amecfw.sage.sulphur.project.Create.VMProxy;

public class Services {
	
	DaoSession session;	

	public Services(DaoSession session){
		this.session = session;
	}
	
	/**
	 * Gets a list of all ProjectSites on the device
	 * @return a list of all ProjectSites or an empty list if none exist
	 */
	public List<ProjectSite> getProjectSites(){
		return new MetaDataService(session).find(ProjectSite.class
				, ProjectSiteMetaDao.TABLENAME
				, Constants.SULPHUR_NAME_DESCRIMINATOR
				, Constants.SULPHUR_VALUE_DESCRIMINATOR);
	}
	
	public void save(VMProxy proxy){
		ProjectSiteMeta meta = new ProjectSiteMeta();
		meta.setName(Constants.SULPHUR_NAME_DESCRIMINATOR);
		meta.setValue(Constants.SULPHUR_VALUE_DESCRIMINATOR);
		MetaDataService.MetaSupportExtensionMethods.addUpdate(proxy.getModel(), meta);
		ProjectSiteServices psService = new ProjectSiteServices(session);
		psService.saveOrUpdate(proxy.getProject(), true);
		Site site = psService.getSite(proxy.getSite(), new ProjectSiteServices.SiteComparatorByName());
		if(site != null) proxy.setSite(site);
		else psService.saveOrUpdate(proxy.getSite(), true);
		proxy.getModel().setProject(proxy.getProject());
		proxy.getModel().setSite(proxy.getSite());
		psService.save(proxy.getModel());
	}
	
	public void update(VMProxy proxy){
		ProjectSiteServices psService = new ProjectSiteServices(session);
		psService.update(proxy.getModel());
	}
	
	/**
	 * Adds a new field staff person to the list of field staff for the GroupObservation.
	 * @param groupObservation
	 * @param fieldStaff
	 * @return
	 */
	public String[] addFieldStaff(GroupObservation groupObservation, String fieldStaff){
		String[] current = ObservationService.parseAllowableValues(groupObservation,",", true);
		if(Arrays.binarySearch(current, fieldStaff) > -1){
			//Add the item
			List<String> tmp = Arrays.asList(current);
			tmp.add(fieldStaff);
			current = tmp.toArray(current);
			Arrays.sort(current);
			String result = Arrays.toString(current);
			groupObservation.setAllowableValues(result.substring(1, current.length - 2));
			session.getGroupObservationDao().update(groupObservation);
		}		
		return current;
	}
	
	/**
	 * Gets all the site stored on the device
	 * @return
	 */
	public String[] getSiteNames(){
		List<Site> sites = session.getSiteDao().loadAll();
		if(sites == null) return new String[0];
		String[] results = new String[sites.size()];
		int i = 0;
		for (Site site : sites) {
			results[i++] = site.getName();
		}
		return results;
	}
	
	public void syncFromViewModel(VMProxy proxy){
		if(proxy.getModel() == null) proxy.setModel(new ProjectSite());
		if(proxy.getModel().getRowGuid() == null) proxy.getModel().setRowGuid();
		if(proxy.getSite() == null) proxy.setSite(new Site());
		if(proxy.getProject() == null) proxy.setProject(new Project());
		proxy.getProject().setName(proxy.getViewModel().getProjectName());
		proxy.getProject().setProjectNumber(proxy.getViewModel().getProjectNumber());
		if(proxy.getProject().getRowGuid() == null) proxy.getProject().setRowGuid();
		proxy.getSite().setName(proxy.getViewModel().getSiteName());
		proxy.getModel().setProject(proxy.getProject());
		proxy.getModel().setSite(proxy.getSite());
		if(proxy.getSite().getRowGuid() == null) proxy.getSite().setRowGuid();
	}
	
}
