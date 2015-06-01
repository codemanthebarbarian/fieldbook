package com.amecfw.sage.fieldbook;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.ui.CancelSaveExitDialog;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.vegetation.rareplant.StationListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class StationManager<TEditFragment extends StationEditFragmentBase> extends Activity
        implements ViewState.ViewStateListener, StationListFragment.OnItemSelectedHandler {
    public static final String EXTRA_PROJECT_SITE_ID = "fieldbook.StationManager.projectSite";
    protected static final String ARG_CONTAINER_STATE = "fieldbook.StationManager.containerState";
    protected static final String ARG_VIEW_STATE = "fieldbook.StationManager.viewState";
    protected static final String ARG_STATIONS_CACHE = "fieldbook.StationManager.stations";


    protected static final int CONTAINER_STATE_ONE = 1;
    protected static final int CONTAINER_STATE_TWO = 2;

    protected StationProxy stationProxy;
    protected int containerState;
    protected ViewState viewState;
    protected ProjectSite projectSite;
    protected List<Station> stations;
    protected MenuItem addBtn;
    protected MenuItem deleteBtn;
    protected MenuItem saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SageApplication.getInstance().getThemeID());
        this.setContentView(R.layout.station_management);
        containerState = findViewById(R.id.rareplant_stationManagement_containerB) == null ? CONTAINER_STATE_ONE : CONTAINER_STATE_TWO;
        if(savedInstanceState == null) {
            initialize(getIntent().getExtras());
            setIntitalView(getIntent().getExtras());
        }
        else intializeFromSavedInstance(savedInstanceState);
        viewState.addListener(this);
    }

    protected void setIntitalView(Bundle savedInstanceState){
        viewState = ViewState.getViewStateView();
        viewState.addListener(this);
        stations = getStations();
        if (stations == null) stations = new ArrayList<>();
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

    protected void initialize(Bundle args){
        if(args == null) return;
        DaoSession session = SageApplication.getInstance().getDaoSession();
        projectSite = new ProjectSiteServices(session).getProjectSite(args.getLong(EXTRA_PROJECT_SITE_ID));
        stations = getStations();
        viewState = ViewState.getViewStateView();
    }

    protected void intializeFromSavedInstance(Bundle savedInstanceState){
        DaoSession session = SageApplication.getInstance().getDaoSession();
        viewState = savedInstanceState.getParcelable(ARG_VIEW_STATE);
        containerState = savedInstanceState.getInt(ARG_CONTAINER_STATE, CONTAINER_STATE_TWO);
        projectSite = new ProjectSiteServices(session).getProjectSite(savedInstanceState.getLong(EXTRA_PROJECT_SITE_ID));
    }

//    protected void loadEditFragment(){
//        Fragment f = getFragmentManager().findFragmentByTag(getEditFragmentClass().getName());
//        stationProxy = null;
//        if(f== null){
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//            TEditFragment editFragment = getEditFragmentInstance();
//            Bundle bundle = new Bundle();
//            bundle.putParcelable(TransectEditFragment.ARV_VIEW_STATE, ViewState.getViewStateAdd());
//            transaction.add(R.id.rareplant_stationManagement_containerB, editFragment, getEditFragmentClass().getName());
//            transaction.commit();
//            saveBtn.setVisible(true);
//        } else {
//            Bundle args = new Bundle();
//            args.putInt(ActionEvent.ARG_COMMAND, StationEditFragmentBase.COMMAND_NOTIFY_NEW);
//            ((TransectEditFragment) f).actionPerformed(ActionEvent.getActionDoCommand(args));
//        }
//        viewState.setStateAdd();
//    }

    /**
     * Used internally to load new fragments.
     * @return a new instance of a TEditFragments
     */
    protected abstract TEditFragment getEditFragmentInstance();

    /**
     * Used internally to get the edit fragments by class name in the transaction manager.
     * @return the class for the TEditFragments
     */
    protected abstract Class<TEditFragment> getEditFragmentClass();

    /**
     * Saves the current station in the station edit fragment
     */
    protected abstract void doSave();

    /**
     * Shows the edit fragment to allow the user to add a new station
     */
    protected void doAdd(){
        if(isDirty()) displayAddNewCancelSaveDialog();
        else showAdd();
    }

    protected void showAdd(){
        Fragment f = getFragmentManager().findFragmentByTag(getEditFragmentClass().getName());
        stationProxy = null;
        if(f== null){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            TEditFragment editFragment = getEditFragmentInstance();
            Bundle bundle = new Bundle();
            bundle.putParcelable(StationEditFragmentBase.ARV_VIEW_STATE, ViewState.getViewStateAdd());
            transaction.add(R.id.rareplant_stationManagement_containerB, editFragment, getEditFragmentClass().getName());
            transaction.commit();
            saveBtn.setVisible(true);
        }
        else {
            Bundle args = new Bundle();
            args.putInt(ActionEvent.ARG_COMMAND, StationEditFragmentBase.COMMAND_NOTIFY_NEW);
            ((TEditFragment) f).actionPerformed(ActionEvent.getActionDoCommand(args));
        }
        viewState.setStateAdd();
    }

    /**
     * deletes the current station in the station edit fragment
     */
    protected abstract void doDelete();

    protected com.amecfw.sage.util.OnItemSelectedHandler<Station> stationLongClickSelectedHandler = new com.amecfw.sage.util.OnItemSelectedHandler<Station>(){
        @Override
        public void onItemSelected(Station item) {
            if(item != null) doEdit(item);
        }
    };

    protected void doEdit(Station station){
        //show the selected item for edit
        StationService ss = new StationService(SageApplication.getInstance().getDaoSession());
        stationProxy = ss.getStationProxy(station);
        Bundle args = new Bundle();
        Fragment f =  getFragmentManager().findFragmentByTag(getEditFragmentClass().getName());
        if(f == null){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            TEditFragment editFragment = getEditFragmentInstance();
            editFragment.setPhotos(stationProxy.getPhotos());
            args.putParcelable(StationEditFragmentBase.ARV_VIEW_STATE, ViewState.getViewStateEdit());
            args.putParcelable(StationEditFragmentBase.ARG_VIEW_MODEL, editFragment.createViewModel(stationProxy));
            editFragment.setArguments(args);
            transaction.add(R.id.rareplant_stationManagement_containerB, editFragment, getEditFragmentClass().getName());
            transaction.commit();
        }else{
            TEditFragment editFragment = (TEditFragment) f;
            if(isDirty());//TODO: should prompt user to save changes
            if(cancel) return;
            args.putInt(ActionEvent.ARG_COMMAND, StationEditFragmentBase.COMMAND_EDIT);
            editFragment.actionPerformed(ActionEvent.getActionDoCommand(args));
            args.putParcelable(StationEditFragmentBase.ARG_VIEW_MODEL, editFragment.createViewModel(stationProxy));
            editFragment.setPhotos(stationProxy.getPhotos());
        }
        saveBtn.setVisible(true);
    }

    /**
     * Cancel any editing currently being done without saving chages
     */
    protected void doCancel(){
        Fragment f = getFragmentManager().findFragmentByTag(getEditFragmentClass().getName());
        if(f != null){
            PhotoService.clearTemp(((TEditFragment) f).getPhotos());
        }
        if(stationProxy != null){
            PhotoService.clearTemp(stationProxy.getPhotos());
        }
    }

    /**
     * is the current item in the station edit fragment in a changed state that
     * has not been saved to the database
     * @return true if the station has unsaved changes otherwise false
     */
    protected boolean isDirty(){
        Fragment f = getFragmentManager().findFragmentByTag(getEditFragmentClass().getName());
        if(f == null) return false;
        return ((TEditFragment)f).isDirty();
    }

    /**
     * Used in updateStationList to get the stationtypes managed by the implementing activity
     * @return the string representing the station types
     */
    protected abstract String getStationType();

    /**
     * updates the stations in the station list fragment from the database using
     * getStations() in the StationService.
     * if using mixed station types, override
     */
    protected void updateStationList(){
        stations = getStations();
        Fragment f = getFragmentManager().findFragmentByTag( StationListFragment.class.getName());
        if(f != null) ((StationListFragment) f).setStations(stations);
    }

    /**
     * Gets the stations corrosponding to the survey.
     * The default uses StationService.find(projectSite, getStationType())
     * In you need stations based on a different query, override this method.
     * @return a list of stations
     */
    protected List<Station> getStations(){
        return new StationService(SageApplication.getInstance().getDaoSession()).find(projectSite, getStationType());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // StateManagement

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_VIEW_STATE, viewState);
        outState.putInt(ARG_CONTAINER_STATE, containerState);
        if(projectSite != null) outState.putLong(EXTRA_PROJECT_SITE_ID, projectSite.getId());
    }

    @Override
    public void onStateChange(int previousState, int newState) {

        switch (newState){
            case ViewState.EDIT:
                break;
            case ViewState.ADD:
                break;
            case ViewState.VIEW:
                //loadEditFragment();
                break;
        }
    }

    // END StateManagement
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Exit handling

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

    // END Exit Handling
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
