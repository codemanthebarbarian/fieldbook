package com.amecfw.sage.sulphur.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.GpsLoggingService;
import com.amecfw.sage.sulphur.Constants;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.util.ViewState;

public class Create extends Activity implements OnClickListener {
	public final static String VIEW_STATE_EXTRA = "viewstate";
	public final static String PROJECT_SITE_ID_EXTRA = "proxy";
	private Button btnSaveSample;
	private ViewState viewState;
	private ListView depthsList;
	private EditText name;
	private TextView latitude;
	private TextView longitude;
	private TextView elevation;
	private TextView accuracy;
	private SampleList.LocationProxy proxy;
	private ProjectSite projectSite;
	private MenuItem logPoint;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(SageApplication.getInstance().getThemeID());
		super.onCreate(savedInstanceState);
		init();
		if(savedInstanceState == null){//new instance 
			Intent intent = getIntent(); 
			if(intent != null){
				viewState = intent.getParcelableExtra(VIEW_STATE_EXTRA);
				getProjectSite(0);
			}
		}
		if(viewState == null) viewState = ViewState.getViewStateAdd();
		if(viewState.getState() == ViewState.ADD) onCreate();
		else if (viewState.getState() == ViewState.EDIT) onEdit();
		else this.finish();
	}
	
	@Override
	protected void onDestroy() {
		if(isWaitingForGpsResponse) sendGpsCancel();
		super.onDestroy();
	}



	private void init(){
		isWaitingForGpsResponse = false;
		gpsMessenger = SageApplication.getInstance().getGpsMessenger();
		this.getActionBar().setIcon(R.drawable.sulphur);
 	    this.setContentView(R.layout.sulphur_create_sample);
		btnSaveSample = (Button)findViewById(R.id.createSample_btnSaveSample);
 	    btnSaveSample.setOnClickListener(this);
 	    name = (EditText) findViewById(R.id.createSample_txtSampleName);
 	    latitude = (TextView) findViewById(R.id.createSample_txtGPSLatitude);
 	    longitude = (TextView) findViewById(R.id.createSample_txtGPSLongitude);
 	    elevation = (TextView) findViewById(R.id.createSample_txtGPSAltitude);
 	    accuracy = (TextView) findViewById(R.id.createSample_txtGPSAccuracy);
 	    depthsList = (ListView)findViewById(R.id.createSample_listDepths);
 	    depthsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, Services.getDepths()));
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sulphur_sample_menu, menu);
		logPoint = (MenuItem) menu.findItem(R.id.menuSampleLocation);
		logPoint.setEnabled(!isWaitingForGpsResponse);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menuSampleLocation){
			onGetLocation();
			return true;
		}
		else if(id == R.id.menuNewSample){
			onCreate();
			return true;
		}
		else{
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		getProjectSite(0);
		proxy = new SampleList.LocationProxy();
		proxy.setGpsLocation((Location) savedInstanceState.getParcelable("proxy.gpsLocation"));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//outState.putLong(PROJECT_SITE_ID_EXTRA, projectSite.getId());
		if(isWaitingForGpsResponse){ 
			sendGpsCancel();
		}
		outState.putBoolean("isWaitingForGpsResponse", isWaitingForGpsResponse);
		outState.putParcelable("proxy.gpsLocation", proxy.getGpsLocation());
		super.onSaveInstanceState(outState);
	}
	
	private SampleList.ViewModel getViewModel(){
		SampleList.ViewModel vm = new SampleList.ViewModel();
		String name = this.name.getText().toString();
		vm.setName(name.trim());
		vm.setLocation(location);
		vm.setDepths(getSelectedDepths());
		return vm;
	}
	
	private String getSelectedDepths(){
		StringBuilder depths = new StringBuilder();
		for (int i = 0; i < depthsList.getCount(); i++) {
			if (depthsList.isItemChecked(i)) {
				depths.append(depthsList.getItemAtPosition(i).toString() + ", ");
			}
		}
		if(depths.length() > 1) depths.delete(depths.length() -2, depths.length() -1); //remove the last comma and space
		return depths.toString();
	}

	private void onCreate(){
		this.setTitle("New Sample");
		proxy = new SampleList.LocationProxy();
	}
	
	private void onEdit(){
		this.setTitle(String.format("Sample %s", proxy.getViewModel().getName()));
	}
	
	private void onSave(){
		//save the new proxy
		proxy.setViewModel(getViewModel());
		Services service = new Services(SageApplication.getInstance().getDaoSession());
		service.buildFromViewModel(proxy);
		service.save(proxy, projectSite.getSite());
	}
	
	private void onUpdate(){
		//update an existing proxy
		proxy.setViewModel(getViewModel());
	}
	
	private void onGetLocation(){
		if(! isWaitingForGpsResponse) sendCoordinateRequest();		
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == btnSaveSample.getId()){
			switch (viewState.getState()){
			case ViewState.ADD:
				onSave();
				break;
			case ViewState.EDIT:
				onUpdate();
				break;
			}
			if(isWaitingForGpsResponse) sendGpsCancel();
			this.finish();
		}
	}
	
	// Gps access

	private Messenger gpsMessenger;
	private Location location;
	private boolean isWaitingForGpsResponse;

	@SuppressLint("HandlerLeak")
	class GpsIncomingHandler extends Handler {
		@Override
		public void handleMessage(Message m) {
			try {
				if (m.what == GpsLoggingService.GET_POINT) {
					location = (Location) m.getData().get(Location.class.getName());
					if (location == null)
						accuracy.setText("GPS UNAVAILABLE");
					else {
						latitude.setText(Double.toString(location.getLatitude()));
						longitude.setText(Double.toString(location.getLongitude()));
						elevation.setText(Double.toString(location.getAltitude()));
						if (location.hasAccuracy())
							accuracy.setText(Float.toString(location.getAccuracy()));
						else
							accuracy.setText("UNKNOWN");
					}
				}
			} catch (Exception e) {
				accuracy.setText("GPS ERROR");
			}
			isWaitingForGpsResponse = false;
			logPoint.setEnabled(! isWaitingForGpsResponse);
		}
	}
	
	private void sendCoordinateRequest() {
		Message msg = Message.obtain(null, GpsLoggingService.GET_POINT);
		msg.replyTo = new Messenger(new GpsIncomingHandler());
		try {
			isWaitingForGpsResponse = true;
			logPoint.setEnabled(!isWaitingForGpsResponse);
			accuracy.setText("Getting Location");
			gpsMessenger.send(msg);
		} catch (RemoteException re) {
			accuracy.setText("GPS ERROR");
			isWaitingForGpsResponse = false;
			logPoint.setEnabled(!isWaitingForGpsResponse);
			Log.e(this.getClass().getSimpleName(), re.getMessage());
		}
	}

	private void sendGpsCancel() {
		if (isWaitingForGpsResponse) {
			Message msg = Message.obtain(null, GpsLoggingService.CANCEL);
			try {
				isWaitingForGpsResponse = false;
				logPoint.setEnabled(!isWaitingForGpsResponse);
				gpsMessenger.send(msg);
			} catch (RemoteException re) {
				accuracy.setText("GPS ERROR");
				isWaitingForGpsResponse = true;
				logPoint.setEnabled(!isWaitingForGpsResponse);
				Log.e(this.getClass().getSimpleName(), re.getMessage());
			}
		}
	}

}
