package com.amecfw.sage.vegetation.transect;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.fieldbook.StationManager;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.vegetation.VegetationGlobals;

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

    }

    @Override
    protected void doDelete() {

    }

    @Override
    protected String getStationType() {
        return VegetationGlobals.SURVEY_TRANSECT;
    }


}
