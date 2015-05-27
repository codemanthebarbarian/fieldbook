package com.amecfw.sage.vegetation.transect;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.ApplicationUI;
import com.amecfw.sage.util.CollectionOperations;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.vegetation.VegetationGlobals;

import java.util.List;

/**
 *
 */
public class TransectManagement extends StationManager<TransectEditFragment> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getActionBar().setIcon(R.drawable.leaf);
    }

    @Override
    protected TransectEditFragment getEditFragmentInstance() {
        return new TransectEditFragment();
    }

    @Override
    protected Class<TransectEditFragment> getEditFragmentClass() {
        return TransectEditFragment.class;
    }

    @Override
    protected void doSave() {
        Fragment f = getFragmentManager().findFragmentByTag(getEditFragmentClass().getName());
        if(f == null) return;
        TransectEditFragment.ViewModel viewModel = ((TransectEditFragment)f).getViewModel();
        List<PhotoProxy> photos = ((TransectEditFragment)f).getPhotos();
        updateProxy(viewModel, photos);
        if(viewModel.location != null) stationProxy.setGpsLocation(viewModel.location);
        StationService stationService = new StationService(SageApplication.getInstance().getDaoSession());
        if(stationService.saveOrUpdateInTransaction(stationProxy)) updateStationList();
        viewState.setStateView();
        saveBtn.setVisible(false);
        ApplicationUI.hideSoftKeyboard(this);
    }

    private void updateProxy(TransectEditFragment.ViewModel viewModel, List<PhotoProxy> photos){
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

    @Override
    protected void doDelete() {

    }

    @Override
    protected String getStationType() {
        return VegetationGlobals.SURVEY_TRANSECT;
    }


}
