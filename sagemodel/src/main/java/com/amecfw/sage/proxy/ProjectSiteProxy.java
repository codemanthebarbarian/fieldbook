package com.amecfw.sage.proxy;

import com.amecfw.sage.model.Project;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.Site;

public abstract class ProjectSiteProxy<TViewModel> extends Model<TViewModel, ProjectSite>{
	protected Site site;
	protected Project project;
	
	public Site getSite() {
		return site;
	}
	public void setSite(Site site) {
		this.site = site;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	
}
