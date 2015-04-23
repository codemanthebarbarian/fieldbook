package com.amecfw.sage.ui;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.FieldDescriptor;
import com.amecfw.sage.model.Project;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.proxy.ProjectSiteProxy;
import com.amecfw.sage.util.OnExitListener;
import com.amecfw.sage.util.ViewState;

import de.greenrobot.dao.DaoException;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class ProjectSiteEdit extends Fragment {

	/** the key for Passing the viewstate for the activity */
	public static final String ARG_VIEWSTATE = ProjectSiteManagement.EXTRA_VIEWSTATE;
	/** The key for passing the viewmodel as parcelable */
	public static final String ARG_VIEW_MODEL = "ProjectSiteEdit.ViewModel";
	
	private ImageButton allProjects;
	private ImageButton allSites;
	private EditText projectNumber;
	private EditText projectName;
	private EditText siteName;
	private ViewState viewState;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.project_site_edit, container, false);
		initialize(view);
		if(savedInstanceState == null) initialize(getArguments());
		return view;
	}
	
	private void initialize(View view){
		allProjects = (ImageButton) view.findViewById(R.id.projectSiteEdit_btnSelectProject);
		allSites = (ImageButton) view.findViewById(R.id.projectSiteEdit_btnSelectSite);
		projectNumber = (EditText) view.findViewById(R.id.projectSiteEdit_txtProjectNum);
		projectName = (EditText) view.findViewById(R.id.projectSiteEdit_txtProjectName);
		siteName = (EditText) view.findViewById(R.id.projectSiteEdit_txtSiteName);
		allProjects.setOnClickListener(selectProjectOnClickListener);
		allSites.setOnClickListener(selectSiteOnClickListener);
	}
	
	private void initialize(Bundle args){
		viewState = args.getParcelable(ARG_VIEWSTATE);
		if(viewState == null) viewState = ViewState.getViewStateAdd();
		if(viewState.getState() != ViewState.ADD) setViewModel((ViewModel)args.getParcelable(ARG_VIEW_MODEL));
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if(savedInstanceState != null){
			if(savedInstanceState.containsKey(ARG_VIEWSTATE)) setViewModel((ViewModel)savedInstanceState.getParcelable(ARG_VIEWSTATE));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(ARG_VIEWSTATE, viewState);
		outState.putParcelable(ARG_VIEW_MODEL, getViewModel());
	}
	
/*	OnClickListener onExitClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.projectSiteEdit_btnExit && onExitListener != null){
				onExitListener.onExit(getViewModel(), viewState);
			}
		}
	};*/
	
	private OnExitListener <ViewModel> onExitListener;
	public void setOnExitListener(OnExitListener<ViewModel> listener){
		onExitListener = listener;
	}
///////////////////////////////////////////////////////
// Site Selection Listeners
///////////////////////////////////////////////////////
	private OnClickListener selectSiteOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fm = getFragmentManager();
			SiteSelectDialogFragment dialog = new SiteSelectDialogFragment();
			dialog.setDialogListener(siteSelectDialogListener);
			dialog.show(fm, null);
		}
	};
	/**
	 * Use to override the default behavior of showing the SiteSelctDialogFragment
	 * @param listener an alternate listener
	 */
	public void setSelectSiteOnClickListener(OnClickListener listener){
		if(listener != null) selectSiteOnClickListener = listener;
	}
	
	private SiteSelectDialogFragment.DialogListener siteSelectDialogListener = new SiteSelectDialogFragment.DialogListener(){
		@Override
		public void onDialogClick(Site site) {
			if(site != null) siteName.setText(site.getName());
		}
		@Override
		public void onDialogCancel() {} //Do nothing		
	};
	/**
	 * use to override the default behavior when responding to the ProjectSelectDialogFragment's events. The
	 * default is to update the project number and project name from the one selected. If canceled, nothing is updated.
	 * @param listener
	 */
	public void setSiteSelectDialogListener(SiteSelectDialogFragment.DialogListener listener){
		if(listener != null) siteSelectDialogListener = listener;
	}	
///////////////////////////////////////////////////////
// END Site Selection Listeners
///////////////////////////////////////////////////////
	
///////////////////////////////////////////////////////
// Project Selection Listeners
///////////////////////////////////////////////////////
	
	private OnClickListener selectProjectOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			FragmentManager fm = getFragmentManager();
			ProjectSelectDialogFragment dialog = new ProjectSelectDialogFragment();
			dialog.setDialogListener(projectSelectDialogListener);
			dialog.show(fm, null);
		}		
	};
	/**
	 * Use to override the default behavior of showing the SiteSelctDialogFragment
	 * @param listener an alternate listener
	 */
	public void setSelectProjectOnClickListener(OnClickListener listener){
		if(listener != null) selectProjectOnClickListener = listener;
	}
	
	private ProjectSelectDialogFragment.DialogListener projectSelectDialogListener = new ProjectSelectDialogFragment.DialogListener(){
		@Override
		public void onDialogCancel() {} //Do nothing
		@Override
		public void onDialogClick(com.amecfw.sage.model.Project project) {
			if(project != null){
				projectNumber.setText(project.getProjectNumber());
				projectName.setText(project.getName());
			}
		};
	};	
	/**
	 * use to override the default behavior when responding to the ProjectSelectDialogFragment's events. The
	 * default is to update the project number and project name from the one selected. If canceled, nothing is updated.
	 * @param listener
	 */
	public void setProjectSelectDialogFragmentListener(ProjectSelectDialogFragment.DialogListener listener){
		if(listener != null) projectSelectDialogListener = listener;
	}
///////////////////////////////////////////////////////
// ENDProject Selection Listeners
///////////////////////////////////////////////////////
	
	public ViewModel getViewModel(){
		ViewModel vm = new ViewModel();
		vm.setProjectName(projectName.getText().toString());
		vm.setProjectNumber(projectNumber.getText().toString());
		vm.setSiteName(siteName.getText().toString());
		return vm;
	}
	
	public void setViewModel(ViewModel viewModel){
		if(viewModel != null){
			projectName.setText(viewModel.getProjectName());
			projectNumber.setText(viewModel.getProjectNumber());
			siteName.setText(viewModel.getSiteName());
		}
	}
	
	public static class ViewModel implements Parcelable {
		@FieldDescriptor(clazz = Project.class, targetGetter = "getProjectNumber", targetSetter = "setProjectNumber", 
				type = DescriptorServices.TYPE_STRING)
		private String projectNumber;
		@FieldDescriptor(clazz = Project.class, targetGetter = "getName", targetSetter = "setName", 
				type = DescriptorServices.TYPE_STRING)
		private String projectName;
		@FieldDescriptor(clazz = Site.class, targetGetter = "getName", targetSetter = "setName", 
				type = DescriptorServices.TYPE_STRING)
		private String siteName;
		
		public ViewModel(){}
		
		public String getProjectNumber() {
			return projectNumber;
		}
		public void setProjectNumber(String projectNumber) {
			this.projectNumber = projectNumber;
		}
		public String getProjectName() {
			return projectName;
		}
		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}
		public String getSiteName() {
			return siteName;
		}
		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}
		
		public static final Parcelable.Creator<ViewModel> CREATOR = 
				new Parcelable.Creator<ViewModel>() {
			@Override
			public ViewModel createFromParcel(Parcel in) {return new ViewModel(in);}
			@Override 
			public ViewModel[] newArray(int size) { return new ViewModel[size];}
				};
				
		public ViewModel(Parcel in){
			this.projectName = in.readString();
			this.projectNumber = in.readString();
			this.siteName = in.readString();
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags){
			dest.writeString(projectName);
			dest.writeString(projectNumber);
			dest.writeString(siteName);
		}
		
		@Override
		public int describeContents(){
			return 0;
		}
	}
	
	public static class Proxy extends ProjectSiteProxy<ProjectSiteEdit.ViewModel>{

		@Override
		public void buildViewModel() {
			if(viewModel == null) viewModel = new ProjectSiteEdit.ViewModel();
			if(project != null) DescriptorServices.getByFieldDescriptor(viewModel, project);
			if(site != null) DescriptorServices.getByFieldDescriptor(viewModel, site);
		}

		@Override
		public void buildModel() {
			if(model == null) model = new ProjectSite();
			if(project == null) project = new Project();
			if(site == null) site = new Site();
			DescriptorServices.setByFieldDescriptor(viewModel, project);
			DescriptorServices.setByFieldDescriptor(viewModel, site);
		}
		
		/**
		 * Takes an existing project site and create a new proxy with a generated viewmodel. The projectsite must
		 * be in and connected the database or null is returned.
		 * @param projectsite
		 * @return the created projectsite or null if the object is disconnected from the database.
		 */
		public static Proxy create(ProjectSite projectsite){
			try {
				Proxy proxy = new Proxy();
				proxy.setModel(projectsite);
				proxy.setProject(projectsite.getProject());
				proxy.setSite(projectsite.getSite());
				proxy.buildViewModel();
				return proxy;
			} catch (DaoException daoe){
				return null;
			}
		}
		
	}
	
}
