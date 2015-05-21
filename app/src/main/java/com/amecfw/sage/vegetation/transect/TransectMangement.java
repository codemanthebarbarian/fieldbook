package com.amecfw.sage.vegetation.transect;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.fieldbook.StationManager;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.ProjectSiteServices;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.util.ViewState;

import java.util.List;

/**
 * Created by amec on 2015-05-21.
 */
public class TransectMangement extends StationManager {

    public static final String EXTRA_PROJECT_SITE_ID = "vegetation.transect.StationManagement.projectSite";
    private static final String ARG_CONTAINER_STATE = "vegetation.transect.StationManagement.containerState";
    private static final String ARG_VIEW_STATE = "vegetation.transect.StationManagement.viewState";


    private static final int CONTAINER_STATE_ONE = 1;
    private static final int CONTAINER_STATE_TWO = 2;

    private int containerState;
    private ViewState viewState;
    private ProjectSite projectSite;
    private List<Station> stations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SageApplication.getInstance().getThemeID());
        this.setContentView(R.layout.station_management);
        this.getActionBar().setIcon(R.drawable.leaf);
        containerState = findViewById(R.id.rareplant_stationManagement_containerB) == null ? CONTAINER_STATE_ONE : CONTAINER_STATE_TWO;
        if(savedInstanceState == null) initialize(getIntent().getExtras());
        else intializeFromSavedInstance(savedInstanceState);
        viewState.addListener(this);
    }

    private void initialize(Bundle args){
        if(args == null) return;
        DaoSession session = SageApplication.getInstance().getDaoSession();
        projectSite = new ProjectSiteServices(session).getProjectSite(args.getLong(EXTRA_PROJECT_SITE_ID));
        stations = new StationService(session).find(projectSite);
        viewState = ViewState.getViewStateView();
    }

    private void intializeFromSavedInstance(Bundle savedInstanceState){
        viewState = savedInstanceState.getParcelable(ARG_VIEW_STATE);
        containerState = savedInstanceState.getInt(ARG_CONTAINER_STATE, CONTAINER_STATE_TWO);
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
                Fragment f = getFragmentManager().findFragmentByTag(TransectEditFragment.class.getName());
                if(f != null){
                    getFragmentManager().beginTransaction().remove(f).commit();
                }
                break;
        }
    }

    // END StateManagement
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
