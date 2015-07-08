package com.amecfw.sage.fieldbook;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

import com.amecfw.sage.fieldbook.HomePage;
import com.amecfw.sage.model.SageApplication;

/**
 * 
 */

public class MainActivity extends Activity {
	
	private boolean refresh = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(this, R.xml.preferences_app, false);
		if(SageApplication.getInstance() == null) SageApplication.initialize(getApplication());
		this.setTheme(SageApplication.getInstance().getThemeID());
		super.onCreate(savedInstanceState);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesListener);		
		setContentView(R.layout.activity_main);
		if (findViewById(R.id.fragmentContainer) != null) {
			if (savedInstanceState != null) {
				return;
			}
			HomePage home = new HomePage();
			getFragmentManager().beginTransaction().add(R.id.fragmentContainer, home).commit();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	public void setActionBarTitle(String title) {
	    getActionBar().setTitle(title);
	}
	
	public void setActionBarIcon(int sulphur) {
	    getActionBar().setIcon(sulphur);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		if(refresh) recreate();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(preferencesListener);
		SageApplication.dispose();
		super.onDestroy();
	}

	private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {		
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if(key.equals( getResources().getString(R.string.preferencesApp_themeKey))){
				refresh = true;
			}			
		}
	};	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.dayNightTheme:
			Intent prfIntent = new Intent(this, SettingsActivity.class);
			startActivity(prfIntent);
			return true;
		case R.id.fieldbook_databaseOptions:
			Intent intent = new Intent(this, com.amecfw.sage.ui.ManageDatabase.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
