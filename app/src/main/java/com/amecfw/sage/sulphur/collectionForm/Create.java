package com.amecfw.sage.sulphur.collectionForm;

import java.util.List;
import java.util.HashMap;

import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.proxy.Model;
import com.amecfw.sage.sulphur.Constants;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.ui.ObservationDialogFragment;
import com.amecfw.sage.util.ApplicationCache;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.model.FieldDescriptor;
import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.Measurement;
import com.amecfw.sage.model.Observation;
import com.amecfw.sage.model.ObservationDescriptor;
import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.Photo;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.model.service.GpsLoggingService;
import com.amecfw.sage.model.service.ObservationService;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.util.ApplicationGps;

import android.app.Activity;
import android.app.FragmentManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Create extends Activity implements OnClickListener {

	public static final String VIEW_STATE_EXTRA = "viewstate";
	public static final String CACHE_KEY_LOCATIONPROXY = "sulphur.collectionForm.Create.locationProxy";
	public static final String CACHE_KEY_COLLECTIONPROXY = "sulphur.collectionForm.Create.collectionProxy";
	/** The key of the observation group to use for allowable values */
	public static final String ID_OBSERVATIONGROUP = "sulphur.collectionFrom.Create.observationGroupID";
	private ViewState viewState;
	private Button save;
	private TextView messages;
	private MenuItem logPoint;
	private TextView depths;
	private SeekBar vegCover;
	private TextView vegCoverValue;
	private EditText comments;
	private RadioGroup efferv;
	private RadioGroup visLime;
	private RadioGroup deadfall;
	private ProjectSite projectSite;
	private com.amecfw.sage.sulphur.sample.SampleList.LocationProxy locationProxy;
	private CollectionProxy proxy;
	private ObservationGroup observationGroup;
	private HashMap<Integer, GroupObservation> groupObservations;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setTheme(SageApplication.getInstance().getThemeID());
		 init();
		 if(savedInstanceState == null){//new instance
			 Intent intent = getIntent(); 
				if(intent != null){
					viewState = intent.getParcelableExtra(VIEW_STATE_EXTRA);
					getObservationGroup(intent.getLongExtra(ID_OBSERVATIONGROUP, 0));
				}				
		 }		 
		 if(viewState == null) viewState = ViewState.getViewStateAdd();
		 if(viewState.getState() == ViewState.ADD) onCreate();
		 else if (viewState.getState() == ViewState.EDIT) onEdit();
		 else this.finish();
	  }
	 
	 private void init(){
		 groupObservations = new HashMap<Integer, GroupObservation>();
		 this.getActionBar().setIcon(R.drawable.sulphur);
	 	 this.setContentView(R.layout.sulphur_collection_form);
	 	 gpsMessenger = SageApplication.getInstance().getGpsMessenger();
	 	 getProjectSite(0);
	 	 getLocationProxy();
	 	 this.getActionBar().setTitle("Site: " + locationProxy.getModel().getName());
	 	 save = (Button) findViewById(R.id.collectionForm_btnSave);
	     save.setOnClickListener(this);
	     depths = (TextView) findViewById(R.id.collectionForm_txtDepth);
	     vegCover = (SeekBar) findViewById(R.id.collectionForm_vegGroundCoverSeekbar);
	     vegCoverValue = (TextView) findViewById(R.id.collectionForm_vegGroundCoverVal);
	     vegCoverValue.setText(String.valueOf(vegCover.getProgress()*5));
	     vegCover.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				vegCoverValue.setText(String.valueOf(progress * 5));
			}
		 });
	     comments = (EditText) findViewById(R.id.collectionForm_Comments);
	     efferv = (RadioGroup) findViewById(R.id.collectionForm_radioEffer);
	     visLime = (RadioGroup) findViewById(R.id.collectionForm_radioVisLime);
	     deadfall = (RadioGroup) findViewById(R.id.collectionForm_radioDeadfall);
	     messages = (TextView) findViewById(R.id.collectionForm_messages);
         if(locationProxy.getDepths() != null) depths.setText(locationProxy.getDepths().getValue());
	 }
	 
	 private void getLocationProxy(){
		 Object cacheItem = SageApplication.getInstance().getObject(CACHE_KEY_LOCATIONPROXY);
		 if (cacheItem == null )
			 onIntitializationError("Error loading collectionForm.Create, no location Proxy");
		 locationProxy = (com.amecfw.sage.sulphur.sample.SampleList.LocationProxy) cacheItem;
	 }
	 
	 private void getProjectSite(long projectSiteId){
		Object cacheItem = SageApplication.getInstance().getObject(Constants.SULPHUR_PROJECTSITE_CACHE_KEY);
		if(cacheItem == null || !(cacheItem instanceof ProjectSite)) 
			onIntitializationError("No project site set exiting form.");
		projectSite = (ProjectSite) cacheItem;
	 }
	 
	 private void getObservationGroup(long id){
		 observationGroup = new ObservationService(SageApplication.getInstance().getDaoSession()).getObservationGroup(id);
	 }
	 
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.collectionForm_soilText){
			GroupObservation go = groupObservations.get(R.id.collectionForm_soilText);
			if(go == null){
				
			}
		}
		else if(v.getId() == R.id.collectionForm_btnSave){
			onSaveOrUpdate();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sulphur_sample_collection_menu, menu);
		logPoint = (MenuItem) menu.findItem(R.id.menuSampleCollectionLocation);
		logPoint.setEnabled(!isWaitingForGpsResponse);
		return true;
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == logPoint.getItemId()){
			sendCoordinateRequest();
			return true;
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(isWaitingForGpsResponse) sendGpsCancel();
		outState.putLong(ID_OBSERVATIONGROUP, observationGroup.getId());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		getObservationGroup(savedInstanceState.getLong(ID_OBSERVATIONGROUP));
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onBackPressed() {
		if(isWaitingForGpsResponse){
			//TODO: notify use
			sendGpsCancel();
		}
		super.onBackPressed();
	}
	
	private void onIntitializationError(String error){
		Log.e("Sulphur.collectionForm", error);
		Toast.makeText(this, error, Toast.LENGTH_LONG).show();
		this.finish();
	}

	private void onCreate(){
		proxy = new CollectionProxy();
	}
	
	private void onEdit(){
		proxy = new CollectionProxy();
		if(locationProxy.getStation() == null) onIntitializationError("No station available for editing");
		Services service = new Services(SageApplication.getInstance().getDaoSession());
		service.hydrateProxy(proxy, locationProxy.getStation());
		service.buildFromModel(proxy);
		setFromViewModel(proxy.getViewModel());
	}
	
	private void onShowObservationDialog(GroupObservation groupObservation, boolean multiSelect, ViewState viewState){
		Bundle bundle = new Bundle();
		bundle.putBoolean(ObservationDialogFragment.ARG_MULTI_SELECT, multiSelect);
		bundle.putParcelable(ObservationDialogFragment.ARG_VIEWSTATE, viewState);
		bundle.putParcelable(ObservationDialogFragment.ARG_GROUP_OBSERVATION, groupObservation);
		ObservationDialogFragment dialogFragment = new ObservationDialogFragment();
		dialogFragment.setArguments(bundle);
		dialogFragment.show(getFragmentManager(), null);
	}
	
	private void onSaveOrUpdate(){
		if(isWaitingForGpsResponse) sendGpsCancel();
		proxy.setViewModel(getViewModel());
		Services service = new Services(SageApplication.getInstance().getDaoSession());
		service.buildFromViewModel(proxy);
		if(viewState.getState() == ViewState.ADD) service.save(proxy, locationProxy.getModel());
		else if (viewState.getState() == ViewState.EDIT) service.update(proxy, locationProxy.getModel());
		this.finish();
	}
	
	// Gps access

		private Messenger gpsMessenger;
		private Location location;
		private boolean isWaitingForGpsResponse;

		class GpsIncomingHandler extends Handler {
			@Override
			public void handleMessage(Message m) {
				try {
					if (m.what == GpsLoggingService.GET_POINT) {
						location = (Location) m.getData().get(Location.class.getName());
						if (location == null)
							messages.setText("GPS UNAVAILABLE");
						else {
							//latitude.setText(Double.toString(location.getLatitude()));
							//longitude.setText(Double.toString(location.getLongitude()));
							//elevation.setText(Double.toString(location.getAltitude()));
							if (location.hasAccuracy())
								messages.setText("GPS Accuracy: " + Float.toString(location.getAccuracy()));
							else
								messages.setText("Position Collected");
						}
					}
				} catch (Exception e) {
					messages.setText("GPS ERROR");
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
				messages.setText("Getting Location");
				gpsMessenger.send(msg);
			} catch (RemoteException re) {
				messages.setText("GPS ERROR");
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
					messages.setText("GPS ERROR");
					isWaitingForGpsResponse = true;
					logPoint.setEnabled(!isWaitingForGpsResponse);
					Log.e(this.getClass().getSimpleName(), re.getMessage());
				}
			}
		}
		
		private ViewModel getViewModel(){
			ViewModel model = new ViewModel();
			model.setName(locationProxy.getModel().getName());
			model.setProjectSiteId(projectSite.getId());
			String temp;
			temp = comments.getText().toString();
			if(temp != null && temp.trim() != new String()) model.setComments(temp);
			else model.setComments(null);
			model.setEffervesence(getSelectedValue(efferv));
			model.setVisibleLime(getSelectedValue(visLime));
			model.setDeadfall(getSelectedValue(deadfall));
			model.setVegCover(vegCoverValue.getText().toString());
			return model;
		}
		
		private void setFromViewModel(ViewModel model){
			if(model == null) return;
			if(model.getComments() != null) comments.setText(model.getComments());
			if(model.getEffervesence() != null) setSelectedValue(model.getEffervesence(), efferv);
			if(model.getVisibleLime() != null) setSelectedValue(model.getVisibleLime(), visLime);
			if(model.getDeadfall() != null) setSelectedValue(model.getDeadfall(), deadfall);
			setVegCover(model.getVegCover());
		}
		
		private void setVegCover(String value){
			int val;
			try{
				val = Integer.parseInt(value);
			}catch(NumberFormatException nfe){
				val = 0;
			}
			vegCoverValue.setText(value);
			val = val / 5;
			vegCover.setProgress(val);
		}
		
		private String getSelectedValue(RadioGroup radioGroup){
			int id = radioGroup.getCheckedRadioButtonId();
			if (id == -1) return null; //nothing selected
			RadioButton button = (RadioButton) findViewById(id);
			return button.getText().toString();
		}
		
		private boolean setSelectedValue(String value, RadioGroup radioGroup){
			if(value != null && value != new String()){
				for(int i = 0 ; i < radioGroup.getChildCount() ; i++){
					View view = radioGroup.getChildAt(i);
					if(view instanceof RadioButton){
						RadioButton button = (RadioButton) view;
						button.setChecked(value.equalsIgnoreCase(button.getText().toString()));
					}
				}
			}
			return true;
		}
		
		public static class ViewModel{
			@FieldDescriptor(defaultValue = "Sulphur Sample", targetGetter = "getStationType", targetSetter = "setStationType", clazz = Station.class)
			private String stationType;
			@FieldDescriptor(targetGetter = "getProjectSiteID", targetSetter = "setProjectSiteID", type = DescriptorServices.TYPE_LONG, clazz = Station.class)
			private long projectSiteId;
			@FieldDescriptor(targetGetter = "getName", targetSetter = "setName", type = DescriptorServices.TYPE_STRING, clazz = Station.class)
			private String name;
			@FieldDescriptor(targetGetter = "getDescription", targetSetter = "setDescription", type = DescriptorServices.TYPE_STRING, clazz = Station.class)
			private String comments;
			@ObservationDescriptor(fieldName = "effervesence", observationType = "Effervesence", defaultValue="Not Sampled")
			private String effervesence;
			@ObservationDescriptor(fieldName = "visibleLime", observationType = "Visible Lime", defaultValue="Not Sampled")
			private String visibleLime;
			@ObservationDescriptor(fieldName = "deadfall", observationType = "Deadfall", defaultValue="Not Sampled")
			private String deadfall;
			@ObservationDescriptor(fieldName = "vegCover", observationType = "Vegetation Cover %", defaultValue="Not Sampled")
			private String vegCover;
			@ObservationDescriptor(fieldName = "soil", observationType = "Soil Type", defaultValue="Not Sampled")
			private String soil;
			
			public String getStationType() {
				return stationType;
			}
			public void setStationType(String stationType) {
				this.stationType = stationType;
			}
			public long getProjectSiteId() {
				return projectSiteId;
			}
			public void setProjectSiteId(long projectSiteId) {
				this.projectSiteId = projectSiteId;
			}
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public String getComments() {
				return comments;
			}
			public void setComments(String comments) {
				this.comments = comments;
			}
			public String getEffervesence() {
				return effervesence;
			}
			public void setEffervesence(String effervesence) {
				this.effervesence = effervesence;
			}
			public String getVisibleLime() {
				return visibleLime;
			}
			public void setVisibleLime(String visibleLime) {
				this.visibleLime = visibleLime;
			}
			public String getDeadfall() {
				return deadfall;
			}
			public void setDeadfall(String deadfall) {
				this.deadfall = deadfall;
			}
			public String getVegCover() {
				return vegCover;
			}
			public void setVegCover(String vegCover) {
				this.vegCover = vegCover;
			}
			public String getSoil() {
				return soil;
			}
			public void setSoil(String soil) {
				this.soil = soil;
			}
						
		}
		
		public static class CollectionProxy extends Model<ViewModel, Station>{
			private List<Observation> observations;
			private List<Measurement> measurements;
			private List<Photo> photos;
			private android.location.Location gpsLocation;
			public List<Observation> getObservations() {
				return observations;
			}
			public void setObservations(List<Observation> observations) {
				this.observations = observations;
			}
			public List<Measurement> getMeasurements() {
				return measurements;
			}
			public void setMeasurements(List<Measurement> measurements) {
				this.measurements = measurements;
			}
			public List<Photo> getPhotos() {
				return photos;
			}
			public void setPhotos(List<Photo> photos) {
				this.photos = photos;
			}
			public android.location.Location getGpsLocation() {
				return gpsLocation;
			}
			public void setGpsLocation(android.location.Location gpsLocation) {
				this.gpsLocation = gpsLocation;
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
