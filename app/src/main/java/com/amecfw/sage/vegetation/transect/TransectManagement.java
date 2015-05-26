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
    protected void doSave() {

    }

    @Override
    protected void doAdd() {

    }

    @Override
    protected void doDelete() {

    }

    @Override
    protected void doEdit(Station station) {

    }

    @Override
    protected void doCancel() {

    }

    @Override
    protected boolean isDirty() {
        return isDirty(TransectEditFragment.class);
    }

    @Override
    protected String getStationType() {
        return VegetationGlobals.SURVEY_TRANSECT;
    }


}
