package com.amecfw.sage.vegetation;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.ProjectSiteMeta;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.MetaDataService;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.ui.ProjectSiteEdit;
import com.amecfw.sage.ui.ProjectSiteList;
import com.amecfw.sage.util.ApplicationUI;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.vegetation.rareplant.StationManagement;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class VegetationMainActivity extends Activity implements ProjectSiteList.OnItemSelectedHandler {
	
	private static String ARG_VIEW_STATE = "VegetationMainActivity.ViewState";
	
	private ProjectSiteEdit.Proxy projectSiteEditProxy;
	private ProjectSiteEdit.ViewModel viewModel;
	private List<ProjectSite> projectSites;
	private ViewState viewState;
	private MenuItem addbtn;
	private MenuItem deletebtn;
	private MenuItem savebtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setTheme(SageApplication.getInstance().getThemeID());
		setContentView(R.layout.activity_vegetation_main);
		getActionBar().setIcon(R.drawable.leaf);
		if(viewState == null) viewState = ViewState.getViewStateAdd();
		viewState.addListener(stateListener);
		if(savedInstanceState == null) displayProjectSiteList();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		viewState.removeListener(stateListener);
		outState.putParcelable(ARG_VIEW_STATE, viewState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_delete_menu, menu);
		deletebtn = menu.findItem(R.id.addDeleteMenu_delete);
		savebtn = menu.findItem(R.id.addDeleteMenu_save);
		deletebtn.setVisible(false);
		savebtn.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == R.id.addDeleteMenu_add){
			doAdd();
			return true;
		}if(id == R.id.addDeleteMenu_save){
			doSave();
			return true;
		}if(id == R.id.addDeleteMenu_delete){
			doDelete();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void doAdd(){
		Fragment f = getFragmentManager().findFragmentByTag(ProjectSiteEdit.class.getCanonicalName());
		if(f == null) {
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			ProjectSiteEdit projectSiteEdit = new ProjectSiteEdit();
			Bundle args = new Bundle();
			args.putParcelable(ProjectSiteEdit.ARG_VIEWSTATE, ViewState.getViewStateAdd());
			projectSiteEdit.setArguments(args);
			transaction.add(R.id.vegetationManagement_containerB, projectSiteEdit, ProjectSiteEdit.class.getName());
			transaction.commit();
			savebtn.setVisible(true);
		}
		else ((ProjectSiteEdit) f).setViewModel(new ProjectSiteEdit().getViewModel());
		viewState.setStateAdd();
	}
	
	private void doDelete() {
		// TODO Auto-generated method stub
		
	}

	private void doSave() {
		Fragment f = getFragmentManager().findFragmentByTag(ProjectSiteEdit.class.getName());
		if(f == null) return;
		viewModel =((ProjectSiteEdit)f).getViewModel();
		if(projectSiteEditProxy == null) projectSiteEditProxy = new ProjectSiteEdit.Proxy();
		projectSiteEditProxy.setViewModel(viewModel);
		saveProxy();
		viewModel = null;
		updateStationList();
		savebtn.setVisible(false);
		ApplicationUI.hideSoftKeyboard(this);
		viewState.setStateView();
	}

	private void updateStationList() {
		projectSites = new ProjectSiteServices(SageApplication.getInstance().getDaoSession()).findProjectSites(ProjectSiteList.ARG_META_NAME, ProjectSiteList.ARG_META_VALUE);
		Fragment f = getFragmentManager().findFragmentByTag(ProjectSiteList.class.getName());
		if(f != null){
			((ProjectSiteList)f).setProjectSite(projectSites);
		}
		
	}

	private ViewState.ViewStateListener stateListener = new ViewState.ViewStateListener() {
		@Override
		public void onStateChange(int previousState, int newState) {
			onChangeMode(newState);
		}
	};
	
	private void displayProjectSiteList(){
		if(projectSites == null) projectSites = new ArrayList<ProjectSite>();
		ProjectSiteList projectSiteList = new ProjectSiteList();
		projectSiteList.setProjectSite(projectSites);
		Bundle args = new Bundle();
		args.putString(ProjectSiteList.ARG_META_VALUE, VegetationGlobals.DESCRIMINATOR_VEGETATION);
		projectSiteList.setArguments(args);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
	    transaction.add(R.id.vegetationManagement_containerA, projectSiteList, ProjectSiteList.class.getName());
	    transaction.commit();
	}

	public void onChangeMode(int newState) {
		switch (newState){
		case ViewState.EDIT:
			break;
		case ViewState.ADD:
			break;
		case ViewState.VIEW:
			Fragment f = getFragmentManager().findFragmentByTag(ProjectSiteEdit.class.getName());
			if(f != null){
				getFragmentManager().beginTransaction().remove(f).commit();
			}
			break;
		}
	}
	
	/**
	 * Saves the proxy to the database, if a project site combination already exists, then update is called
	 * to add the provisioning meta data to identify it as a vegetation project
	 */
	private boolean saveProxy(){
		projectSiteEditProxy.buildModel();
		MetaDataService.MetaSupportExtensionMethods.addUpdate(projectSiteEditProxy.getModel(), getDiscriminator());
		ProjectSiteServices service = new ProjectSiteServices(SageApplication.getInstance().getDaoSession());
		return service.saveOrUpdateInTransaction(projectSiteEditProxy); 
	}
	
	/**
	 * calls saveProxy
	 * @return
	 */
	private boolean updateProxy(){
		return saveProxy();
	}
	
	private ProjectSiteMeta getDiscriminator(){
		ProjectSiteMeta descriminator = new ProjectSiteMeta();
		descriminator.setName(VegetationGlobals.DESCRIMINATOR_VEGETATION);
		descriminator.setValue(VegetationGlobals.DESCRIMINATOR_VEGETATION);
		return descriminator;
	}

	@Override
	public void onItemSelected(ProjectSite projectSite) {
		if (projectSite == null) return;
		Intent intent = new Intent(this, StationManagement.class);
		intent.putExtra(StationManagement.EXTRA_PROJECT_SITE_ID, projectSite.getId());
		startActivity(intent);
	}

	private void doCategorySurvey(ProjectSite projectSite){
		Intent intent = new Intent(this, StationManagement.class);
		intent.putExtra(StationManagement.EXTRA_PROJECT_SITE_ID, projectSite.getId());
		startActivity(intent);
	}

	private void doTransectSurvey(ProjectSite projectSite){
		Intent intent = new Intent(this, StationManagement.class);
	}
}
