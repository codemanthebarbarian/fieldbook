package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.model.ElementGroup;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.ElementService;
import com.amecfw.sage.ui.CancelSaveExitDialog;
import com.amecfw.sage.ui.ElementsListDialogFragment;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.OnExitListener;
import com.amecfw.sage.util.OnItemSelectedHandler;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.vegetation.elements.GroupsListDialogFragment;
import com.amecfw.sage.vegetation.elements.SearchActivity;
import com.amecfw.sage.vegetation.rareplant.CategoryFragment.ViewModel;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

public class CategorySurvey extends Activity {
	
	public static final String ARG_VIEW_STATE = "sage.vegetation.CategorySurvey.viewState";
	public static final String ARG_STATION = "sage.vegetation.CategorySurvey.station";
	private static final String ARG_PROXY = "sage.vegetation.CategorySurvey.proxy";
	private static final String ARG_IS_DIRTY = "sage.vegetation.CategorySurvey.isDirty";
	private static final String ARG_CATEGORY = "sage.vegetation.CategorySurvey.category";
	
	private ViewState viewState;
	private CategorySurveyProxy proxy;
	private Switch addEdit;
	private MenuItem saveButton;
	private SearchView searchView;
	private CategoryFragment.ViewModel currentCategory;
	private boolean isDirty;
	

	public void setCategorySurveyProxy(Station station){
		//TODO: null check on station (shouldn't ever be null)
		this.proxy = CategorySurveyService.create(station, getResources().getStringArray(R.array.categorySurvey_categories));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(SageApplication.getInstance().getThemeID());
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.category_survey);
        this.getActionBar().setIcon(R.drawable.leaf);
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.rareplant_categorySurvey_searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        //searchView.setOnQueryTextListener(queryTextListener);
        searchView.setOnQueryTextFocusChangeListener(queryFocusListener);
        if(savedInstanceState != null) loadSavedInstance(savedInstanceState);
        else loadInitialView();
        viewState.addListener(stateListener);
	}

    private void loadSavedInstance(Bundle savedInstanceState){
		viewState = savedInstanceState.getParcelable(ARG_VIEW_STATE);
		proxy = SageApplication.getInstance().removeItem(ARG_PROXY);
		isDirty = savedInstanceState.getBoolean(ARG_IS_DIRTY, true);
		currentCategory = savedInstanceState.getParcelable(ARG_CATEGORY);
		if(viewState == null) viewState = ViewState.getViewStateAdd();
		FragmentManager fm = getFragmentManager();
		Fragment f = fm.findFragmentByTag(CategoryFragment.class.getName());
		if(f != null) ((CategoryFragment)f).setOnCategorySelectedHandler(categorySelectedHandler);
		//f = fm.findFragmentByTag(ElementsListDialogFragment.class.getName());
		//if(f != null) searchView.setOnQueryTextListener(((ElementsListDialogFragment)f).getOnQueryTextListener());
		f = fm.findFragmentByTag(GroupsListDialogFragment.class.getName());
		if(f != null) ((GroupsListDialogFragment)f).setGroupSelectedHandler(elementGroupSelectedListener);
	}
	
	private void loadInitialView(){
		Station station = SageApplication.getInstance().removeItem(getIntent().getStringExtra(ARG_STATION));
		this.proxy = CategorySurveyService.create(station, getResources().getStringArray(R.array.categorySurvey_categories));
		if(proxy == null) {
			proxy = CategorySurveyService.create(new Station(), getResources().getStringArray(R.array.categorySurvey_categories));
		}		
		this.setTitle(String.format(getResources().getString(R.string.categorySurvey_title) , proxy == null || proxy.getModel() == null ? "Unknown" : proxy.getModel().getName()));
		if(viewState == null) viewState = ViewState.getViewStateAdd();
		isDirty = false;
		FragmentTransaction trans = getFragmentManager().beginTransaction();
		//Set the categories
		CategoryFragment categoryFrag = new CategoryFragment();
		categoryFrag.setCategoryViewModels(getCategories());
		categoryFrag.setOnCategorySelectedHandler(categorySelectedHandler);
		trans.add(R.id.rareplant_categroySurvey_containerC, categoryFrag, CategoryFragment.class.getName());
		//Set the groups
		GroupsListDialogFragment groupsFragment = new GroupsListDialogFragment();
		groupsFragment.setElementGroups(getGroups());
		groupsFragment.setGroupSelectedHandler(elementGroupSelectedListener);
		trans.add(R.id.rareplant_categroySurvey_containerA, groupsFragment, GroupsListDialogFragment.class.getName());
		//Set the elements
		ElementsListDialogFragment elementsFragment = new ElementsListDialogFragment();
		elementsFragment.setElements(new ArrayList<Element>());
		trans.add(R.id.rareplant_categroySurvey_containerB, elementsFragment, ElementsListDialogFragment.class.getName());
		trans.commit();
		searchView.setOnQueryTextListener(elementsFragment.getOnQueryTextListener());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		viewState.removeListener(stateListener);
		outState.putParcelable(ARG_VIEW_STATE, viewState);
		outState.putString(ARG_PROXY, ARG_PROXY);
		SageApplication.getInstance().setItem(ARG_PROXY, proxy);
		outState.putBoolean(ARG_IS_DIRTY, isDirty);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categroy_survey_menu, menu);
        addEdit = (Switch) menu.findItem(R.id.categorySurveyMenu_addEditSwitch).getActionView().findViewById(R.id.switchMenuItem);
        addEdit.setOnCheckedChangeListener(addEditListener);
        addEdit.setChecked(viewState.getState() == ViewState.EDIT);
        saveButton = menu.findItem(R.id.addDeleteMenu_save);
        saveButton.setVisible(viewState.getState() == ViewState.EDIT);
        saveButton.setOnMenuItemClickListener(saveMenuItemClickListener);
        return super.onCreateOptionsMenu(menu);
	}
	
	private MenuItem.OnMenuItemClickListener saveMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {		
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			doSave();
			return true;
		}
	};
	
	private void doSave(){
		Fragment f = getFragmentManager().findFragmentByTag(CategoryElementsDialogListFragment.class.getName());
		if(f != null && ((CategoryElementsDialogListFragment)f).isDirty()){ //Get the displayed items and update the proxy
			ArrayList<CategoryElementsListAdapter.ViewModel> elements = ((CategoryElementsDialogListFragment)f).getCategoryElements();
			updateCategory(elements);
			isDirty = true;
		}
		if(! isDirty) return; //Nothing has changed, nothing to save
		//Save the proxy
		boolean saved = CategorySurveyService.saveOrUpdate(proxy);
		isDirty = !saved;
	}

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Search

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener(){
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }
        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private View.OnFocusChangeListener queryFocusListener = new View.OnFocusChangeListener(){
        @Override
        public void onFocusChange(View view, boolean b) {
            if(viewState.getState() == ViewState.ADD && view.getId() == searchView.getId() && b){
                Fragment f = getFragmentManager().findFragmentByTag(ElementsListDialogFragment.class.getName());
                if(f != null){
                    ElementsListDialogFragment ef = (ElementsListDialogFragment) f;
                    ef.setElements(getElements(allGroup));
                    searchView.setOnQueryTextListener(ef.getOnQueryTextListener());
                    ef.setOnItemClickListener(queryItemClickListener);
                }
            }
        }
    };

    private AdapterView.OnItemClickListener queryItemClickListener = new AdapterView.OnItemClickListener () {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            searchView.setOnQueryTextListener(null);
            searchView.clearFocus();
            searchView.setQuery(null, false);
        }
    };

    // END Search
    ///////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////
	// VIEW STATE MANAGEMENT //////////////////////////////////////////////////////////////////////
	
	private OnCheckedChangeListener addEditListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) { 
			if (isChecked) viewState.setStateEdit();
			else viewState.setStateAdd(); 
		}		
	};
	
	private ViewState.ViewStateListener stateListener = new ViewState.ViewStateListener() {
		@Override
		public void onStateChange(int previousState, int newState) {
			onChangeMode(newState);
		}
	};
	
	private void onChangeMode(int newState){
		Fragment fragment;
		switch(newState){
		case ViewState.EDIT:
			fragment = getFragmentManager().findFragmentByTag(CategoryElementsDialogListFragment.class.getName());
			CategoryElementsDialogListFragment categoryElementsFrag = fragment == null ? new CategoryElementsDialogListFragment() :
				(CategoryElementsDialogListFragment) fragment;
			getFragmentManager().beginTransaction().replace(R.id.rareplant_categroySurvey_containerB, categoryElementsFrag, 
					CategoryElementsDialogListFragment.class.getName()).commit();
			saveButton.setVisible(true);
			break;				
		case ViewState.ADD:
			fragment = getFragmentManager().findFragmentByTag(ElementsListDialogFragment.class.getName());
			ElementsListDialogFragment elementsFrag = fragment == null ? new ElementsListDialogFragment() :
				(ElementsListDialogFragment) fragment;
			getFragmentManager().beginTransaction().replace(R.id.rareplant_categroySurvey_containerB, elementsFrag, 
					ElementsListDialogFragment.class.getName()).commit();
			saveButton.setVisible(false);
			break;			
		}
		Toast.makeText(this, newState == ViewState.EDIT ? "Edit Mode" : "Add Mode", Toast.LENGTH_SHORT).show();
	}
	
	// END VIEW STATE MANAGEMENT //////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// CATEGORY MANAGEMENT METHODS ////////////////////////////////////////////////////////////////
	
	private OnItemSelectedHandler<CategoryFragment.ViewModel> categorySelectedHandler = new OnItemSelectedHandler<CategoryFragment.ViewModel>() {
		@Override
		public void onItemSelected(ViewModel item) {
			if(item != null) onCategorySelected(item);
		}
	};	
	
	private void onCategorySelected(CategoryFragment.ViewModel viewModel){
		Toast.makeText(this, viewModel.getCategoryName(), Toast.LENGTH_SHORT).show();
		switch (viewState.getState()){
		case ViewState.ADD:
			updateCategory(viewModel);
			break;
		case ViewState.EDIT:
			loadCategoryElements(viewModel);
			break;
		}
	}
	
	private void updateCategory(ArrayList<CategoryElementsListAdapter.ViewModel> viewModels){
		proxy.updateCanopyElementViewModels(viewModels, currentCategory.getCategoryName());
	}
	
	/**
	 * Updates the selected category with the list of selected elements
	 * @param viewModel
	 */
	private void updateCategory(CategoryFragment.ViewModel viewModel){
		currentCategory = viewModel;
		Fragment fragment = getFragmentManager().findFragmentByTag(ElementsListDialogFragment.class.getName());
		if(fragment != null) {
			List<Element> selectedElements = ((ElementsListDialogFragment)fragment).getCheckedItems();
			int added = proxy.addElements(selectedElements, currentCategory.getCategoryName());
			currentCategory.setElementCount(currentCategory.getElementCount() + added);
			if(!isDirty && added > 0) isDirty = true;
			Bundle args = new Bundle();
			args.putInt(ActionEvent.ARG_COMMAND, CategoryFragment.COMMAND_UPDATE_CATEGORY);
			args.putParcelable(CategoryFragment.ARG_CATEGORY_VIEW_MODEL, currentCategory);
			Fragment cFragment = (CategoryFragment)getFragmentManager().findFragmentByTag(CategoryFragment.class.getName());
			((CategoryFragment)cFragment).actionPerformed(ActionEvent.getActionDoCommand(args));
		}
	}
	
	/**
	 * loads the station elements for the selected category
	 * @param viewModel
	 */
	private void loadCategoryElements(CategoryFragment.ViewModel viewModel){		
		Fragment fragment = getFragmentManager().findFragmentByTag(CategoryElementsDialogListFragment.class.getName());
		if(fragment == null) return;
		CategoryElementsDialogListFragment ceFragment = (CategoryElementsDialogListFragment) fragment;
		if(ceFragment.isDirty()){ // update the previous elements
			proxy.updateCanopyElementViewModels(ceFragment.getCategoryElements(), currentCategory.getCategoryName());
			isDirty = true;
		}
		currentCategory = viewModel;
		//Set the new elements
		((CategoryElementsDialogListFragment) fragment).setCategoryElements(getCategoryElements(currentCategory));
	}
	
	private ArrayList<CategoryElementsListAdapter.ViewModel> getCategoryElements(CategoryFragment.ViewModel category){
		ArrayList<CategoryElementsListAdapter.ViewModel> elements;
		elements = proxy.getCanopyElementViewModels(category.getCategoryName());
		if(elements == null) elements = new ArrayList<CategoryElementsListAdapter.ViewModel>();
		return elements;
	}
	
	private CategoryFragment.ViewModel[] getCategories(){
		String[] names = getResources().getStringArray(R.array.categorySurvey_categories);
 		CategoryFragment.ViewModel[] results = new CategoryFragment.ViewModel[names.length];
		for(int i = 0 ; i < results.length ; i++){
			CategoryFragment.ViewModel vm = new CategoryFragment.ViewModel();
			vm.setCategoryName(names[i]);
			if(proxy != null) vm.setElementCount(proxy.getCanopyElementViewModels(names[i]).size());
			else vm.setElementCount(0);
			results[i] = vm;
		}
		return results;
	}
	
	// END CATEGORY MANAGEMENT METHODS ////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////
	// ELEMENT METHODS ////////////////////////////////////////////////////////////////////////////

    ElementGroup allGroup;

	private List<ElementGroup> getGroups(){
		ElementService es = new ElementService(SageApplication.getInstance().getDaoSession());
		allGroup = es.getAllGroup(); //run to create or update the all group, it will be then included in the next line
		List<ElementGroup> all = new ElementService(SageApplication.getInstance().getDaoSession()).findByOwner(null);
		return all;
	}
	
	private List<Element> getElements(ElementGroup group){
		List<Element> elements = new ElementService(SageApplication.getInstance().getDaoSession()).findElementsByGroup(group);
		return elements;
	}
	
	private OnItemSelectedHandler<ElementGroup> elementGroupSelectedListener = new OnItemSelectedHandler<ElementGroup>(){
		@Override
		public void onItemSelected(ElementGroup item) {
			if(item != null) onGroupSelected(item);
		}		
	};

	private void onGroupSelected(ElementGroup group){
		Toast.makeText(this, group.getName(), Toast.LENGTH_SHORT).show();
		Fragment fragment = getFragmentManager().findFragmentByTag(ElementsListDialogFragment.class.getName());
		if(fragment != null) ((ElementsListDialogFragment)fragment).setElements(getElements(group));
	}
	
	// END ELEMENT METHODS ////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// EXIT METHODS ///////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onStop() {
		if(exitListener != null) exitListener.onExit(proxy, viewState);
		super.onStop();
	}

	private OnExitListener<CategorySurveyProxy> exitListener;
	/** 
	 * An exit listener that will receive the CategorySurveyProxy. This will be fired in 
	 * the Activity's onStop() method
	 * @param exitListener
	 */
	public void setOnExitListener(OnExitListener<CategorySurveyProxy> exitListener){
		this.exitListener = exitListener;
	}
	
	@Override
	public void onBackPressed() {
		if(exit){
			exit = false;
			super.onBackPressed();
		} else if(isDirty){
			CancelSaveExitDialog dialog = new CancelSaveExitDialog(cancelSaveExitDialogListener);
			dialog.show(getFragmentManager(), CancelSaveExitDialog.class.getName());
		} else if (viewState.getState() == ViewState.EDIT){
			addEdit.performClick();
		} else super.onBackPressed();
	}
	
	@Override
	public boolean onNavigateUp(){
		if(exit){
			exit = false;
		}
		else if(isDirty) {
			CancelSaveExitDialog dialog = new CancelSaveExitDialog(cancelSaveExitDialogListener);
			dialog.show(getFragmentManager(), CancelSaveExitDialog.class.getName());
			return false;
		}
		if(viewState.getState() == ViewState.EDIT){
			addEdit.performClick();
			return false;
		}
		return super.onNavigateUp();
	}
	private boolean exit = false;
	private CancelSaveExitDialog.Listener cancelSaveExitDialogListener = new CancelSaveExitDialog.Listener() {	
	
		@Override
		public void onSave(CancelSaveExitDialog dialog) {
			doSave();
			onBackPressed();
		}		
		@Override
		public void onExit(CancelSaveExitDialog dialog) {
			exit = true;
			onBackPressed();		
		}		
		@Override
		public void onCancel(CancelSaveExitDialog dialog) { 
			//Do nothing, 
		}
	};
	
	// END EXIT METHODS ///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////
	
}
