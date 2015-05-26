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
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.ui.CancelSaveExitDialog;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.vegetation.rareplant.StationListFragment;
import com.amecfw.sage.vegetation.transect.TransectEditFragment;

import java.util.List;

/**
 * Created by amec on 2015-05-21.
 */
public abstract class StationManager<TEditFragment extends StationEditFragmentBase> extends Activity implements ViewState.ViewStateListener {
    public static final String EXTRA_PROJECT_SITE_ID = "fieldbook.StationManager.projectSite";
    protected static final String ARG_CONTAINER_STATE = "fieldbook.StationManager.containerState";
    protected static final String ARG_VIEW_STATE = "fieldbook.StationManager.viewState";


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
        if(savedInstanceState == null) initialize(getIntent().getExtras());
        else intializeFromSavedInstance(savedInstanceState);
        viewState.addListener(this);
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
        stations = new StationService(session).find(projectSite);
        viewState = ViewState.getViewStateView();
    }

    protected void intializeFromSavedInstance(Bundle savedInstanceState){
        viewState = savedInstanceState.getParcelable(ARG_VIEW_STATE);
        containerState = savedInstanceState.getInt(ARG_CONTAINER_STATE, CONTAINER_STATE_TWO);
    }

    protected void loadEditFragment(){
        Fragment f = getFragmentManager().findFragmentByTag(TransectEditFragment.class.getName());
        stationProxy = null;
        if(f== null){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            TEditFragment editFragment = new TEditFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(TransectEditFragment.ARV_VIEW_STATE, ViewState.getViewStateAdd());
            transaction.add(R.id.rareplant_stationManagement_containerB, editFragment, TransectEditFragment.class.getName());
            transaction.commit();
            saveBtn.setVisible(true);
        }
        else {
            Bundle args = new Bundle();
            args.putInt(ActionEvent.ARG_COMMAND, TransectEditFragment.COMMAND_NOTIFY_NEW);
            ((TransectEditFragment) f).actionPerformed(ActionEvent.getActionDoCommand(args));
        }
        viewState.setStateAdd();
    }

    /**
     * Saves the current station in the station edit fragment
     */
    protected abstract void doSave();

    /**
     * Shows the edit fragment to allow the user to add a new station
     */
    protected abstract void doAdd();

    /**
     * deletes the current station in the station edit fragment
     */
    protected abstract void doDelete();

    /**
     * display the provided station in the station edit fragment for editing
     * @param station the station to edit
     */
    protected abstract void doEdit(Station station);

    private void doEdit(Class<TEditFragment> editFragmentClass, Station station){
        //show the selected item for edit
        StationService ss = new StationService(SageApplication.getInstance().getDaoSession());
        stationProxy = ss.getStationProxy(station);
        //convert to viewmodel
        StationEditFragment.ViewModel vm = new StationEditFragment.ViewModel();
        CategorySurveyService.updateFromProxy(stationProxy, vm, SageApplication.getInstance().getDaoSession());
        Bundle args = new Bundle();
        args.putParcelable(StationEditFragment.ARG_VIEW_MODEL, vm);
        Fragment f =  getFragmentManager().findFragmentByTag(editFragmentClass.getName());
        if(f == null){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            TEditFragment editFragment = new StationEditFragment();
            editFragment.setPhotos(stationProxy.getPhotos());
            args.putParcelable(StationEditFragment.ARV_VIEW_STATE, ViewState.getViewStateEdit());
            editFragment.setArguments(args);
            transaction.add(R.id.rareplant_stationManagement_containerB, editFragment, editFragmentClass.getName());
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

    /**
     * Cancel any editing currently being done without saving chages
     */
    protected abstract void doCancel();

    /**
     * is the current item in the station edit fragment in a changed state that
     * has not been saved to the database
     * just make a call to isDirty(Class[TEditFragment])
     * @return true if the station has unsaved changes otherwise false
     */
    protected abstract boolean isDirty();

    /**
     * is the current item in the station edit fragment in a changed state that
     * has not been saved to the database
     * @param editFragment the class type for the edit fragment
     * @return true if the station has unsaved changes otherwise false
     */
    protected boolean isDirty(Class<TEditFragment> editFragment){
        Fragment f = getFragmentManager().findFragmentByTag(editFragment.getName());
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
     * find(ProjectSite projectSite, String stationType) in the StationService.
     * if using mixed station types, override
     */
    protected void updateStationList(){
        stations = new StationService(SageApplication.getInstance().getDaoSession()).find(projectSite, getStationType());
        Fragment f = getFragmentManager().findFragmentByTag( StationListFragment.class.getName());
        if(f != null) ((StationListFragment) f).setStations(stations);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // StateManagement

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_VIEW_STATE, viewState);
        outState.putInt(ARG_CONTAINER_STATE, containerState);
        outState.putLong(EXTRA_PROJECT_SITE_ID, projectSite.getId());
    }

    @Override
    public void onStateChange(int previousState, int newState) {

        switch (newState){
            case ViewState.EDIT:
                break;
            case ViewState.ADD:
                break;
            case ViewState.VIEW:
                loadEditFragment();
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
            doAdd();
        }

        @Override
        public void onExit(CancelSaveExitDialog dialog) {
            doAdd();
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
