package com.amecfw.sage.model.service;

import java.util.List;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.Project;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.ProjectDao;
import com.amecfw.sage.persistence.ProjectSiteMetaDao;
import com.amecfw.sage.persistence.SiteDao;
import com.amecfw.sage.proxy.ProjectSiteProxy;
import com.amecfw.sage.ui.ProjectSiteEdit;

import de.greenrobot.dao.DaoException;

public class ProjectSiteServices {

	private DaoSession session;
	
	/**
	 * 
	 * @param session
	 * 
	 * @exception IllegalArgumentException if session is null
	 */
	public ProjectSiteServices(DaoSession session){
		if(session == null) throw new IllegalArgumentException();
		this.session = session;
	}
	
	/**
	 * gets all the project sites in the database
	 * @return
	 */
	public List<ProjectSite> getProjectSites(){
		return session.getProjectSiteDao().loadAll();
	}
	
	public ProjectSite getProjectSite(long id){
		return session.getProjectSiteDao().load(id);
	}
	
	/**
	 * Get the project site based on the provided metadata name and value
	 * @param metaName
	 * @param metaValue
	 * @return the list of matching project sites or and empty list if none are found
	 */
	public List<ProjectSite> findProjectSites(String metaName, String metaValue){
		return new MetaDataService(session).find(ProjectSite.class, ProjectSiteMetaDao.TABLENAME, metaName, metaValue);
	}
	
	/**
	 * Get the project site based on the provided metadata name
	 * @param metaName
	 * @return the list of matching project sites or and empty list if none are found
	 */
	public List<ProjectSite> findProjectSitesByMetaName(String metaName){
		return new MetaDataService(session).findByMetaName(ProjectSite.class, ProjectSiteMetaDao.TABLENAME, metaName);
	}
	
	/**
	 * Get the project site based on the provided metadata value
	 * @param metaValue
	 * @return the list of matching project sites or and empty list if none are found
	 */
	public List<ProjectSite> findProjectSitesByMetaValue(String metaValue){
		return new MetaDataService(session).findByMetaValue(ProjectSite.class, ProjectSiteMetaDao.TABLENAME, metaValue);
	}
	
	public ProjectSite saveOrUpdate(ProjectSite projectSite){
		if (projectSite == null) return null;
		if(projectSite.getId() != null && projectSite.getId() > 0){
			update(projectSite);
			return projectSite;
		}
		List<ProjectSite> projectSites = getProjectSites();
		if(projectSites != null){
			projectSite.setComparator(new ProjectSiteComparatorByProjectAndSiteId());
			int index = projectSites.indexOf(projectSite);
			if(index >= 0 ){
				ProjectSite persisted = projectSites.get(index);
				projectSite.setId(persisted.getId());
				projectSite.setRowGuid(persisted.getRowGuid());
				MetaDataService.MetaSupportExtensionMethods.merge(persisted, projectSite);
				update(persisted);
				return persisted;
			}else{
				save(projectSite);
				return projectSite;
			}
		}//generally will not get past here only on first projectsite ever persisted.
		save(projectSite);
		return projectSite;
	}
	
	/**
	 * Saves the project site, the dependent project and site must be saved before calling
	 * this method.
	 * @param projectSite
	 */
	public void save(ProjectSite projectSite){
		if(projectSite.getRowGuid() == null) projectSite.setRowGuid();
		projectSite.setId(null);
		session.getProjectSiteDao().insert(projectSite);
		MetaDataService.save(projectSite, session.getProjectSiteMetaDao());
	}
	
	public void update(ProjectSite projectSite){
		projectSite.setProject(saveOrUpdate(projectSite.getProject(), true));
		projectSite.setSite(saveOrUpdate(projectSite.getSite(), true));
		session.getProjectSiteDao().update(projectSite);
		MetaDataService.update(projectSite, session.getProjectSiteMetaDao());
	}
	
	/**
	 * Save the proxy in a transaction
	 * @param proxy
	 * @return true if saved successfully otherwise false
	 */
	public <T> boolean saveOrUpdateInTransaction(ProjectSiteProxy<T> proxy){
		boolean result;
		session.getDatabase().beginTransaction();
		try{
			saveOrUpdate(proxy);
			session.getDatabase().setTransactionSuccessful();
			result = true;
		}catch(DaoException daoe){
			result = false;
		}finally{
			session.getDatabase().endTransaction();
		}
		return result;
	}
	
	/**
	 * Saves the proxy, can be used in your own transaction.
	 * @param proxy
	 * @exception DaoException is there was an issue saving the proxy
	 */
	public <T> void saveOrUpdate(ProjectSiteProxy<T> proxy){
		proxy.setProject(saveOrUpdate(proxy.getProject(), true));
		proxy.setSite(saveOrUpdate(proxy.getSite(), true));
		proxy.getModel().setProject(proxy.getProject());
		proxy.getModel().setSite(proxy.getSite());
		proxy.setModel(saveOrUpdate(proxy.getModel()));
	}
	
	/**
	 * updates the proxy in a transaction. If not successful the changes should have been 
	 * rolled back.
	 * @param proxy
	 * @return true if updated successfully otherwise false.
	 */
	public <T> boolean updateInTransaction(ProjectSiteProxy<T> proxy){
		boolean result;
		session.getDatabase().beginTransaction();
		try{
			update(proxy);
			session.getDatabase().setTransactionSuccessful();
			result = true;
		}catch(DaoException daoe){
			result = false;
		}finally{
			session.getDatabase().endTransaction();
		}
		return result;
	}
	/**
	 * updates the proxy (including project and site)
	 * @param proxy
	 * @exception DaoException if the changes were not successfully saved to the database
	 */
	public <T> void update(ProjectSiteProxy<T> proxy){
		proxy.setProject(saveOrUpdate(proxy.getProject(), true));
		proxy.setSite(saveOrUpdate(proxy.getSite(), true));
		proxy.getModel().setProject(proxy.getProject());
		proxy.getModel().setSite(proxy.getSite());
		update(proxy.getModel());
	}
	
	public List<Project> getProjects(){
		return session.getProjectDao().loadAll();
	}
	
	public void save(Project project){
		if(project.getRowGuid() == null) project.setRowGuid();
		project.setId(null);
		session.getProjectDao().insert(project);
	}
	
	public void update(Project project){
		session.getProjectDao().update(project);
	}
	
	public boolean exists(Project project, EqualityComparator comparator){
		List<Project> projects = getProjects();
		if(projects == null || projects.size() < 1) return false;
		project.setComparator(comparator == null ? new ProjectComparatorByProjectNumber() : comparator);
		return projects.contains(project);
	}
	
	public List<Project> findProjects(String projectNumber){
		if(projectNumber == null) return null;
		return session.getProjectDao().queryBuilder()
				.where(ProjectDao.Properties.ProjectNumber.eq(projectNumber)).list();
	}
	
	/**
	 * Gets the first project (sorted by id) in the database based on the provided EqulityComparator. If one doesn't exist
	 * then null is returned.
	 * @param project the project to search for, if null, null is returned
	 * @param comparator the EqualityComparator to use, if null ProjectComparatorByProjectNumber is used
	 * @return the persisted project or null if no match is found
	 */
	public Project getProject(Project project, EqualityComparator comparator){
		if(project == null) return null;
		project.setComparator(comparator == null ? new ProjectComparatorByProjectNumber() : comparator);
		if(project == null || project.getProjectNumber() == null) return null;
		List<Project> results = session.getProjectDao().queryBuilder().orderAsc(ProjectDao.Properties.Id).list();
		if(results == null || results.size() <1) return null;
		int index = results.indexOf(project);
		if(index < 0) return null;
		return results.get(index);
	}
	
	/**
	 * Saves the project as a new one in the database if the id is null or less than zero and one is not found using
	 * the ProjectComparator. If it has a valid ID, it the matching project is updated in the database. If saved the same project
	 * is returned or if updated when a matching project if found, the persisted one is returned.
	 * @param project the project to save or update
	 * @param cascade true to update any root projects
	 * @return the same project provided or a persisted matching site
	 */
	public Project saveOrUpdate(Project project, boolean cascade){
		if(cascade && project.hasRoot()) project.setRoot(saveOrUpdate(project.getRoot(), cascade));
		if(project.getId() == null || project.getId() < 1){
			Project persisted = getProject(project, new ProjectComparator());
			if(persisted == null) {
				save(project);
				return project;
			}else{
				persisted.setNode(project.getNode());
				project.setRowGuid(persisted.getRowGuid());
				update(persisted);
				return persisted;
			}
		}
		else {
			update(project);
			return project;
		}
	}
	
	public List<Site> getSites(){
		return session.getSiteDao().loadAll();
	}
	
	public void save(Site site){
		if(site.getRowGuid() == null) site.setRowGuid();
		site.setId(null);
		session.getSiteDao().insert(site);
	}
	
	public void update(Site site){
		session.getSiteDao().update(site);
	}
	
	/**
	 * Saves the site as a new one in the database if the id is null or less than zero and one is not found using
	 * the SiteComparator. If it has a valid ID, it the matching site is updated in the database. If saved the same site
	 * is returned or if updated when a matching site if found, the persisted one is returned.
	 * @param site the site to save or update
	 * @param cascade true to update any roots sites
	 * @return the same site provided or a persisted matching site
	 */
	public Site saveOrUpdate(Site site, boolean cascade){
		if(cascade && site.hasRoot()) site.setRoot(saveOrUpdate(site.getRoot(), cascade));
		if(site.getId() == null || site.getId() < 1){ 
			Site persisted = getSite(site, new SiteComparator());
			if(persisted == null) {
				save(site);
				return site;
			}			else {
				persisted.setNode(site.getNode());
				site.setRowGuid(persisted.getRowGuid());
				update(persisted);
				return persisted;
			}
		}
		else { 
			update(site);
			return site;
		}
	}
	
	/**
	 * Checks the database to see if a matching site already exists using the SiteComparator
	 * @param site
	 * @return true is a match exists in the database
	 */
	public boolean exists(Site site, EqualityComparator comparator){
		if(site == null) return false;
		site.setComparator(comparator == null ? new SiteComparatorByName() : comparator);
		List<Site> sites = session.getSiteDao().queryBuilder().orderAsc(SiteDao.Properties.Id).list();
		if(sites == null || sites.size() < 1) return false;
		return sites.contains(site);
	}
	
	/**
	 * Gets the first site (sorted by id) in the database based on the provided EqulityComparator. If one doesn't exist
	 * then null is returned.
	 * @param site the Site to search for, if null, null is returned
	 * @param comparator the EqualityComparator to use, if null SiteComparatorByName is used
	 * @return The persisted site or null if there is no match
	 */
	public Site getSite(Site site, EqualityComparator comparator){
		if(site == null) return null;
		List<Site> sites = session.getSiteDao().queryBuilder().orderAsc(SiteDao.Properties.Id).list();
		if(sites == null || sites.size() < 1) return null;
		site.setComparator(comparator == null ? new SiteComparatorByName() : comparator);
		int index = sites.indexOf(site);
		if(index >= 0) return sites.get(index);
		return null;
	}
	
	public List<Site> findSites(String name){
		return session.getSiteDao().queryBuilder().where(SiteDao.Properties.Name.eq(name)).list();
	}

	public static ProjectSiteEdit.ViewModel createViewModel(ProjectSite projectSite){
		ProjectSiteEdit.ViewModel result = new ProjectSiteEdit.ViewModel();
		if (projectSite == null) return result;
		DescriptorServices.getByFieldDescriptor(result, projectSite.getProject());
		DescriptorServices.getByFieldDescriptor(result, projectSite.getSite());
		return result;
	}
	
	/**
	 * An EqualityComparator based on a ProjectSite's project's id and site's id. The project and site
	 * for the ProjectSite must be already existing in the database (otherwise they have no ids).
	 * If anything is null (ProjectSite, Project, ProjectID, Site, or SiteID) false is returned.
	 */
	public static class ProjectSiteComparatorByProjectAndSiteId implements EqualityComparator {

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof ProjectSite)) return false;
			if(!(objB instanceof ProjectSite)) return false;
			ProjectSite a = (ProjectSite) objA;
			ProjectSite b = (ProjectSite) objB;
			if(a.getProject() == null || b.getProject() == null) return false;
			if(a.getSite() == null || b.getSite() == null) return false;
			if(a.getProject().getId() == null || b.getProject().getId() == null) return false;
			if(a.getSite().getId() == null || b.getSite().getId() == null) return false;
			if(a.getProject().getId() !=  b.getProject().getId()) return false;
			if(a.getSite().getId() != b.getSite().getId()) return false;
			return true;
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof ProjectSite)) return 0;
			ProjectSite ps = (ProjectSite) obj;
			if(ps.getProject() == null || ps.getSite() == null) return 0;
			if(ps.getProject().getId() == null || ps.getSite().getId() == null) return 0;
			return ps.getProject().getId().hashCode() * ps.getSite().getId().hashCode() * 13;
		}
		
	}
	
	public static class SiteComparatorByName implements EqualityComparator {

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Site)) return false;
			if(!(objB instanceof Site)) return false;
			Site a = (Site) objA;
			Site b = (Site) objB;
			if(! a.getName().equalsIgnoreCase(b.getName())) return false;
			return true;
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Site)) return obj.hashCode();
			Site s = (Site) obj;
			int hash = s.getName() == null ? 7 : s.getName().toUpperCase().hashCode();
			return hash;
		}
		
	}
	
	public static class SiteComparator implements EqualityComparator {

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Site)) return false;
			if(!(objB instanceof Site)) return false;
			Site a = (Site) objA;
			Site b = (Site) objB;
			if(! a.getName().equalsIgnoreCase(b.getName())) return false;
			//check root equality
			if(a.hasRoot()){
				a.getRoot().setComparator(this);
				if(! b.hasRoot()) return false;
				if(! a.getRoot().equals(b.getRoot())) return false;
			}else if(b.hasRoot()) return false; //a root was null but b was not null
			return true;
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Site)) return obj.hashCode();
			Site s = (Site) obj;
			int hash = s.getName() == null ? 7 : s.getName().toUpperCase().hashCode();
			if(s.hasRoot()){
				s.getRoot().setComparator(this);
				hash *= s.getRoot().hashCode();
			}
			return hash;
		}
		
	}
	
	public static class ProjectComparatorByProjectNumber implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Project)) return false;
			if(!(objB instanceof Project)) return false;
			Project a = (Project) objA;
			Project b = (Project) objB;
			if(! a.getProjectNumber().equalsIgnoreCase(b.getProjectNumber())) return false;
			return true;
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Project)) return obj.hashCode();
			Project s = (Project) obj;
			int hash = s.getProjectNumber() == null ? 7 : s.getProjectNumber().toUpperCase().hashCode();
			return hash;
		}
		
	}

	public static class ProjectComparator implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Project)) return false;
			if(!(objB instanceof Project)) return false;
			Project a = (Project) objA;
			Project b = (Project) objB;
			if(! a.getProjectNumber().equalsIgnoreCase(b.getProjectNumber())) return false;
			//check root equality
			if(a.hasRoot()){
				a.getRoot().setComparator(this);
				if(! b.hasRoot()) return false;
				if(! a.getRoot().equals(b.getRoot())) return false;
			}else if(b.hasRoot()) return false; //a root was null but b was not null
			return true;
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Project)) return obj.hashCode();
			Project s = (Project) obj;
			int hash = s.getProjectNumber() == null ? 7 : s.getProjectNumber().toUpperCase().hashCode();
			if(s.hasRoot()){
				s.getRoot().setComparator(this);
				hash *= s.getRoot().hashCode();
			}
			return hash;
		}
		
	}

}
