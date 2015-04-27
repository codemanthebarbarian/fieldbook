package com.amecfw.sage.sulphur.sample;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.LocationMeta;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.sulphur.Constants;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.util.ApplicationCache;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.proxy.Model;

/**
 * The main entry activity for working with data on a specific project/site combination
 */
public class SampleList extends ListActivity {
	public static final String PROJECT_SITE_KEY = "SampleList_ProjectSite";
	private ProjectSite projectSite;
	private ListView locations;
	private SampleListAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Object tmp = savedInstanceState.get(PROJECT_SITE_KEY);
		this.getActionBar().setTitle("Samples");
		this.getActionBar().setIcon(R.drawable.sulphur);
		this.setContentView(R.layout.sulphur_sample_list);
		getProjectSite(0);
		locations = (ListView) findViewById(android.R.id.list);
		adapter = new SampleListAdapter(this, new ArrayList<LocationProxy>());
		locations.setAdapter(adapter);
		locations.setItemsCanFocus(true);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		List<LocationProxy> proxies = new Services(SageApplication.getInstance().getDaoSession()).getLocations(projectSite);
		adapter.clear();
		if(proxies != null) adapter.addAll(proxies);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sulphur_sample_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if(id == R.id.menuNewSample){
			onNewSample();
			return true;
		}
		else{
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void getProjectSite(long projectSiteId){
		Object cacheItem = SageApplication.getInstance().getObject(Constants.SULPHUR_PROJECTSITE_CACHE_KEY);
		if(cacheItem == null || !(cacheItem instanceof ProjectSite)){
			//Log error
			//toast message
			//end activity
		}else{
			projectSite = (ProjectSite) cacheItem;
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(state);
		getProjectSite(0);
	}
	
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(l != null){
			LocationProxy locProxy = (LocationProxy) locations.getItemAtPosition(position);
			SageApplication.getInstance().setItem(com.amecfw.sage.sulphur.collectionForm.Create.CACHE_KEY_LOCATIONPROXY, locProxy);
			Intent intent = new Intent(this, com.amecfw.sage.sulphur.collectionForm.Create.class);
			intent.putExtra(com.amecfw.sage.sulphur.collectionForm.Create.VIEW_STATE_EXTRA, 
					locProxy.getStation() == null ? ViewState.getViewStateAdd() : ViewState.getViewStateEdit());
			startActivity(intent);
		}
	}

	private void onNewSample(){
		SageApplication.getInstance().setItem(Constants.SULPHUR_PROJECTSITE_CACHE_KEY, projectSite);
		Intent intent = new Intent(this, Create.class);
		intent.putExtra(Create.VIEW_STATE_EXTRA, ViewState.getViewStateAdd());
		intent.putExtra(Create.PROJECT_SITE_ID_EXTRA, projectSite.getId());
		this.startActivity(intent);
	}
	
	public static class ViewModel{
		private String name;
		private String depths;
		private boolean completed;
		private android.location.Location location;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDepths() {
			return depths;
		}
		public void setDepths(String depths) {
			this.depths = depths;
		}
		public boolean isCompleted() {
			return completed;
		}
		public void setCompleted(boolean completed) {
			this.completed = completed;
		}
		public android.location.Location getLocation() {
			return location;
		}
		public void setLocation(android.location.Location location) {
			this.location = location;
		}		
	}
	
	public static class LocationProxy extends Model<ViewModel, Location>{
		private android.location.Location gpsLocation;
		private LocationMeta depths;
		private List<Coordinate> coordinates;
		private Station station;
		public android.location.Location getGpsLocation() {
			return gpsLocation;
		}
		public void setGpsLocation(android.location.Location gpsLocation) {
			this.gpsLocation = gpsLocation;
		}
		public LocationMeta getDepths() {
			return depths;
		}
		public void setDepths(LocationMeta depths) {
			this.depths = depths;
		}
		public List<Coordinate> getCoordinates() {
			return coordinates;
		}
		public void setCoordinates(List<Coordinate> coordinates) {
			this.coordinates = coordinates;
		}
		public Station getStation() {
			return station;
		}
		public void setStation(Station station) {
			this.station = station;
		}
		@Override
		public void buildViewModel() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void buildModel() {
			// TODO Auto-generated method stub
			
		}
	}
}
