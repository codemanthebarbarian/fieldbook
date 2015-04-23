package com.amecfw.sage.model.service;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.persistence.SampleDataProvisioner;
import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.Owner;
import com.amecfw.sage.model.Project;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.OwnerDao;
import com.amecfw.sage.persistence.OwnerDao.Properties;
import com.amecfw.sage.util.CollectionOperations;

import de.greenrobot.dao.query.QueryBuilder;

public class OwnerService {
	/** A owner type of fieldbook , use when a type of fieldbook owns data */
	public static final String FIELDBOOK = "FIELDBOOK";
	
	DaoSession session;
	
	public OwnerService(DaoSession session){
		this.session = session;
	}
	
	public List<Owner> getOwners(){
		return session.getOwnerDao().loadAll();
	}
	
	public List<Owner> getPotentialOwners(){
		get(); //create the default owner
		List<Owner> results = getOwners();
		OwnerNameTypeComparator comparator = new OwnerNameTypeComparator();
		CollectionOperations.Merge(results, getProjectsAsOwner(), comparator);
		CollectionOperations.Merge(results, getSitesAsOwner(), comparator);
		return results;
	}
	
	public List<Owner> getProjectsAsOwner(){
		List<Owner> results;
		List<Project> projects = session.getProjectDao().loadAll();
		if(projects == null) results = new ArrayList<Owner>();
		else results = new ArrayList<Owner>(projects.size());
		for(Project project: projects) results.add(convert(project));
		return results;
	}
	
	public List<Owner> getSitesAsOwner(){
		List<Owner> results;
		List<Site> sites = session.getSiteDao().loadAll();
		if(sites == null) results = new ArrayList<Owner>();
		else results = new ArrayList<Owner>(sites.size());
		for(Site site: sites) results.add(convert(site));
		return results;
	}
	
	public Owner convert(Project project){
		if(project == null) return null;
		return build(project.getProjectNumber(), com.amecfw.sage.persistence.ProjectDao.TABLENAME);
	}
	
	public Owner convert(Site site){
		if(site == null) return null;
		return build(site.getName(), com.amecfw.sage.persistence.SiteDao.TABLENAME);
	}
	
	public Owner build(String name, String type){
		Owner result = new Owner();
		result.setName(name);
		result.setType(type);
		return result;
	}
	
	/**
	 * gets the default tablet owner
	 * @return
	 */
	public Owner get(){
		Owner owner = get("TABLET", "TABLET");
		if(owner == null){
			owner = new SampleDataProvisioner(session).getSystem();
		}
		return owner;
	}
	
	/**
	 * gets an owner based on the id
	 * @param id the owner's id
	 * @return the corresponding owner or null if it doesn't exist
	 */
	public Owner get(long id){
		return session.getOwnerDao().load(id);
	}
	
	public Owner get(Site site){
		return get(site.getName(), com.amecfw.sage.persistence.SiteDao.TABLENAME);
	}
	
	/**
	 * Get the project as an owner
	 * @param project
	 * @return
	 */
	public Owner get(Project project){
		return get(project.getProjectNumber(), com.amecfw.sage.persistence.ProjectDao.TABLENAME);
	}
	
	/**
	 * get a generic owner of the provided name
	 * @param name
	 * @return
	 */
	public Owner get(String name){
		return get(name, OwnerDao.TABLENAME);
	}
	
	private Owner get(String name, String type){
		OwnerDao dao = session.getOwnerDao();
		QueryBuilder<Owner> qb = dao.queryBuilder();
		qb.and(Properties.Name.eq(name), Properties.Type.eq(type));
		return qb.unique();
	}
	
	/**
	 * add a generic owner with the provided name, if one already exists the existing one is returned
	 * @param name
	 * @return
	 */
	public Owner add(String name){
		Owner persisted = get(name, OwnerDao.TABLENAME);
		if(persisted == null){
			persisted = new Owner();
			persisted.setName(name);
			persisted.setType(OwnerDao.TABLENAME);
			persisted.setRowGuid();
			insert(persisted);
		}
		return persisted;
	}
	
	/**
	 * add the project as an owner, it the project is already an owner, the existing one is returned
	 * @param project
	 * @return
	 */
	public Owner add(Project project){
		if(project == null) return null;
		Owner persisted = get(project);
		if(persisted == null){
			persisted = convert(project);
			persisted.setRowGuid();
			insert(persisted);
		}
		return persisted;
	}
	
	public Owner add(Site site){
		if(site == null) return null;
		Owner persisted = get(site);
		if(persisted == null){
			persisted = convert(site);
			persisted.setRowGuid();
			insert(persisted);
		}
		return persisted;
	}
	
	private void insert(Owner owner){
		Owner persisted = get(owner.getName(), owner.getType());
		if(persisted != null){
			owner.setId(persisted.getId());
			owner.setRowGuid(persisted.getRowGuid());
			return;
		}
		session.insert(owner);
	}
	
	public static class OwnerNameTypeComparator implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof Owner)) return false;
			if(!(objB instanceof Owner)) return false;
			Owner a = (Owner) objA;
			Owner b = (Owner) objB;
			if(!a.getName().equalsIgnoreCase(b.getName())) return false;
			if(!a.getType().equalsIgnoreCase(b.getType())) return false;
			return true;
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof Owner)) return 0;
			Owner o = (Owner) obj;
			int code = o.getName() == null ? 7 : o.getName().toUpperCase().hashCode();
			return 13 * code * o.getType().toUpperCase().hashCode();
		}
		
	}
	
}
