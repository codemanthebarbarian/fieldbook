package com.amecfw.sage.vegetation.transect;

import com.amecfw.sage.fieldbook.StationManager;
import com.amecfw.sage.vegetation.VegetationGlobals;

/**
 * Created by amec on 2015-05-27.
 */
public class PlotManagement extends StationManager<PlotEditFragment> {
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

    }

    @Override
    protected void doDelete() {

    }

    @Override
    protected String getStationType() {
        return VegetationGlobals.STATION_TYPE_VEGETATION_PLOT;
    }
}
