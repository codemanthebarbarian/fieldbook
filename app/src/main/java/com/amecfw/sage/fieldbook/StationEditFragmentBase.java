package com.amecfw.sage.fieldbook;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.GpsLoggingService;
import com.amecfw.sage.model.service.LocationService;
import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.ui.DateTimePicker;
import com.amecfw.sage.ui.PhotoActivity;
import com.amecfw.sage.ui.PhotoHorizontalListFragment;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.ApplicationUI;
import com.amecfw.sage.util.OnExitListener;
import com.amecfw.sage.util.ViewState;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * A base fragement for creating and editing stations. Be sure to call intializeBase(view)
 */
public abstract class StationEditFragmentBase<TViewModel extends com.amecfw.sage.proxy.ViewModel> extends Fragment implements ActionEvent.Listener {

    public static final String ARG_VIEW_MODEL = "sage.fieldbook.StationEditFragmentBase.viewModel";
    public static final String ARV_VIEW_STATE = "sage.fieldbook.StationEditFragmentBase.viewState";
    protected static final String ARG_IS_DIRTY = "sage.fieldbook.StationEditFragmentBase.isDirty";

    protected ViewState viewState;
    protected EditText stationName;
    protected EditText comments;
    protected ImageButton setDateTime;
    protected ImageButton getPhoto;
    protected TextView coordinateText;
    protected TextView dateTimeCollected;
    protected static String dtFormatText = "Date Time Collected: %s %s";
    protected Date dateCreatedStamp;
    protected Date timeCreatedStamp;
    protected TimeZone timeZone;
    protected List<PhotoProxy> photos;

    protected void initializeBase(View view) {
        stationName = (EditText) view.findViewById(R.id.stationEditBase_layout_stationName);
        stationName.addTextChangedListener(textWatcher);
        coordinateText = (TextView) view.findViewById(R.id.stationEditBase_layout_coordinateText);
        setDateTime = (ImageButton) view.findViewById(R.id.stationEditBase_layout_setDateTime);
        setDateTime.setOnClickListener(setDateTimeListener);
        comments = (EditText) view.findViewById(R.id.stationEditBase_layout_comments);
        comments.addTextChangedListener(textWatcher);
        dateTimeCollected = (TextView) view.findViewById(R.id.stationEditBase_layout_dateTimeCollected);
        dateTimeCollected.addTextChangedListener(textWatcher);
        getLocation = (ImageButton) view.findViewById(R.id.stationEditBase_layout_locationButton);
        getLocation.setOnClickListener(gpsButtonClickListener);
        getPhoto = (ImageButton) view.findViewById(R.id.stationEditBase_layout_takePhoto);
        getPhoto.setOnClickListener(photoListener);
    }

    /**
     * initialize parameters from a saved instance state or fragment arguments
     * @param bundle
     */
    protected void initialize(Bundle bundle){
        TViewModel viewModel = null;
        if(bundle != null){
            viewState = bundle.getParcelable(ARV_VIEW_STATE);
            viewModel = bundle.getParcelable(ARG_VIEW_MODEL);
            mIsDirty = bundle.getBoolean(ARG_IS_DIRTY, false);
        } else {
            mIsDirty = false;
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
            updateLocationText((Location)null);
            Calendar now = Calendar.getInstance();
            setDateCollected(now);
            setTimeCollected(now);
        }
        PhotoHorizontalListFragment fragment = new PhotoHorizontalListFragment();
        fragment.setProxies(photos);
        getFragmentManager().beginTransaction()
                .add(R.id.stationEditBase_layout_photoFragment, fragment, PhotoHorizontalListFragment.class.getName())
                .commit();
    }

    /**
     * creates a new TViewModel from the provided StationProxy. If the proxy is null. creates blank
     * TViewModel
     * @param stationProxy the proxy to create from or null to create a default
     * @return a TViewModel
     */
    protected abstract  TViewModel createViewModel(StationProxy stationProxy);

    public abstract TViewModel getViewModel();

    public abstract void setViewModel(TViewModel viewModel);

    /**
     * Saves mIsDirty, viewState, and ViewModel to the instance state.
     * Will also cancel any gps requests if initialized and waiting
     * @param outState the saved instance state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(gpsHandler != null && gpsHandler.isWaitingForGpsResponse) gpsHandler.sendGpsCancel();
        outState.putBoolean(ARG_IS_DIRTY, mIsDirty);
        outState.putParcelable(ARV_VIEW_STATE, viewState);
        outState.putParcelable(ARG_VIEW_MODEL, getViewModel());
        super.onSaveInstanceState(outState);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Action Event

    private void onExit(){
        if(exitListener == null) return;
        exitListener.onExit(getViewModel(), viewState);
    }

    protected void doEdit(Bundle bundle){
        TViewModel vm = bundle.getParcelable(ARG_VIEW_MODEL);
        if(vm != null) setViewModel(vm);
        mIsDirty = false;
    }

    private OnExitListener<TViewModel> exitListener;
    public void setOnExitListener(OnExitListener<TViewModel> listener){
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
                        mIsDirty = false;
                        break;
                    case COMMAND_NOTIFY_NEW:
                        TViewModel vm = args.getParcelable(ARG_VIEW_MODEL);
                        if(vm == null) vm = createViewModel(null);
                        setViewModel(vm);
                        Calendar now = Calendar.getInstance();
                        setDateCollected(now);
                        setTimeCollected(now);
                        mIsDirty = false;
                }
        }
    }

    // END Action Event
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Gps access

    @Override
    public void onDestroy() {
        if(gpsHandler != null){
            gpsHandler.destroy();
            gpsHandler = null;
        }
        super.onDestroy();
    }

    protected void updateLocationText(Location location){
        if(location != null)
            coordinateText.setText(LocationService.formatLocationText(location));
        else coordinateText.setText(R.string.notAvailable_na);
    }

    protected void updateLocationText(String message){
        coordinateText.setText(message);
    }

    protected void getCoordinate(){
        if(gpsHandler == null) {
            gpsHandler = new GpsIncomingHandler(this);
            gpsHandler.setOnEditListener(gpsOnEditListener);
        }
        gpsHandler.sendCoordinateRequest();
    }

    com.amecfw.sage.util.OnEditListener gpsOnEditListener = new com.amecfw.sage.util.OnEditListener() {
        @Override
        public void onDirty() {
            if(! mIsDirty) mIsDirty = true;
        }

        @Override
        public void onSave() {
            //Do nothing
        }
    };

    protected View.OnClickListener gpsButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) { getCoordinate();}
    };

    protected Location location;
    protected ImageButton getLocation;
    protected GpsIncomingHandler gpsHandler;

    static class GpsIncomingHandler extends Handler {
        private Location location;
        private StationEditFragmentBase fragment;
        private Messenger gpsMessenger;
        public boolean isWaitingForGpsResponse;
        private com.amecfw.sage.util.OnEditListener onEditListener;

        public GpsIncomingHandler(StationEditFragmentBase fragment){
            this.fragment = fragment;
            gpsMessenger = SageApplication.getInstance().getGpsMessenger();
        }

        public void setOnEditListener(com.amecfw.sage.util.OnEditListener listener){onEditListener = listener; }

        @Override
        public void handleMessage(Message m) {
            try {
                if (m.what == GpsLoggingService.GET_POINT) {
                    location = (Location) m.getData().get(Location.class.getName());
                    if (location == null)
                        fragment.updateLocationText("GPS UNAVAILABLE");
                    else {
                        fragment.updateLocationText(location);
                        fragment.location = location;
                        if(onEditListener != null) onEditListener.onDirty();
                    }
                }
            } catch (Exception e) {
                fragment.coordinateText.setText("GPS ERROR");
            }
            isWaitingForGpsResponse = false;
        }

        public void sendCoordinateRequest() {
            Message msg = Message.obtain(null, GpsLoggingService.GET_POINT);
            msg.replyTo = new Messenger(this);
            try {
                isWaitingForGpsResponse = true;
                fragment.coordinateText.setText("Getting Location");
                gpsMessenger.send(msg);
            } catch (RemoteException re) {
                fragment.coordinateText.setText("GPS ERROR");
                isWaitingForGpsResponse = false;
                Log.e(this.getClass().getSimpleName(), re.getMessage());
            }
        }

        public void sendGpsCancel() {
            if (isWaitingForGpsResponse) {
                Message msg = Message.obtain(null, GpsLoggingService.CANCEL);
                try {
                    isWaitingForGpsResponse = false;
                    gpsMessenger.send(msg);
                } catch (RemoteException re) {
                    fragment.coordinateText.setText("GPS ERROR");
                    isWaitingForGpsResponse = true;
                    Log.e(this.getClass().getSimpleName(), re.getMessage());
                }
            }
        }

        public void destroy(){
            if(isWaitingForGpsResponse) sendGpsCancel();
            gpsMessenger = null;
            location = null;
            fragment = null;
            onEditListener = null;
        }
    }

    /// END GPS
    ///////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Photots

    protected static final int PHOTO_RESULT_CODE = PhotoService.PHOTO_RESULT_CODE;

    protected View.OnClickListener photoListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            ApplicationUI.hideSoftKeyboard(getActivity());
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

    protected void addPhoto(PhotoProxy proxy){
        if(photos == null) photos = new ArrayList<>();
        photos.add(proxy);
        onEdit();
        Fragment fragment = getFragmentManager().findFragmentByTag(PhotoHorizontalListFragment.class.getName());
        if(fragment != null) {
            ((PhotoHorizontalListFragment)fragment).setProxies(photos);
        }
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

    // END Photos
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// DATE TIME
    protected View.OnClickListener setDateTimeListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            ApplicationUI.hideSoftKeyboard(getActivity());
            DateTimePicker.DatePicker datePicker = new DateTimePicker.DatePicker();
            datePicker.setOnDateSelectedListener(dateSelectedListener);
            datePicker.show(getFragmentManager(), DateTimePicker.DatePicker.class.getName());
        }
    };
    protected DateTimePicker.OnDateSelectedListener dateSelectedListener = new DateTimePicker.OnDateSelectedListener() {
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
    protected DateTimePicker.OnTimeSelectedListener timeSelectedListener = new DateTimePicker.OnTimeSelectedListener() {
        @Override
        public void onTimeSelected(Bundle bundle) {
            Calendar now = Calendar.getInstance();
            now.setTime(dateCreatedStamp);
            now.set(Calendar.HOUR, bundle.getInt(DateTimePicker.KEY_HOUR));
            now.set(Calendar.MINUTE, bundle.getInt(DateTimePicker.KEY_MINUTE));
            setTimeCollected(now);
        }
    };

    protected void setDateTimeCollected(){
        String date = dateCreatedStamp == null ? "-" : DateFormat.getDateInstance(DateFormat.LONG).format(dateCreatedStamp);
        String time = timeCreatedStamp == null ? "" : DateFormat.getTimeInstance(DateFormat.LONG).format(timeCreatedStamp);
        dateTimeCollected.setText(String.format(dtFormatText, date, time));
    }
    protected void setDateCollected(Calendar calendar){
        dateCreatedStamp = calendar.getTime();
        dateTimeCollected.setText(String.format(dtFormatText
                , DateFormat.getDateInstance(DateFormat.LONG).format(dateCreatedStamp)
                , DateFormat.getTimeInstance(DateFormat.LONG).format(timeCreatedStamp == null ? dateCreatedStamp : timeCreatedStamp)));
    }

    protected void setTimeCollected(Calendar calendar){
        timeCreatedStamp = calendar.getTime();
        timeZone = calendar.getTimeZone();
        dateTimeCollected.setText(String.format(dtFormatText
                , DateFormat.getDateInstance(DateFormat.LONG).format(dateCreatedStamp == null ? timeCreatedStamp : dateCreatedStamp)
                , DateFormat.getTimeInstance(DateFormat.LONG).format(timeCreatedStamp)));
    }
    /// END DATE TIME
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // OnEdit methods

    protected boolean mIsDirty;
    protected boolean notify; //a flag for the textWater to notify
    public boolean isDirty() { return mIsDirty; }

    protected void onEdit(){
        if(notify && ! mIsDirty) {
            mIsDirty = true;
        }
    }

    protected void onSave(){
        if(mIsDirty) {
            mIsDirty = false;
        }
    }

    protected TextWatcher textWatcher = new TextWatcher() {
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

}
