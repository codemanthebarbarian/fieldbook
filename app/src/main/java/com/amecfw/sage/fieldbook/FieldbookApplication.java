package com.amecfw.sage.fieldbook;

import com.amecfw.sage.model.SageApplication;

import android.app.Application;

/**
 * The main application class to store static variables and provide access to application services
 */
public class FieldbookApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SageApplication.initialize(this);
	}

	@Override
	public void onTerminate() {
		SageApplication.dispose();
		super.onTerminate();		
	}
	
}
