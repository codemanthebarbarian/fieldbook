package com.amecfw.sage.sulphur;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.sulphur.project.ProjectList;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SulphurMainActivity extends Activity {
	private int themeId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		themeId = SageApplication.getInstance().getThemeID();
		setTheme(themeId);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sulphur_activity_main);
		
		if (findViewById(R.id.sulphurContainer) != null) {
			if (savedInstanceState != null) {
				return;
			}
		}
			
		if (savedInstanceState == null) {
			ProjectList pl = new ProjectList();
			getFragmentManager().beginTransaction()
					.add(R.id.sulphurContainer, pl).commit();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sulphur_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if(id == R.id.dayNightTheme){
			if (themeId == R.style.AppTheme_Dark) {
				themeId = R.style.AppTheme_Light;
			} else {
				themeId = R.style.AppTheme_Dark;
			}
			this.recreate();
			return true;
		}
		else if (id == R.id.database_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void setActionBarTitle(String title) {
	    getActionBar().setTitle(title);
	}
	
	public void setActionBarIcon(int sulphur) {
	    getActionBar().setIcon(sulphur);
	}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.sulphur_fragment_main, container,
					false);
			return rootView;
		}
	}

}
