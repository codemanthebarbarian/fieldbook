package com.amecfw.sage.fieldbook;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.ui.CancelSaveExitDialog;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.vegetation.rareplant.StationListFragment;

import java.util.List;

/**
 * Created by amec on 2015-05-21.
 */
public abstract class StationManager extends Activity implements ViewState.ViewStateListener {
    public static final String EXTRA_PROJECT_SITE_ID = "fieldbook.StationManager.projectSite";
    protected static final String ARG_CONTAINER_STATE = "fieldbook.StationManager.containerState";
    protected static final String ARG_VIEW_STATE = "fieldbook.StationManager.viewState";


    protected static final int CONTAINER_STATE_ONE = 1;
    protected static final int CONTAINER_STATE_TWO = 2;

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

    protected abstract void loadEditFragment();

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

    /**
     * Cancel any editing currently being done without saving chages
     */
    protected abstract void doCancel();

    /**
     * is the current item in the station edit fragment in a changed state that
     * has not been saved to the database
     * @return true if the station has unsaved changes otherwise false
     */
    protected abstract boolean isDirty();

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
