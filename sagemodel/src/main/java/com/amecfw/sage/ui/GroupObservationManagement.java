package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.Owner;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.ObservationService;
import com.amecfw.sage.model.service.OwnerService;
import com.amecfw.sage.proxy.GroupObservationProxy;
import com.amecfw.sage.proxy.ObservationGroupProxy;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.ViewState;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A generic activity that can be used for the management of Observation Groups. This uses the fragments 
 * for group management otherwise a component can orchestrate the use of those fragments to complete the task
 * or group management.
 * An owner can be provided to only retrieve only those groups belonging to the provided owner
 * The com.amecfw.sage.R.view id can be provided to set the initial view. 
 * If not provided com.amecfw.sage.R.layout.observation_groups_list is used
 */
public class GroupObservationManagement extends Activity implements ObservationGroupsList.OnItemSelectedHandler {
	
	/** the key for Passing the viewstate for the activity */
	public static final String EXTRA_VIEWSTATE = "sage.ui.GroupObservationManagement.viewState";
	/** The key for passing the owner as parcelable (must be Owner object) */
	public static final String EXTRA_OWNER = "sage.ui.GroupObservationManagement.owner";
	/** The key for passing the owner's key in the SageApplication Cache (should be string)*/
	public static final String EXTRA_OWNER_CACHE_KEY = "sage.ui.GroupObservationManagement.ownerCacheKey";
	/** The key for passing the owner's id to get from the database (must be long) */
	public static final String EXTRA_OWNER_ID = "sage.ui.GroupObservationManagement.ownerID";
	/** The key for the id in the sage domain R file for the initial fragment 
	 * The Value must be either VIEW_OBSERVATION_GROUPS or VIEW_OBSERVATION_GROUP_EDIT 
	 * (default is VIEW_OBSERVATION_GROUPS if not specified or not either of the two values) */
	public static final String EXTRA_INITIAL_VIEW = "sage.ui.GroupObservationManagement.fragment";
	/** the key for the id of the observation group to display (must be long) */
	public static final String EXTRA_OBSERVATION_GROUP_ID = "sage.ui.GroupObservationManagement.observationGroupID";
	/** the key for passing the observation group key in the SageApplication Cache (should be string) */
	public static final String EXTRA_OBSERVATION_GROUP_CACHE_KEY = "sage.ui.GroupObservationManagement.observationGroupCacheKey";
	
	public static final int VIEW_OBSERVATION_GROUPS = 0;
	public static final int VIEW_OBSERVATION_GROUP_EDIT = 1;
	private static final int VIEW_GROUP_OBSERVATIONS = 2;
	private static final int VIEW_GROUP_OBSERVATION_EDIT = 3;
	
	/** observation list in container A */
	private static final int STATE_GROUPS_LIST = VIEW_OBSERVATION_GROUPS;
	/** observation group edit in container A and group observations list in container B */
	private static final int STATE_GROUP_EDIT_GROUPOBS = 21;
	/** observation group edit in container A and group observation edit in container B */
	private static final int STATE_GROUP_EDIT_GROUPOB_EDIT = 31;
	
	
	private static final String KEY_PROXY = "sage.ui.GroupObservationManagement.proxy";
	private static final String KEY_VIEW_STACK = "sage.ui.GroupObservationManagement.viewStack";
	
	private ViewState viewState;
	private Stack<Integer> viewStack;
	private int initialView;
	private ObservationGroupProxy proxy;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		super.onCreate(savedInstanceState);
		setTheme(SageApplication.getInstance().getThemeID());
		viewStack = new Stack<Integer>();
		setContentView(R.layout.observation_group_management);
		if(savedInstanceState == null) initialize(getIntent().getExtras());
	}	
	
	@Override
	protected void onDestroy() {
		SageApplication.getInstance().remove(KEY_PROXY);
		super.onDestroy();
	}

	private void initialize(Bundle bundle){
		proxy = new ObservationGroupProxy();
		if(bundle == null) {
			initialView = android.R.layout.list_content;
		} else {
			viewState = bundle.getParcelable(EXTRA_VIEWSTATE);
			proxy.setOwner(getOwner(bundle));
			initialView = bundle.getInt(EXTRA_INITIAL_VIEW, VIEW_OBSERVATION_GROUPS);
		}
		setView(initialView);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		viewState = savedInstanceState.getParcelable(EXTRA_VIEWSTATE);
		proxy = SageApplication.getInstance().getItem(KEY_PROXY);
		if(proxy == null) proxy = new ObservationGroupProxy();
		proxy.setOwner(getOwner(savedInstanceState));
		viewStack = Convert.toStack(savedInstanceState.getIntArray(KEY_VIEW_STACK));
		initialView = savedInstanceState.getInt(EXTRA_INITIAL_VIEW, VIEW_OBSERVATION_GROUPS);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(EXTRA_VIEWSTATE, viewState);
		if(proxy.getOwner() != null) outState.putLong(EXTRA_OWNER_ID, proxy.getOwner().getId());
		outState.putInt(EXTRA_INITIAL_VIEW, initialView);
		outState.putIntArray(KEY_VIEW_STACK, Convert.toPrimitive(viewStack.toArray(new Integer[outState.size()])));
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_delete_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.addDeleteMenu_add){
			doAdd();
			return true;
		}
		if(id == R.id.addDeleteMenu_save){
			doSave();
			return true;
		}
		if(id == R.id.addDeleteMenu_delete){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if(viewStack.size() < 2) super.onBackPressed();//At view start so close the activity
		else{
			//no special case, pop stack and back to previous state
			CancelSaveExitDialog dialog = new CancelSaveExitDialog();
			dialog.setListener(cancelSaveExitDialogListener);
			dialog.show(getFragmentManager(), CancelSaveExitDialog.class.getName());
		}
	}
	
	@Override
	public boolean onNavigateUp(){
		if(viewStack.size() < 2){ 
			super.onBackPressed();
			return true;
		}else{
			CancelSaveExitDialog dialog = new CancelSaveExitDialog();
			dialog.setListener(cancelSaveExitDialogListener);
			dialog.show(getFragmentManager(), CancelSaveExitDialog.class.getName());
			return false;
		}
	}

	private Owner getOwner(Bundle bundle){
		Owner result = null;
		if(bundle.containsKey(EXTRA_OWNER_ID))
			result = new OwnerService(SageApplication.getInstance().getDaoSession()).get(bundle.getLong(EXTRA_OWNER_ID, 0));
		if(result == null && bundle.containsKey(EXTRA_OWNER_CACHE_KEY))
			result = SageApplication.getInstance().getItem(EXTRA_OWNER_CACHE_KEY);
		if(result == null && bundle.containsKey(EXTRA_OWNER))
			result = bundle.getParcelable(EXTRA_OWNER);
		return result;
	}

	private Fragment getGroupsList(){
		ObservationGroupsList fragment = new ObservationGroupsList();
		if(proxy.getOwner() != null) {
			Bundle args = new Bundle();
			args.putParcelable(ObservationGroupsList.EXTRA_OWNER, proxy.getOwner());
			fragment.setArguments(args);
		}
		fragment.setOnItemSelectedHandler(this);
		return fragment;
	}
	
	private void setView(int view){
		switch(view){
		case VIEW_OBSERVATION_GROUPS:
			loadFragment(getGroupsList(), initialView, ObservationGroupsList.class.getName());
			break;
		}
	}
	
	private void loadFragment(Fragment fragment, int view, String tag){
		getFragmentManager().beginTransaction().add(R.id.observationGroupManagementActivity_A, fragment, tag).commit();
		viewStack.push(view);
	}
	
	private void doAdd(){
		if(viewStack.peek() == STATE_GROUPS_LIST){
			//in observation groups, need to add a new group
			ObservationGroupEditFragment ogFragment = new ObservationGroupEditFragment();
			Bundle argsA = new Bundle();
			argsA.putParcelable(ObservationGroupEditFragment.ARG_VIEWSTATE, ViewState.getViewStateAdd());
			argsA.putParcelable(ObservationGroupEditFragment.ARG_VIEW_MODEL, new ObservationGroupEditFragment.ViewModel());
			ogFragment.setArguments(argsA);
			FragmentTransaction trans =  getFragmentManager().beginTransaction()
					.replace(R.id.observationGroupManagementActivity_A, ogFragment, ogFragment.getClass().getName());
			GroupObservationsList goListFragment = new GroupObservationsList();
			goListFragment.setOnItemSelectedHandler(groupObservationsOnItemSelectedHandler);
			if(proxy != null) goListFragment.setProxies(proxy.getGroupObservations());
			trans.add(R.id.observationGroupManagementActivity_B, goListFragment, goListFragment.getClass().getName());
			trans.addToBackStack(null);
			trans.commit();
			viewStack.push(STATE_GROUP_EDIT_GROUPOBS);
		} else if(viewStack.peek() == STATE_GROUP_EDIT_GROUPOBS){
			//allow user to add
			GroupObservationEditFragment fragment = new GroupObservationEditFragment();
			Bundle args = new Bundle();
			args.putParcelable(GroupObservationEditFragment.ARG_VIEW_STATE, ViewState.getViewStateAdd());
			fragment.setArguments(args);
			fragment.setOnExitListener(groupObservationEditOnExitListener);
			FragmentTransaction trans = getFragmentManager().beginTransaction();
			trans.replace(R.id.observationGroupManagementActivity_B, fragment, fragment.getClass().getName());
			trans.addToBackStack(null);
			trans.commit();
			viewStack.push(STATE_GROUP_EDIT_GROUPOB_EDIT);
		}
	}
	
	private GroupObservationEditFragment.OnExitListener groupObservationEditOnExitListener =
			new GroupObservationEditFragment.OnExitListener(){
				@Override
				public void onExit(GroupObservationEditFragment.ViewModel viewModel, ViewState viewState) {
					addGroupObservation(viewModel);
					onBackPressed();
					viewStack.pop();
				}		
	};
	
	private void addGroupObservation(GroupObservationEditFragment.ViewModel viewModel){
		if(viewModel == null) return;
		if(proxy == null) proxy = new ObservationGroupProxy();
		proxy.addGroupObservation(viewModel);
	}
	
	private void doPop(){
		viewStack.pop();
		super.onBackPressed();
	}
	
	private void doSave(){
		if(viewStack.isEmpty()) return;
		if(proxy == null) proxy = new ObservationGroupProxy();
		if(viewStack.peek() == STATE_GROUP_EDIT_GROUPOBS){
			Fragment fragment = getFragmentManager().findFragmentByTag(ObservationGroupEditFragment.class.getName());
			if(fragment != null){
				ObservationGroupEditFragment.ViewModel vm = ((ObservationGroupEditFragment)fragment).getViewModel();
				proxy.update(vm);
			}
		}else if(viewStack.peek() == STATE_GROUP_EDIT_GROUPOB_EDIT){
			Fragment fragment = getFragmentManager().findFragmentByTag(GroupObservationEditFragment.class.getName());
			if(fragment != null){
				GroupObservationEditFragment.ViewModel vm = ((GroupObservationEditFragment)fragment).getViewModel();
				proxy.addGroupObservation(vm);
				Fragment groupObsList = getFragmentManager().findFragmentByTag(GroupObservationsList.class.getName());
				if(groupObsList != null) ((GroupObservationsList)groupObsList).setProxies(proxy.getGroupObservations());
			}
		}
	}
	
	private GroupObservationsList.OnItemSelectedHandler groupObservationsOnItemSelectedHandler = new GroupObservationsList.OnItemSelectedHandler() {		
		@Override
		public void onItemSelected(GroupObservationProxy groupObservationProxy) {
			GroupObservationEditFragment fragment = new GroupObservationEditFragment();
			Bundle args = new Bundle();
			args.putParcelable(GroupObservationEditFragment.ARG_VIEW_STATE, ViewState.getViewStateEdit());
			GroupObservationEditFragment.ViewModel vm = new GroupObservationEditFragment.ViewModel();
			vm.setAllowableValues(new ArrayList<String>(Arrays.asList(ObservationService.parseAllowableValues(groupObservationProxy.getModel(), ",", true))));
			vm.setTypeName(groupObservationProxy.getObservationType().getName());
			args.putParcelable(GroupObservationEditFragment.ARG_VIEW_MODEL, vm);
			fragment.setArguments(args);
			fragment.setOnExitListener(groupObservationEditOnExitListener);
			FragmentTransaction trans = getFragmentManager().beginTransaction();
			trans.replace(R.id.observationGroupManagementActivity_B, fragment, fragment.getClass().getName());
			trans.addToBackStack(null);
			trans.commit();
			viewStack.push(STATE_GROUP_EDIT_GROUPOB_EDIT);
		}
	};
	
	private CancelSaveExitDialog.Listener cancelSaveExitDialogListener = new CancelSaveExitDialog.Listener() {		
		@Override
		public void onSave(CancelSaveExitDialog dialog) {
			doSave();
			doPop();
		}		
		@Override
		public void onExit(CancelSaveExitDialog dialog) {
			doPop();			
		}		
		@Override
		public void onCancel(CancelSaveExitDialog dialog) { 
			//Do nothing, 
		}
	};

	@Override
	public void OnItemSelected(ObservationGroup observationGroup) {
		proxy.setModel(observationGroup);
	}
			
}
