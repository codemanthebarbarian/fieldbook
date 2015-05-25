package com.amecfw.sage.vegetation.transect;

import android.os.Bundle;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.fieldbook.StationManager;
import com.amecfw.sage.model.Station;

/**
 *
 */
public class TransectManagement extends StationManager {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getActionBar().setIcon(R.drawable.leaf);
    }

    @Override
    protected void loadEditFragment() {

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
        return false;
    }

    @Override
    protected String getStationType() {
        return null;
    }


}
