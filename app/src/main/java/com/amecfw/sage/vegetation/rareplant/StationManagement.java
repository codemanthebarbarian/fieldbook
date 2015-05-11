package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.ui.CancelSaveExitDialog;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.ApplicationUI;
import com.amecfw.sage.util.CollectionOperations;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.vegetation.VegetationGlobals;
import com.amecfw.sage.model.service.LocationService;
import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.proxy.StationProxy;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class StationManagement extends Activity implements ViewState.ViewStateListener, StationListFragment.OnItemSelectedHandler {
	
	public static final String EXTRA_PROJECT_SITE_ID = "vegetation.rareplant.StationManagement.projectSite";
	private static final String ARG_PROXY = "vegetation.rareplant.StationManagement.proxy";
	private static final String ARG_CONTAINER_STATE = "vegetation.rareplant.StationManagement.containerState";
	private static final String ARG_VIEW_STATE = "vegetation.rareplant.StationManagement.viewState";
	private static final String ARG_IS_DIRTY = "vegetation.rareplant.StationManagement.isDirty";
	
	private static final int CONTAINER_STATE_ONE = 1;
	private static final int CONTAINER_STATE_TWO = 2;
	
	private StationProxy stationProxy;
	private StationEditFragment.ViewModel viewModel;
	private ProjectSite projectSite;
	private List<Station> stations;
	private int containerState;
	private ViewState viewState;
	private MenuItem addBtn;
	private MenuItem deleteBtn;
	private MenuItem saveBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(SageApplication.getInstance().getThemeID());
		this.setContentView(R.layout.station_management);
		this.getActionBar().setIcon(R.drawable.leaf);
		this.getActionBar().setTitle(R.string.rarePlantSampleList);
		containerState = findViewById(R.id.rareplant_stationManagement_containerB) == null ? CONTAINER_STATE_ONE : CONTAINER_STATE_TWO;
		if(savedInstanceState == null){
			initialize(getIntent().getExtras());
			setIntitalView(savedInstanceState);
		}
	}
	
	private void initialize(Bundle args){
		if(args == null) return;
		DaoSession session = SageApplication.getInstance().getDaoSession();
		projectSite = new ProjectSiteServices(session).getProjectSite(args.getLong(EXTRA_PROJECT_SITE_ID));
		stations = new StationService(session).find(projectSite);
	}
	
	private void setIntitalView(Bundle savedInstanceState){
		viewState = ViewState.getViewStateView();
		viewState.addListener(this);
		stations = new StationService(SageApplication.getInstance().getDaoSession()).find(projectSite, VegetationGlobals.SURVEY_RARE_PLANT);
		if (stations == null) stations = new ArrayList<Station>();
		StationListFragment stationList = new StationListFragment();
		stationList.setStationLongClickSelectedHandler(stationLongClickSelectedHandler);
		stationList.setStations(stations);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.rareplant_stationManagement_containerA, stationList, StationListFragment.class.getName());
		transaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_delete_menu, menu);
		deleteBtn = menu.findItem(R.id.addDeleteMenu_delete).setVisible(false);
		saveBtn = menu.findItem(R.id.addDeleteMenu_save).setVisible(false);
		return true;
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		viewState = state.getParcelable(ARG_VIEW_STATE);
		viewState.addListener(this);
		projectSite = new ProjectSiteServices(SageApplication.getInstance().getDaoSession()).getProjectSite(state.getLong(EXTRA_PROJECT_SITE_ID));
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(ARG_VIEW_STATE, viewState);
		outState.putLong(EXTRA_PROJECT_SITE_ID, projectSite.getId());
		outState.putInt(ARG_CONTAINER_STATE, containerState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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

	private void showAdd(){
		Fragment f = getFragmentManager().findFragmentByTag(StationEditFragment.class.getName());
		stationProxy = null;
		if(f== null){
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			StationEditFragment editFragment = new StationEditFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable(StationEditFragment.ARV_VIEW_STATE, ViewState.getViewStateAdd());
			transaction.add(R.id.rareplant_stationManagement_containerB, editFragment, StationEditFragment.class.getName());
			transaction.commit();
			saveBtn.setVisible(true);
		}
		else {
			Bundle args = new Bundle();
			args.putInt(ActionEvent.ARG_COMMAND, StationEditFragment.COMMAND_NOTIFY_NEW);
			((StationEditFragment) f).actionPerformed(ActionEvent.getActionDoCommand(args));
		}
		viewState.setStateAdd();
	}
	
	private void doAdd(){
		if(isDirty()) displayAddNewCancelSaveDialog();
		else showAdd();
	}
	
	private void doSave(){
		Fragment f = getFragmentManager().findFragmentByTag(StationEditFragment.class.getName());
		if(f == null) return;
		viewModel = ((StationEditFragment)f).getViewModel();
		List<PhotoProxy> photos = ((StationEditFragment)f).getPhotos();
		updateProxy(viewModel, photos);
		if(viewModel.location != null) stationProxy.setGpsLocation(viewModel.location);
		StationService stationService = new StationService(SageApplication.getInstance().getDaoSession());
		if(stationService.saveOrUpdateInTransaction(stationProxy)) updateStationList();
		viewState.setStateView();
		saveBtn.setVisible(false);
		ApplicationUI.hideSoftKeyboard(this);
	}
	
	private void updateProxy(StationEditFragment.ViewModel viewModel, List<PhotoProxy> photos){
		if(stationProxy == null) stationProxy = new StationProxy();
		if(stationProxy.getProjectSite() == null) stationProxy.setProjectSite(projectSite);
		StationService.updateFromViewModel(stationProxy, viewModel, photos);
		if(stationProxy.getLocationProxy() == null) {
			stationProxy.setLocationProxy(LocationService.createProxyFromLocations(CollectionOperations.createList(viewModel.location)
					, new Location(), LocationService.FEATURE_TYPE_POINT));
			stationProxy.getLocationProxy().setSite(stationProxy.getProjectSite().getSite());
		} else {
			stationProxy.getLocationProxy().setLocations(CollectionOperations.createList(viewModel.location));
		}
		stationProxy.getLocationProxy().getModel().setName(stationProxy.getModel().getName());
	}
	
	private void doDelete(){
		Fragment stnList = getFragmentManager().findFragmentByTag(StationListFragment.class.getName());
		if(stnList != null){
			//((StationListFragment)stnList).set
		}
	}
	
	private com.amecfw.sage.util.OnItemSelectedHandler<Station> stationLongClickSelectedHandler = new com.amecfw.sage.util.OnItemSelectedHandler<Station>(){
		@Override
		public void onItemSelected(Station item) {
			if(item != null) doEdit(item);
		}		
	};
	
	private void doEdit(Station station){
		//show the selected item for edit
		StationService ss = new StationService(SageApplication.getInstance().getDaoSession());
		stationProxy = ss.getStationProxy(station);
		//convert to viewmodel
		StationEditFragment.ViewModel vm = new StationEditFragment.ViewModel();
		CategorySurveyService.updateFromProxy(stationProxy, vm, SageApplication.getInstance().getDaoSession());
		Bundle args = new Bundle();
		args.putParcelable(StationEditFragment.ARG_VIEW_MODEL, vm);
		Fragment f =  getFragmentManager().findFragmentByTag(StationEditFragment.class.getName());
		if(f == null){
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			StationEditFragment editFragment = new StationEditFragment();
			editFragment.setPhotos(stationProxy.getPhotos());
			args.putParcelable(StationEditFragment.ARV_VIEW_STATE, ViewState.getViewStateEdit());
			editFragment.setArguments(args);
			transaction.add(R.id.rareplant_stationManagement_containerB, editFragment, StationEditFragment.class.getName());
			transaction.commit();
		}else{
			StationEditFragment editFragment = (StationEditFragment) f;
			if(isDirty());//TODO: should prompt user to save changes
			if(cancel) return;
			args.putInt(ActionEvent.ARG_COMMAND, StationEditFragment.COMMAND_EDIT);
			editFragment.actionPerformed(ActionEvent.getActionDoCommand(args));
			editFragment.setPhotos(stationProxy.getPhotos());
		}
		saveBtn.setVisible(true);
	}
	
	private void updateStationList(){
		stations = new StationService(SageApplication.getInstance().getDaoSession()).find(projectSite, VegetationGlobals.SURVEY_RARE_PLANT);
		Fragment f = getFragmentManager().findFragmentByTag( StationListFragment.class.getName());
		if(f != null) ((StationListFragment) f).setStations(stations);
	}

	@Override
	public void onStateChange(int previousState, int newState) {
		
		switch (newState){
		case ViewState.EDIT:
			break;
		case ViewState.ADD:
			break;
		case ViewState.VIEW:
			Fragment f = getFragmentManager().findFragmentByTag(StationEditFragment.class.getName());
			if(f != null){
				getFragmentManager().beginTransaction().remove(f).commit();
			}
			break;
		}
	}

	@Override
	public void onItemSelected(Station station) {
		if (station == null)return;
		SageApplication.getInstance().setItem(CategorySurvey.ARG_STATION, station);
		Intent intent = new Intent(this, CategorySurvey.class);
		intent.putExtra(CategorySurvey.ARG_STATION, CategorySurvey.ARG_STATION);
		startActivity(intent);
	}

	private boolean isDirty(){
		Fragment f = getFragmentManager().findFragmentByTag(StationEditFragment.class.getName());
		if(f == null) return false;
		return ((StationEditFragment)f).isDirty();
	}

	private void doCancel(){
		Fragment f = getFragmentManager().findFragmentByTag(StationEditFragment.class.getName());
		if(f != null){
			PhotoService.clearTemp(((StationEditFragment)f).getPhotos());
		}
		if(stationProxy != null){
			PhotoService.clearTemp(stationProxy.getPhotos());
		}
	}

	private CancelSaveExitDialog.Listener addCancelSaveExitListener = new CancelSaveExitDialog.Listener(){
		@Override
		public void onCancel(CancelSaveExitDialog dialog) {
			//Do nothing, just close the dialog
		}

		@Override
		public void onSave(CancelSaveExitDialog dialog) {
			doSave();
			showAdd();
		}

		@Override
		public void onExit(CancelSaveExitDialog dialog) {
			showAdd();
		}
	};

	private void displayAddNewCancelSaveDialog(){
		CancelSaveExitDialog dialog = new CancelSaveExitDialog();
		dialog.setListener(addCancelSaveExitListener);
		dialog.show(getFragmentManager(), CancelSaveExitDialog.class.getName());
	}

	private boolean exit = false;
	private boolean cancel = false;
	private CancelSaveExitDialog.Listener exitCancelSaveExitDialogListener = new CancelSaveExitDialog.Listener() {
		@Override
		public void onSave(CancelSaveExitDialog dialog) {
			doSave();
		}		
		@Override
		public void onExit(CancelSaveExitDialog dialog) {
			doCancel();
			exit = true;
			onNavigateUp();
		}		
		@Override
		public void onCancel(CancelSaveExitDialog dialog) {
			cancel = true;
		}
	};

	private void dispalyCancelSaveDialog(){
		CancelSaveExitDialog dialog = new CancelSaveExitDialog();
		dialog.setListener(exitCancelSaveExitDialogListener);
		dialog.show(getFragmentManager(), CancelSaveExitDialog.class.getName());
	}

	@Override
	public void onBackPressed() {
		if(cancel){
			cancel = false;
			super.onBackPressed();
		} else if (exit){
			super.onBackPressed();
		} else if(isDirty()){
			dispalyCancelSaveDialog();
		} else super.onBackPressed();
	}

	@Override
	public boolean onNavigateUp() {
		if(cancel){
			cancel = false;
			return true;
		} else if(exit){
			return super.onNavigateUp();
		} else if(isDirty()) {
			dispalyCancelSaveDialog();
			return false;
		} 
		return super.onNavigateUp();
	}	
	
}
