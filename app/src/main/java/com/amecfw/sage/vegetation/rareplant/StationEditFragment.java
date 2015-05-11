package com.amecfw.sage.vegetation.rareplant;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.amecfw.sage.model.FieldDescriptor;
import com.amecfw.sage.model.ObservationDescriptor;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.ui.DateTimePicker;
import com.amecfw.sage.ui.ObservationDialogFragment;
import com.amecfw.sage.ui.PhotoActivity;
import com.amecfw.sage.ui.PhotoHorizontalListFragment;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.OnExitListener;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.vegetation.VegetationGlobals;
import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.model.service.GpsLoggingService;
import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.proxy.PhotoProxy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class StationEditFragment extends Fragment implements ActionEvent.Listener {
	
	public static final String ARG_VIEW_MODEL = "sage.vegetation.rareplant.StationEditFragment.viewModel";
	public static final String ARV_VIEW_STATE = "sage.vegetation.rareplant.StationEditFragment.viewState";
	private static final String ARG_IS_DIRTY = "sage.vegetation.rareplant.StationEditFragment.isDirty";
	
	private ViewState viewState;
	private EditText stationName;
	private EditText comments;
	private ImageButton fieldLead; 
	private ImageButton fieldCrew;
	private ImageButton ecoSite; 
	private ImageButton setDateTime;
	private ImageButton getPhoto;
	private EditText fieldLeadTextField;
	private EditText fieldCrewTextField;
	private EditText ecoSiteTextField;
	private TextView coordinateText;
	private TextView dateTimeCollected;
	private static String dtFormatText = "Date Time Collected: %s %s";
	private Date dateCreatedStamp;
	private Date timeCreatedStamp;
	private TimeZone timeZone;
	private List<PhotoProxy> photos;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		notify = false;
		View view = inflater.inflate(R.layout.rareplant_station_edit, container, false);
 		stationName = (EditText) view.findViewById(R.id.rareplant_stationEdit_stationName);
		stationName.addTextChangedListener(textWatcher);
		fieldLead = (ImageButton)view.findViewById(R.id.rareplant_stationEdit_FieldLeadimageButton);
		fieldLead.setOnClickListener(fieldLeadListener);
		fieldLeadTextField = (EditText)view.findViewById(R.id.rareplant_stationEdit_FieldLead);
		fieldLeadTextField.addTextChangedListener(textWatcher);
		fieldCrew = (ImageButton) view.findViewById(R.id.rareplant_stationEdit_FieldCrewimageButton);
		fieldCrew.setOnClickListener(fieldCrewListener);
		fieldCrewTextField = (EditText)view.findViewById(R.id.rareplant_stationEdit_FieldCrew);
		fieldCrewTextField.addTextChangedListener(textWatcher);
		ecoSite = (ImageButton) view.findViewById(R.id.rareplant_stationEdit_EcoSiteimageButton);
		ecoSite.setOnClickListener(ecoSiteListener);
		ecoSiteTextField = (EditText) view.findViewById(R.id.rareplant_stationEdit_EcoSite);
		ecoSiteTextField.addTextChangedListener(textWatcher);
		coordinateText = (TextView) view.findViewById(R.id.rareplant_stationEdit_coordinateText);
		setDateTime = (ImageButton) view.findViewById(R.id.rareplant_stationEdit_setDateTime);
		setDateTime.setOnClickListener(setDateTimeListener);
		comments = (EditText) view.findViewById(R.id.rareplant_stationEdit_comments);
		comments.addTextChangedListener(textWatcher);
		dateTimeCollected = (TextView) view.findViewById(R.id.rareplant_stationEdit_dateTimeCollected);
		dateTimeCollected.addTextChangedListener(textWatcher);
		initialize(savedInstanceState == null ? getArguments() : savedInstanceState);
		isWaitingForGpsResponse = false;
		gpsMessenger = SageApplication.getInstance().getGpsMessenger();
		getLocation = (ImageButton) view.findViewById(R.id.rareplant_stationEdit_locationButton);
		getLocation.setOnClickListener(gpsButtonClickListener);
		getPhoto = (ImageButton) view.findViewById(R.id.rareplant_stationEdit_takePhoto);
		getPhoto.setOnClickListener(photoListener);
		notify = true;
		return view;
	}
	
	private void initialize(Bundle bundle){
		ViewModel viewModel = null;
		if(bundle != null){
			viewState = bundle.getParcelable(ARV_VIEW_STATE);
			viewModel = bundle.getParcelable(ARG_VIEW_MODEL);
			_isDirty = bundle.getBoolean(ARG_IS_DIRTY, false);
		} else {
			_isDirty = false;
		}
		if(viewState == null) viewState = viewModel == null ? ViewState.getViewStateAdd() : ViewState.getViewStateView();
		if(viewModel != null){
			setViewModel(viewModel);
			Calendar dt = Calendar.getInstance();
			if(dateCreatedStamp != null){
				dt.setTime(dateCreatedStamp);
				setDateCollected(dt);
			}
			if(timeCreatedStamp != null){
				dt.setTime(timeCreatedStamp);
				setTimeCollected(dt);
			}
		} else{
			updateLocationText(null);
			Calendar now = Calendar.getInstance();
			setDateCollected(now);
			setTimeCollected(now);
		}
		PhotoHorizontalListFragment fragment = new PhotoHorizontalListFragment();
		fragment.setProxies(photos);
		getFragmentManager().beginTransaction()
							.add(R.id.rarePlant_stationEdit_photoFragment, fragment, PhotoHorizontalListFragment.class.getName())
							.commit();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(isWaitingForGpsResponse) sendGpsCancel();
		outState.putBoolean(ARG_IS_DIRTY, _isDirty);
		outState.putParcelable(ARV_VIEW_STATE, viewState);
		outState.putParcelable(ARG_VIEW_MODEL, getViewModel());
		super.onSaveInstanceState(outState);
	}
	
	public void setViewModel(ViewModel viewModel){
		notify = false;
		if(viewModel == null){
			notify = true;
			return;
		}
		stationName.setText(viewModel.stationName);
		fieldLeadTextField.setText(viewModel.fieldLead);
		fieldCrewTextField.setText(viewModel.fieldCrew);
		ecoSiteTextField.setText(viewModel.ecoSite);
		dateCreatedStamp = viewModel.dateCreated;
		timeCreatedStamp = viewModel.timeCreated;
		comments.setText(viewModel.comments);
		if(viewModel.timeZone != null) timeZone = TimeZone.getTimeZone(viewModel.timeZone);
		setDateTimeCollected();
		location = viewModel.location;
		updateLocationText(location);
		_isDirty = false;
		notify = true;
	}
	
	public ViewModel getViewModel(){
		ViewModel viewModel = new ViewModel();
		viewModel.stationName = Convert.toStringOrNull(stationName);
		viewModel.fieldLead = Convert.toStringOrNull(fieldLeadTextField);
		viewModel.fieldCrew = Convert.toStringOrNull(fieldCrewTextField);
		viewModel.ecoSite = Convert.toStringOrNull(ecoSiteTextField);
		viewModel.dateCreated = dateCreatedStamp;
		viewModel.timeCreated = timeCreatedStamp;
		viewModel.comments = Convert.toStringOrNull(comments);
		viewModel.timeZone = timeZone.getID();
		viewModel.location = location;
		return viewModel;
	}
	
	public void setPhotos(List<PhotoProxy> photos){
		notify = false;
		this.photos = photos;
		if(getFragmentManager() == null) return;
		Fragment f = getFragmentManager().findFragmentByTag(PhotoHorizontalListFragment.class.getName());
		if(f != null){
			PhotoHorizontalListFragment photoFrag = (PhotoHorizontalListFragment) f;
			photoFrag.setProxies(this.photos);
		}
		notify = true;
	}
	
	public List<PhotoProxy> getPhotos() { return photos; }
	
	private void onExit(){
		if(exitListener == null) return;
		exitListener.onExit(getViewModel(), viewState);
	}
	
	private OnExitListener<ViewModel> exitListener;
	public void setOnExitListener(OnExitListener<ViewModel> listener){
		exitListener = listener;
	}
	
	/** edit command - same as ViewState.EDIT provide the ViewModel to edit in the command bundle ARG_VIEW_MODEL */
	public static final int COMMAND_EDIT = ViewState.EDIT;
	/** notify save command, notifies that the viewmodel has been saved to the database and sets dirty to false */
	public static final int COMMAND_NOTIFY_SAVE = ViewState.VIEW;
	/** refresh the form to create a new station */
	public static final int COMMAND_NOTIFY_NEW = ViewState.ADD;

	/**
	 * Responds to the ActionEvent.Exit by returning the viewmodel
	 * Responds to the ActionEvent.Save by returning the viewmodel (Same as ActionEvent.EXIT)
	 * Responds to the ActionEvent.DO_COMMAND with a COMMAND_EDIT flag, args must contain a viewmodel for editing
	 * Responds to the ActionEvent.DO_COMMAND with a COMMAND_NOTIFY_SAVE flag, notify of a save and set dirty to false
	 * Responds to the ActionEvent.DO_COMMAND with a COMMAND_NOTIFY_NEW flag, args can contain a new viewmodel as template
	 * this will use the current date and time
	 */
	@Override
	public void actionPerformed(ActionEvent e){
		switch(e.getAction()){
		case ActionEvent.SAVE:
		case ActionEvent.EXIT:
			onExit();			
			break;
		case ActionEvent.DO_COMMAND:
			Bundle args = e.getArgs();
			int command = args.getInt(ActionEvent.ARG_COMMAND, -1);
			switch(command){
			case COMMAND_EDIT:
				doEdit(args);
				break;
			case COMMAND_NOTIFY_SAVE:
				_isDirty = false;
				break;
			case COMMAND_NOTIFY_NEW:
				ViewModel vm = args.getParcelable(ARG_VIEW_MODEL);
				setViewModel(vm == null ? new ViewModel() : vm);
				Calendar now = Calendar.getInstance();
				setDateCollected(now);
				setTimeCollected(now);
				_isDirty = false;
			}
		}
	}
	
	private void doEdit(Bundle bundle){
		ViewModel vm = bundle.getParcelable(ARG_VIEW_MODEL);
		if(vm != null) setViewModel(vm);
		_isDirty = false;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Gps access
	
	@Override
	public void onDestroy() {
		if(isWaitingForGpsResponse) sendGpsCancel();
		super.onDestroy();
	}

	private void updateLocationText(Location location){
		String coordText = "Lat: %f Long: %f Elev: %f Acc: %f";
		if(location != null)
			coordinateText.setText(String.format(coordText
					, location.getLatitude()
					, location.getLongitude()
					, location.getAltitude()
					, location.getAccuracy()));
		else coordinateText.setText(R.string.notAvailable_na);
	}
	
	private void setMessageText(String message){
		coordinateText.setText(message);
	}
	
	private void onGetLocation(){
		if(! isWaitingForGpsResponse) sendCoordinateRequest();		
	}
	
	private OnClickListener gpsButtonClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) { onGetLocation();}
	};

	private Messenger gpsMessenger;
	private Location location;
	private boolean isWaitingForGpsResponse;
	private ImageButton getLocation; 

	@SuppressLint("HandlerLeak")
	class GpsIncomingHandler extends Handler {
		@Override
		public void handleMessage(Message m) {
			try {
				if (m.what == GpsLoggingService.GET_POINT) {
					location = (Location) m.getData().get(Location.class.getName());
					if (location == null)
						setMessageText("GPS UNAVAILABLE");
					else {
						updateLocationText(location);
						onEdit();
					}
				}
			} catch (Exception e) {
				setMessageText("GPS ERROR");
			}
			isWaitingForGpsResponse = false;
			getLocation.setEnabled(! isWaitingForGpsResponse);
		}
	}
	
	private void sendCoordinateRequest() {
		Message msg = Message.obtain(null, GpsLoggingService.GET_POINT);
		msg.replyTo = new Messenger(new GpsIncomingHandler());
		try {
			isWaitingForGpsResponse = true;
			getLocation.setEnabled(!isWaitingForGpsResponse);
			setMessageText("Getting Location");
			gpsMessenger.send(msg);
		} catch (RemoteException re) {
			setMessageText("GPS ERROR");
			isWaitingForGpsResponse = false;
			getLocation.setEnabled(!isWaitingForGpsResponse);
			Log.e(this.getClass().getSimpleName(), re.getMessage());
		}
	}

	private void sendGpsCancel() {
		if (isWaitingForGpsResponse) {
			Message msg = Message.obtain(null, GpsLoggingService.CANCEL);
			try {
				isWaitingForGpsResponse = false;
				getLocation.setEnabled(!isWaitingForGpsResponse);
				gpsMessenger.send(msg);
			} catch (RemoteException re) {
				setMessageText("GPS ERROR");
				isWaitingForGpsResponse = true;
				getLocation.setEnabled(!isWaitingForGpsResponse);
				Log.e(this.getClass().getSimpleName(), re.getMessage());
			}
		}
	}
	/// END GPS 
	///////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////
	/// DATE TIME
	private OnClickListener setDateTimeListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			DateTimePicker.DatePicker datePicker = new DateTimePicker.DatePicker();
			datePicker.setOnDateSelectedListener(dateSelectedListener);
			datePicker.show(getFragmentManager(), DateTimePicker.DatePicker.class.getName());
		}		
	};
	private DateTimePicker.OnDateSelectedListener dateSelectedListener = new DateTimePicker.OnDateSelectedListener() {		
		@Override
		public void onDateSelected(Bundle bundle) {
			Calendar now = Calendar.getInstance();
			now.set(bundle.getInt(DateTimePicker.KEY_YEAR)
					, bundle.getInt(DateTimePicker.KEY_MONTH)
					, bundle.getInt(DateTimePicker.KEY_DAY));
			setDateCollected(now);
			//Show the time picker
			DateTimePicker.TimePicker timePicker = new DateTimePicker.TimePicker();
			timePicker.setOnTimeSelectedListener(timeSelectedListener);
			timePicker.show(getFragmentManager(), DateTimePicker.TimePicker.class.getName());
		}
	};
	private DateTimePicker.OnTimeSelectedListener timeSelectedListener = new DateTimePicker.OnTimeSelectedListener() {		
		@Override
		public void onTimeSelected(Bundle bundle) {
			Calendar now = Calendar.getInstance();
			now.setTime(dateCreatedStamp);
			now.set(Calendar.HOUR, bundle.getInt(DateTimePicker.KEY_HOUR));
			now.set(Calendar.MINUTE, bundle.getInt(DateTimePicker.KEY_MINUTE));
			setTimeCollected(now);
		}
	};

	private void setDateTimeCollected(){
		String date = dateCreatedStamp == null ? "-" : DateFormat.getDateInstance(DateFormat.LONG).format(dateCreatedStamp);
		String time = timeCreatedStamp == null ? "" : DateFormat.getTimeInstance(DateFormat.LONG).format(timeCreatedStamp);
		dateTimeCollected.setText(String.format(dtFormatText, date, time));
	}
	private void setDateCollected(Calendar calendar){
		dateCreatedStamp = calendar.getTime();
		dateTimeCollected.setText(String.format(dtFormatText
				, DateFormat.getDateInstance(DateFormat.LONG).format(dateCreatedStamp)
				, DateFormat.getTimeInstance(DateFormat.LONG).format(timeCreatedStamp == null ? dateCreatedStamp : timeCreatedStamp)));
	}
	
	private void setTimeCollected(Calendar calendar){
		timeCreatedStamp = calendar.getTime();
		timeZone = calendar.getTimeZone();
		dateTimeCollected.setText(String.format(dtFormatText
				, DateFormat.getDateInstance(DateFormat.LONG).format(dateCreatedStamp == null ? timeCreatedStamp : dateCreatedStamp)
				, DateFormat.getTimeInstance(DateFormat.LONG).format(timeCreatedStamp)));
	}
	/// END DATE TIME
	///////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////
	// PHOTO
	private static final int PHOTO_RESULT_CODE = 100;
	
	private OnClickListener photoListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			PhotoProxy proxy = PhotoService.createProxy(null);
			SageApplication.getInstance().setItem(PhotoActivity.ARG_VIEW_PROXY_CACHE_KEY, proxy);
			Intent intent = new Intent(getActivity(), PhotoActivity.class);
			intent.putExtra(PhotoActivity.ARG_VIEW_PROXY_CACHE_KEY, PhotoActivity.ARG_VIEW_PROXY_CACHE_KEY);
			intent.putExtra(PhotoActivity.ARG_VIEW_STATE, ViewState.getViewStateAdd());
			startActivityForResult(intent, PHOTO_RESULT_CODE);
		}		
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == PHOTO_RESULT_CODE){
			if(resultCode == Activity.RESULT_OK){
				PhotoProxy photoProxy = SageApplication.getInstance().removeItem(data.getExtras().getString(PhotoActivity.ARG_VIEW_PROXY_CACHE_KEY));
				if(photoProxy != null) addPhoto(photoProxy);
			}
			if(resultCode == Activity.RESULT_CANCELED){
				Toast.makeText(getActivity(), "Photo Canceled by User", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void addPhoto(PhotoProxy proxy){
		if(photos == null) photos = new ArrayList<PhotoProxy>();
		photos.add(proxy);
		onEdit();
		Fragment fragment = getFragmentManager().findFragmentByTag(PhotoHorizontalListFragment.class.getName());
		if(fragment != null) {
			((PhotoHorizontalListFragment)fragment).setProxies(photos);
		}
	}
	
	// END PHOTO
	///////////////////////////////////////////////////////////////////////////

	private OnClickListener fieldLeadListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			GroupObservation g = new GroupObservation();
			g.setAllowableValues("ML,LT,CC,OJ,KL,DP");
			Bundle bundle = new Bundle();
			bundle.putParcelable(ObservationDialogFragment.ARG_GROUP_OBSERVATION, g);
			ObservationDialogFragment dialog = new ObservationDialogFragment();
			dialog.setArguments(bundle);
			dialog.setExitListener(new OnExitListener<String>() {
				
				@Override
				public void onExit(String viewModel, ViewState viewState) {
					fieldLeadTextField.setText(viewModel);
				}
			});
			dialog.show(getFragmentManager(), null);
		}
	};
	
	private OnClickListener fieldCrewListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			GroupObservation g = new GroupObservation();
			g.setAllowableValues("ML,LT,CC,OJ,KL,DP");
			Bundle bundle = new Bundle();
			bundle.putParcelable(ObservationDialogFragment.ARG_GROUP_OBSERVATION, g);
			bundle.putBoolean(ObservationDialogFragment.ARG_MULTI_SELECT, true);
			ObservationDialogFragment dialog = new ObservationDialogFragment();
			dialog.setArguments(bundle);
			dialog.setExitListener(new OnExitListener<String>() {
				
				@Override
				public void onExit(String viewModel, ViewState viewState) {
					fieldCrewTextField.setText(viewModel);
				}
			});
			dialog.show(getFragmentManager(), null);
			
		}
	};
	
	private OnClickListener ecoSiteListener = new OnClickListener() {

		
		@Override
		public void onClick(View v) {
			GroupObservation g = new GroupObservation();
			g.setAllowableValues("a1,b1,c1,c2,wetland 1, wetland 2");
			Bundle bundle = new Bundle();
			bundle.putParcelable(ObservationDialogFragment.ARG_GROUP_OBSERVATION, g);
			ObservationDialogFragment dialog = new ObservationDialogFragment();
			dialog.setArguments(bundle);
			dialog.setExitListener(new OnExitListener<String>() {
				
				@Override
				public void onExit(String viewModel, ViewState viewState) {
					ecoSiteTextField.setText(viewModel);
				}
			});
			dialog.show(getFragmentManager(), null);
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////
	// OnEdit methods

	private boolean _isDirty;
	private boolean notify; //a flag for the textWater to notify
	public boolean isDirty() { return _isDirty; }

	private void onEdit(){
		if(notify && ! _isDirty) {
			_isDirty = true;
		}
	}

	private void onSave(){
		if(_isDirty) {
			_isDirty = false;
		}
	}

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void afterTextChanged(Editable editable) {
			onEdit();
		}
	};

	// End OnEdit methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public static class ViewModel implements com.amecfw.sage.proxy.ViewModel {
		
		@FieldDescriptor(clazz = Station.class, targetGetter = "getName", targetSetter = "setName")
		public String stationName;
		@ObservationDescriptor(fieldName="fieldLead", observationType = "field lead", defaultValue = "not recorded")
		public String fieldLead;
		@ObservationDescriptor(fieldName="fieldCrew", observationType = "field crew", defaultValue = "not recorded")
		public String fieldCrew;
		@ObservationDescriptor(fieldName="ecoSite", observationType = "eco site", defaultValue = "")
		public String ecoSite;
		@FieldDescriptor(clazz = Station.class, targetGetter = "getStationType", targetSetter = "setStationType")
		public String stationType = VegetationGlobals.SURVEY_RARE_PLANT;
		@FieldDescriptor(clazz = Station.class, targetGetter = "getSurveyDate", targetSetter = "setSurveyDate", type=DescriptorServices.TYPE_DATE)
		public Date dateCreated;
		@FieldDescriptor(clazz = Station.class, targetGetter = "getSurveyTime", targetSetter = "setSurveyTime", type=DescriptorServices.TYPE_DATE)
		public Date timeCreated;
		@FieldDescriptor(clazz = Station.class, targetGetter = "getTimeZone", targetSetter = "setTimeZone", type=DescriptorServices.TYPE_STRING)
		public String timeZone;
		@FieldDescriptor(clazz = Station.class, targetGetter = "getDescription", targetSetter = "setDescription")
		public String comments;
		public String[] photos;
		public android.location.Location location;	
		
		public static final Parcelable.Creator<ViewModel> CREATOR = 
				new Parcelable.Creator<ViewModel>() {
			@Override
			public ViewModel createFromParcel(Parcel in) {return new ViewModel(in); }
			@Override
			public ViewModel[] newArray(int size) {return new ViewModel[size]; }
				};
				
		public ViewModel(){}
		
		public ViewModel(Parcel in){
			this.stationName = in.readString();
			this.fieldLead = in.readString();
			this.fieldCrew = in.readString();
			this.dateCreated = new Date(in.readLong());
			this.timeCreated = new Date(in.readLong());
			this.timeZone = in.readString();
			this.comments = in.readString();
			photos = new String[in.readInt()];
			in.readStringArray(photos);
			this.location = in.readParcelable(Location.class.getClassLoader());
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags){
			dest.writeString(stationName);
			dest.writeString(fieldLead);
			dest.writeString(fieldCrew);
			dest.writeLong(dateCreated.getTime());
			dest.writeLong(timeCreated.getTime());
			dest.writeString(timeZone);
			dest.writeString(comments);
			dest.writeInt(photos == null ? 0 : photos.length ); 
			dest.writeStringArray(photos);
			dest.writeParcelable(location, PARCELABLE_WRITE_RETURN_VALUE);
		}
		
		@Override
		public int describeContents(){ return 0; }
			
	}
	
}
