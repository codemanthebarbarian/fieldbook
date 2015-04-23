package com.amecfw.sage.ui;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.ProjectSiteMeta;
import com.amecfw.sage.ui.ProjectSiteEdit.ViewModel;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.MetaDataService;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.persistence.DaoSession;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

/**
 * A generic activity for managing projects and project sites. Project can be provisioned using associated metadata names
 * and or values. If a meta name and/or value is passed as an extra, they will be used as filters for
 * which projects to retrieve from the system and for provisioning.
 */
public class ProjectSiteManagement extends Activity { 
//Implements ProjectSiteList.OnItemSelectedHandler , ProjectSiteEdit.OnExitListener 
	
	/** the key for Passing the viewstate for the activity */
	public static final String EXTRA_VIEWSTATE = "sage.ui.ProjectSiteManagement.viewState";
	/** The key for passing the project's key in the SageApplication Cache (should be string)*/
	public static final String EXTRA_PROJECT_CACHE_KEY = "sage.ui.ProjectSiteManagement.projectCacheKey";
	/** The key for passing the project's id to get from the database (must be long) */
	public static final String EXTRA_PROJECT_ID = "sage.ui.ProjectSiteManagement.projectID";
	/** The key for the id in the sage domain R file for the initial fragment 
	 * (must be one of project_list or project_edit
	 * */
	public static final String EXTRA_INITIAL_VIEW = "sage.ui.ProjectSiteManagement.fragment";
	/** the key for filtering project site by metadata name (used in provisioning for specific components) */
	public static final String EXTRA_META_NAME = "sage.ui.ProjectSiteManagement.metaName";
	/** the key for filtering projects by metadata value (used in provisioning for specific components)  */
	public static final String EXTRA_META_VALUE = "sage.ui.ProjectSiteManagement.metaValue";
	/** The key for passing the project site's key in the SageApplication Cache (should be string)*/
	public static final String EXTRA_PROJECT_SITE_CACHE_KEY = "sage.ui.ProjectSiteManagement.projectSiteCacheKey";
	/** The key for passing the project site's id to get from the database (must be long) */
	public static final String EXTRA_PROJECT_SITE_ID = "sage.ui.ProjectSiteManagement.projectSiteID";
	/** The key for passing the project site's as parcelable */
	public static final String EXTRA_PROJECT_SITE = "sage.ui.ProjectSiteManagement.projectSite";
	/** the key for passing the current view in the saved instance state bundle */
	protected static final String BUNDLE_CURRENT_VIEW = "sage.ui.ProjectSiteManagement.currentView";
	
/*	protected int initialView;
	protected int currentView;
	protected ViewState viewState;
	protected String metaName;
	protected String metaValue;
	protected ProjectSite projectSite;
	protected ProjectSiteEdit.Proxy projectSiteProxy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null) onRestoreInstanceState(savedInstanceState);
		else initialize(getIntent().getExtras());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState.containsKey(EXTRA_META_NAME)) metaName = savedInstanceState.getString(EXTRA_META_NAME);
		if(savedInstanceState.containsKey(EXTRA_META_VALUE)) metaValue = savedInstanceState.getString(EXTRA_META_VALUE);
		viewState = savedInstanceState.getParcelable(EXTRA_VIEWSTATE);
		initialView = savedInstanceState.getInt(EXTRA_INITIAL_VIEW, R.layout.simple_list_layout);
		currentView = savedInstanceState.getInt(BUNDLE_CURRENT_VIEW, initialView);
		projectSite = getProjectSite(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(metaName != null) outState.putString(EXTRA_META_NAME, metaName);
		if(metaValue != null) outState.putString(EXTRA_META_VALUE, metaValue);
		outState.putInt(EXTRA_INITIAL_VIEW, initialView);
		outState.putInt(BUNDLE_CURRENT_VIEW, currentView);
		outState.putParcelable(EXTRA_VIEWSTATE, viewState);
		super.onSaveInstanceState(outState);
	}

	*//**
	 * The default behavior is to display the projectSite in edit mode. If extending this can 
	 * be overwritten to do what is required with the selected item in list display mode.
	 *//*
	@Override
	public void onItemSelected(ProjectSite projectSite) {
		if(projectSite ==  null) return;
		this.projectSite = projectSite;
		displayEdit(ViewState.getViewStateEdit());
	}
	
	private void initialize(Bundle extras){
		if(extras != null){
			viewState = extras.getParcelable(EXTRA_VIEWSTATE);
			metaName = extras.getString(EXTRA_META_NAME);
			metaValue = extras.getString(EXTRA_META_VALUE);
			initialView = extras.getInt(EXTRA_INITIAL_VIEW, R.layout.simple_list_layout);
			projectSite = getProjectSite(extras);
		}else{
			viewState = ViewState.getViewStateView();
			initialView = R.layout.project_site_edit;
		}
		if(initialView == R.layout.project_site_edit) displayEdit(viewState);
		else displayList();
	}
	
	*//**
	 * Display a projectSite in edit mode or however the viewState is set. If no viewState is provided or if projectSite
	 * is null. then defaults to add.
	 * @param viewState
	 *//*
	protected final void displayEdit(ViewState viewState){
		Bundle args = new Bundle();
		if((viewState != null && viewState.getState() != ViewState.ADD) || projectSite != null){
			projectSiteProxy = ProjectSiteEdit.Proxy.create(projectSite);
			args.putParcelable(ProjectSiteEdit.ARG_VIEW_MODEL, projectSiteProxy.getViewModel());
			args.putParcelable(ProjectSiteEdit.ARG_VIEWSTATE, viewState == null ? ViewState.getViewStateEdit() : viewState);
		}else{
			args.putParcelable(ProjectSiteEdit.ARG_VIEWSTATE, viewState == null ? ViewState.getViewStateAdd() : viewState);
		}
		ProjectSiteEdit fragment = new ProjectSiteEdit();
		fragment.setArguments(args);
		fragment.setOnExitListener(this);
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.projectManagementActivity, fragment);
		currentView = R.layout.project_site_edit;
		ft.commit();
	}
	
	private boolean projectSiteEditExitListenerAttached;
	private ProjectSiteEdit.OnExitListener projectSiteEditExitListener;
	*//**
	 * Override the exit listener for the ProjectSiteEdit fragment hosted in the activity. The default 
	 * behavior is to save/update the project site in the database and display the previous 
	 * @param listener the new listener functionality to add to or replace the default
	 * @param attach true to run the provided listener after the default is run
	 *//*
	public void setProjectSiteEditExitListener(ProjectSiteEdit.OnExitListener listener, boolean attach){
		projectSiteEditExitListener = listener;
		projectSiteEditExitListenerAttached = attach;
	}
	
	@Override
	public void onExit(ViewModel viewModel, ViewState viewState) {
		if(projectSiteProxy == null) projectSiteProxy = new ProjectSiteEdit.Proxy();
		if(projectSiteEditExitListener == null || projectSiteEditExitListenerAttached){
			switch (viewState.getState()) {
			case ViewState.EDIT:
				projectSiteProxy.setViewModel(viewModel);
				update(projectSiteProxy);
				break;
			case ViewState.ADD:
				projectSiteProxy.setViewModel(viewModel);
				save(projectSiteProxy);
				break;
			}
		}
		if(projectSiteEditExitListener != null) projectSiteEditExitListener.onExit(viewModel, viewState);
	}

	*//**
	 * display the list fragment using values from metaname and metavalue passed in arguments 
	 * to the fragment. If none are provided, displays all the project sites in the system
	 *//*
	protected final void displayList(){
		Bundle args = new Bundle();
		if(metaName != null) args.putString(ProjectSiteList.ARG_META_NAME, metaName);
		if(metaValue != null) args.putString(ProjectSiteList.ARG_META_VALUE, metaValue);
		ProjectSiteList fragment = new ProjectSiteList();
		fragment.setArguments(args);
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.projectManagementActivity, fragment);
		ft.commit();
	}
	
	private void update(ProjectSiteEdit.Proxy proxy){
		proxy.buildModel();
		if(metaName != null || metaValue != null){
			ProjectSiteMeta meta = new ProjectSiteMeta();
			meta.setName(metaName);
			meta.setValue(metaValue);
			MetaDataService.MetaSupportExtensionMethods.addUpdate(proxy.getModel(), meta);
		}
		DaoSession session = SageApplication.getInstance().getDaoSession();
		ProjectSiteServices service = new ProjectSiteServices(session);
		service.updateInTransaction(proxy);
	}
	
	private void save(ProjectSiteEdit.Proxy proxy){
		proxy.buildModel();
		if(metaName != null || metaValue != null){
			ProjectSiteMeta meta = new ProjectSiteMeta();
			meta.setName(metaName);
			meta.setValue(metaValue);
			MetaDataService.MetaSupportExtensionMethods.addUpdate(proxy.getModel(), meta);
		}
		DaoSession session = SageApplication.getInstance().getDaoSession();
		ProjectSiteServices service = new ProjectSiteServices(session);
		service.saveOrUpdateInTransaction(proxy);
	}
	
	private ProjectSite getProjectSite(Bundle bundle){
		if (bundle == null) return null;
		ProjectSite result;
		if(bundle.containsKey(EXTRA_PROJECT_SITE_CACHE_KEY))
			result = (ProjectSite) SageApplication.getInstance().getObject(bundle.getString(EXTRA_PROJECT_SITE_CACHE_KEY));
		else if(bundle.containsKey(EXTRA_PROJECT_SITE_ID))
			result = new ProjectSiteServices(SageApplication.getInstance().getDaoSession()).getProjectSite(bundle.getLong(EXTRA_PROJECT_SITE_ID));
		else if(bundle.containsKey(EXTRA_PROJECT_SITE))
			result = bundle.getParcelable(EXTRA_PROJECT_SITE);
		else
			result = null;
		return result;
	}
*/
}
