package com.amecfw.sage.fieldbook;

import com.amecfw.sage.model.SageApplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(SageApplication.getInstance().getThemeID());
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new SettingsFragment())
			.commit();
	}	

	@Override
	protected void onResume() {
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferencesListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(preferencesListener);
	}

	private void refresh(){
		this.recreate();
	}

	private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {		
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if(key.equals( getResources().getString(R.string.preferencesApp_themeKey))){
//				String theme = sharedPreferences.getString(key, SageApplication.THEME_LIGHT);
//				if(theme.equals(SageApplication.THEME_DARK)) SageApplication.getInstance().setThemeID(R.style.AppTheme_Dark);
//				else SageApplication.getInstance().setThemeID(R.style.AppTheme_Light);
				refresh();
			}			
		}
	};	
	
}
