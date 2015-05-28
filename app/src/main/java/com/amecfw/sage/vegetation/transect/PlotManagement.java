package com.amecfw.sage.vegetation.transect;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.fieldbook.StationManager;
import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.LocationService;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.util.ApplicationUI;
import com.amecfw.sage.util.CollectionOperations;
import com.amecfw.sage.vegetation.VegetationGlobals;
import com.amecfw.sage.vegetation.rareplant.CategorySurvey;

import java.util.List;

/**
 * Created by amec on 2015-05-27.
 */
public class PlotManagement extends StationManager<PlotEditFragment> {

    public static final String ARG_TRANSECT_CACHE = "vegetation.transect.PlotManagement.transectProxy";
    private Station transect;
    private StationProxy root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //need the transect before super.onCreate
        if(savedInstanceState == null) transect = SageApplication.getInstance().removeItem(getIntent().getExtras().getString(ARG_TRANSECT_CACHE));
        else transect = SageApplication.getInstance().removeItem(ARG_TRANSECT_CACHE);
        super.onCreate(savedInstanceState);
        this.getActionBar().setIcon(R.drawable.leaf);
        setTitle(getResources().getString(R.string.transectPlot_Title, transect.getName()));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SageApplication.getInstance().setItem(ARG_TRANSECT_CACHE, transect);
    }

    @Override
    protected PlotEditFragment getEditFragmentInstance() {
        return new PlotEditFragment();
    }

    @Override
    protected Class<PlotEditFragment> getEditFragmentClass() {
        return PlotEditFragment.class;
    }

    @Override
    protected void doSave() {
        Fragment f = getFragmentManager().findFragmentByTag(getEditFragmentClass().getName());
        if(f == null) return;
        PlotEditFragment.ViewModel viewModel = ((PlotEditFragment)f).getViewModel();
        List<PhotoProxy> photos = ((PlotEditFragment)f).getPhotos();
        updateProxy(viewModel, photos);
        if(viewModel.location != null) stationProxy.setGpsLocation(viewModel.location);
        StationService stationService = new StationService(SageApplication.getInstance().getDaoSession());
        if(stationService.saveOrUpdateInTransaction(stationProxy)) updateStationList();
        viewState.setStateView();
        saveBtn.setVisible(false);
        ApplicationUI.hideSoftKeyboard(this);
    }

    private void updateProxy(PlotEditFragment.ViewModel viewModel, List<PhotoProxy> photos){
        if(stationProxy == null) stationProxy = new StationProxy();
        if(stationProxy.getProjectSite() == null) stationProxy.setProjectSite(projectSite);
        if(stationProxy.getRoot() == null) {
            if(root == null) root = new StationService(SageApplication.getInstance().getDaoSession()).getStationProxy(transect);
            stationProxy.setRoot(root);
        }
        StationService.updateFromViewModel(stationProxy, viewModel, photos);
        if(stationProxy.getLocationProxy() == null) {
            stationProxy.setLocationProxy(LocationService.createProxyFromLocations(CollectionOperations.createList(viewModel.location)
                    , new Location(), LocationService.FEATURE_TYPE_POINT));
            stationProxy.getLocationProxy().setSite(transect.getProjectSite().getSite());
        } else {
            stationProxy.getLocationProxy().setLocations(CollectionOperations.createList(viewModel.location));
        }
        stationProxy.getLocationProxy().getModel().setName(stationProxy.getModel().getName());
    }

    @Override
    protected void doDelete() {

    }

    @Override
    protected String getStationType() {
        return VegetationGlobals.STATION_TYPE_VEGETATION_PLOT;
    }

    @Override
    protected List<Station> getStations(){
        return new StationService(SageApplication.getInstance().getDaoSession()).getSubstations(transect, getStationType());
    }

    @Override
    public void onItemSelected(Station station) {
        if (station == null)return;
        SageApplication.getInstance().setItem(CategorySurvey.ARG_STATION, station);
        Intent intent = new Intent(this, CategorySurvey.class);
        intent.putExtra(CategorySurvey.ARG_STATION, CategorySurvey.ARG_STATION);
        startActivity(intent);
    }
}
