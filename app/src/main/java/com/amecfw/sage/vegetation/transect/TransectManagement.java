package com.amecfw.sage.vegetation.transect;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.fieldbook.StationEditFragmentBase;
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
import com.amecfw.sage.vegetation.rareplant.StationListFragment;

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
        Fragment f = getFragmentManager().findFragmentById(R.id.rareplant_stationManagement_containerB);
        if(f == null) return;
        StationService stationService = new StationService(SageApplication.getInstance().getDaoSession());
        if(f.getTag().equals(TransectEditFragment.class.getName())) {
            TransectEditFragment.ViewModel viewModel = ((TransectEditFragment) f).getViewModel();
            List<PhotoProxy> photos = ((TransectEditFragment) f).getPhotos();
            updateProxy(viewModel, photos);
            if (viewModel.location != null) stationProxy.setGpsLocation(viewModel.location);
            if (stationService.saveOrUpdateInTransaction(stationProxy)) updateStationList();
        } else if(f.getTag().equals(TransectEndEditFragment.class.getName())){
            TransectEndEditFragment endFragment = (TransectEndEditFragment) f;
            TransectEndEditFragment.ViewModel viewModel = endFragment.getViewModel();
            List<PhotoProxy> photos = endFragment.getPhotos();
            StationProxy transEnd = getProxy(viewModel, photos, stationService);
            stationService.saveOrUpdateInTransaction(transEnd);
        }
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

    private StationProxy getProxy(TransectEndEditFragment.ViewModel viewModel, List<PhotoProxy> photos, StationService service){
        StationProxy transEnd = new StationProxy();
        StationService.updateFromViewModel(transEnd, viewModel, photos);
        transEnd.setLocationProxy(LocationService.createProxyFromLocations(CollectionOperations.createList(viewModel.location)
                , new Location(), LocationService.FEATURE_TYPE_POINT));
        transEnd.getLocationProxy().setSite(transEnd.getProjectSite().getSite());
        transEnd.getLocationProxy().getModel().setName(transEnd.getModel().getName());
        transEnd.setRoot(service.getStationProxy(service.getStation(viewModel.transectId)));
        return transEnd;
    }

    @Override
    protected void doDelete() {

    }

    @Override
    protected String getStationType() {
        return VegetationGlobals.SURVEY_TRANSECT;
    }

    @Override
    public void onItemSelected(Station station) {
        if (station == null)return;
        SageApplication.getInstance().setItem(PlotManagement.ARG_TRANSECT_CACHE, station);
        Intent intent = new Intent(this, PlotManagement.class);
        intent.putExtra(PlotManagement.ARG_TRANSECT_CACHE, PlotManagement.ARG_TRANSECT_CACHE);
        startActivityForResult(intent, TRANSECT_END_RESULT_CODE);
    }

    public static final int TRANSECT_END_RESULT_CODE = TransectManagement.class.getName().hashCode();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TRANSECT_END_RESULT_CODE && resultCode == Activity.RESULT_OK){
            Station station = SageApplication.getInstance().removeItem(data.getStringExtra(PlotManagement.ARG_TRANSECT_CACHE));
            doTransectEnd(station);
        }
    }

    private void doTransectEnd(Station transect){
        StationService service = new StationService(SageApplication.getInstance().getDaoSession());
        stationProxy = service.getStationProxy(transect);
        List<Station> endStations = service.getSubstations(transect, VegetationGlobals.SURVEY_TRANSECT_END);
        Station endStation;
        if(endStations != null && endStations.size() > 0) endStation = endStations.get(0);
        else endStation = newTransectEnd(transect);
        TransectEndEditFragment.ViewModel viewModel = new TransectEndEditFragment.ViewModel();
        service.updateFromProxy(service.getStationProxy(endStation), viewModel);
        viewModel.transectId = transect.getId();
        Fragment f = getFragmentManager().findFragmentById(R.id.rareplant_stationManagement_containerB);
        if(f == null){
            TransectEndEditFragment endFragment = new TransectEndEditFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(StationEditFragmentBase.ARV_VIEW_STATE, ViewState.getViewStateEdit());
            bundle.putParcelable(StationEditFragmentBase.ARG_VIEW_MODEL, viewModel);
            getFragmentManager().beginTransaction().add(R.id.rareplant_stationManagement_containerB, endFragment, TransectEndEditFragment.class.getName());
        }else{ //replace the current fragment
            TransectEndEditFragment endFragment = new TransectEndEditFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(StationEditFragmentBase.ARV_VIEW_STATE, ViewState.getViewStateEdit());
            bundle.putParcelable(StationEditFragmentBase.ARG_VIEW_MODEL, viewModel);
            getFragmentManager().beginTransaction().replace(R.id.rareplant_stationManagement_containerB, endFragment, TransectEndEditFragment.class.getName());
        }
    }

    private Station newTransectEnd(Station transect){
        Station end = new Station();
        end.setStation(transect);
        end.setStationType(VegetationGlobals.SURVEY_TRANSECT_END);
        end.setName(transect.getName());
        return end;
    }
}
