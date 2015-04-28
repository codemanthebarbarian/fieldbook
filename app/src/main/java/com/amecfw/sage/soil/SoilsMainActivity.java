package com.amecfw.sage.soil;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.ProjectSiteMeta;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.MetaDataService;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.ui.ProjectSiteEdit;
import com.amecfw.sage.ui.ProjectSiteList;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.fieldbook.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class SoilsMainActivity extends Activity implements ProjectSiteList.OnItemSelectedHandler   {
	
	private static String ARG_VIEW_STATE = "com.amecfw.sage.soils.SoilsMainActivity.ViewState";
	
	private ProjectSiteEdit.Proxy projectSiteEditProxy;
	private ProjectSiteEdit.ViewModel viewModel;
	private List<ProjectSite> projectSites;
	private ViewState viewState;
	private MenuItem deletebtn;
	private MenuItem savebtn;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(SageApplication.getInstance().getThemeID());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_soils_main);
		getActionBar().setIcon(R.drawable.soils);
		if(viewState == null) viewState = ViewState.getViewStateAdd();
		viewState.addListener(stateListener);
		if(savedInstanceState == null) displayProjectSiteList();
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
	protected void onSaveInstanceState(Bundle outState) {
		viewState.removeListener(stateListener);
		outState.putParcelable(ARG_VIEW_STATE, viewState);
		super.onSaveInstanceState(outState);
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
	
	private void displayProjectSiteList(){
		if(projectSites == null) projectSites = new ArrayList<ProjectSite>();
		ProjectSiteList projectSiteList = new ProjectSiteList();
		projectSiteList.setProjectSite(projectSites);
		Bundle args = new Bundle();
		args.putString(ProjectSiteList.ARG_META_VALUE, SoilsGlobals.DESCRIMINATOR_SOILS);
		projectSiteList.setArguments(args);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
	    transaction.add(R.id.soilsManagement_containerA, projectSiteList, ProjectSiteList.class.getName());
	    transaction.commit();
	}
	
	private ViewState.ViewStateListener stateListener = new ViewState.ViewStateListener() {
		@Override
		public void onStateChange(int previousState, int newState) {
			onChangeMode(newState);
		}
	};
	
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
	
	private void doAdd(){
		Fragment f = getFragmentManager().findFragmentByTag(ProjectSiteEdit.class.getCanonicalName());
		if(f == null) {
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			ProjectSiteEdit projectSiteEdit = new ProjectSiteEdit();
			Bundle args = new Bundle();
			args.putParcelable(ProjectSiteEdit.ARG_VIEWSTATE, ViewState.getViewStateAdd());
			projectSiteEdit.setArguments(args);
			transaction.add(R.id.soilsManagement_containerB, projectSiteEdit, ProjectSiteEdit.class.getName());
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
		updateProjectsList();
		savebtn.setVisible(false);
		//ApplicationUI.hideSoftKeyboard(this);
		viewState.setStateView();
	}
	
	private void updateProjectsList() {
		projectSites = new ProjectSiteServices(SageApplication.getInstance().getDaoSession()).findProjectSites(ProjectSiteList.ARG_META_NAME, ProjectSiteList.ARG_META_VALUE);
		Fragment f = getFragmentManager().findFragmentByTag(ProjectSiteList.class.getName());
		if(f != null){
			((ProjectSiteList)f).setProjectSite(projectSites);
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
	
	private ProjectSiteMeta getDiscriminator(){
		ProjectSiteMeta descriminator = new ProjectSiteMeta();
		descriminator.setName(SoilsGlobals.DESCRIMINATOR_SOILS);
		descriminator.setValue(SoilsGlobals.DESCRIMINATOR_SOILS);
		return descriminator;
	}
	
	@Override
	public void onItemSelected(ProjectSite projectSite) {
		if (projectSite == null) return;
//		Intent intent = new Intent(this, StationManagement.class);
//		intent.putExtra(StationManagement.EXTRA_PROJECT_SITE_ID, projectSite.getId());
//		startActivity(intent);
	}

}
