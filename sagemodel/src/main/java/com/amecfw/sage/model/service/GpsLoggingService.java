package com.amecfw.sage.model.service;

import java.util.List;

//import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
//import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class GpsLoggingService extends Service {
	/** Constant Value: 0 */
	public static final int LOG_STOP = 0;
	/** Constant Value: 1 */
	public static final int LOG_POINT = 1;
	/** Constant Value: 2 */
	public static final int LOG_LINE = 2;
	/** Constant Value: 3 */
	public static final int LOG_AREA = 3;
	/** Constant Value: 4 */
	public static final int GET_POINT = 4;
	/** Constant Value: 5 */
	public static final int PASSIVE_START = 5;
	/** Constant Value: 6 */
	public static final int PASSIVE_STOP = 6;
	/** Constant Value: 7 */
	public static final int PASSIVE_MESSAGE = 7;
	/** Constant Value: 10 */
	public static final int CANCEL = 10;
	/** Constant Value: -1 */
	public static final int TIMEOUT = -1;
	/** Same as Criteria.ACCURACY_FINE (Constant Value: 1) */
	public static final int SOURCE_FINE_GPS = Criteria.ACCURACY_FINE;
	/** Same as Criteria.ACCURACY_COARSE (Constant Value: 2) */
	public static final int SOURCE_COARSE_NETWORK = Criteria.ACCURACY_COARSE;
	/** the key for the location source */
	public static final String KEY_SOURCE = "SOURCE";
	/** the key for the location (same as Location.class.getName() ) */
	public static final String KEY_LOCATION = Location.class.getName();
	
	private LocationManager locManager;
	private GpsLocationListener locListenerFine;
	private GpsLocationListener locListenerCoarse;
	private Messenger messenger = new Messenger(new GpsServiceMessageHandler());
	
	private int timeOutInSeconds = 120;
	private int timeSinceUpdate = 0;
	private long minTimeMillis = 3000;
	private long minDistanceMeters = 5;
	private float minAccuracyMeters = 15;	
	private Boolean showStatusToasts = true;
	private List<Location> locations;
	private Messenger singleSource = null;
	private Location singleUpdate = null;
	private int singleTimeout = 0;
	private int logState = LOG_STOP;
	private Boolean isPassive = false;
	private Messenger passiveSource;
	private Boolean isRecording = false;
	private Boolean isReqestingUpdates = false;
	
	public long getMinTimeMillis() {
		return minTimeMillis;
	}
	public void setMinTimeMillis(long minTimeMillis) {
		this.minTimeMillis = minTimeMillis;
	}

	public long getMinDistanceMeters() {
		return minDistanceMeters;
	}
	public void setMinDistanceMeters(long minDistanceMeters) {
		this.minDistanceMeters = minDistanceMeters;
	}

	public float getMinAccuracyMeters() {
		return minAccuracyMeters;
	}
	public void setMinAccuracyMeters(float minAccuracyMeters) {
		this.minAccuracyMeters = minAccuracyMeters;
	}

	public Boolean getShowStatusToasts() {
		return showStatusToasts;
	}

	public void setShowStatusToasts(Boolean showStatusToasts) {
		this.showStatusToasts = showStatusToasts;
	}

	/** Called when the activity is first created. */	 
    private void startLoggerService() {
    	// ---use the LocationManager class to obtain GPS locations---
    	locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	locListenerFine = new GpsLocationListener(Criteria.ACCURACY_FINE, LocationManager.GPS_PROVIDER);
    	locListenerCoarse = new GpsLocationListener(Criteria.ACCURACY_COARSE, LocationManager.NETWORK_PROVIDER);
    }
    
    private void shutdownLoggerService() { 
    	if(isReqestingUpdates) locManager.removeUpdates(locListenerFine);
    } 


	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
	
	@Override	 
    public void onCreate() {
		super.onCreate();
		startLoggerService();
		if(showStatusToasts)Toast.makeText(this, "Starting GPS", Toast.LENGTH_SHORT).show();
     }
	
     @Override
     public void onDestroy() {
         shutdownLoggerService();
         // Tell the user we stopped.
         if(showStatusToasts) Toast.makeText(this, "Stopping GPS", Toast.LENGTH_SHORT).show();
         super.onDestroy();
     }
     
     public void onGetPoint(Message msg){
    	 if(singleSource != null) return;
    	 singleSource = msg.replyTo;
    	 if(singleTimeout < 1) singleTimeout = 10; //flag for collecting a single value
    	 if(! isReqestingUpdates){
	    	 registerLocationListeners();
    	 }    	 
     }
     
     public void sendSingle(Location location, int source){
    	 singleTimeout = 0;
    	 tryStopRequestingUpdates();
    	 if(singleSource == null) return;
    	 Message response = Message.obtain(null, GET_POINT);
    	 Bundle bundle = new Bundle(); //need to bundle up the point for the response
    	 bundle.putParcelable(KEY_LOCATION, location);
    	 bundle.putInt(KEY_SOURCE, source);
    	 //singleUpdate = null; // reset the single timeout params
    	 response.setData(bundle);
    	 try {
			singleSource.send(response);
			singleSource = null;
		} catch (RemoteException re) {
			Log.e(this.getClass().getSimpleName(), re.getMessage());
			singleSource = null;
		}
     }
     
     public void onLogStop(Message msg) throws RemoteException{
    	 isRecording = false;
    	 logState = LOG_STOP;
    	 tryStopRequestingUpdates();    	 
    	 Message response = Message.obtain(null, LOG_STOP);
    	 response.setData(bundleLocations());
    	 msg.replyTo.send(response);
     }
     
     private void onPassiveStart(Message msg){
    	 if(isPassive) return;
    	 if(! isReqestingUpdates){
    		 registerLocationListeners();
    	 }
    	 passiveSource = msg.replyTo;
    	 isPassive = true;
     }
     
     private void onPassiveStop(){
    	 if(isPassive){
    		 isPassive = false;
    	 }
    	 tryStopRequestingUpdates();
    	 passiveSource = null;
     }
     
     private void onCancel(){
    	 if(isRecording) isRecording = false;
    	 logState = LOG_STOP;
    	 passiveSource = null;
    	 singleSource = null;
    	 singleTimeout = 0;
    	 tryStopRequestingUpdates();    	 
     }
     
     private void sendPassive(Location location, int source){
    	 try{
	    	 Message response = Message.obtain(null, PASSIVE_MESSAGE);
	    	 Bundle bundle = new Bundle();
	    	 bundle.putParcelable(KEY_LOCATION, location);
	    	 response.setData(bundle);
	    	 passiveSource.send(response);
    	 }catch(RemoteException e) {
    		 onPassiveStop();
    		 Log.e("GPS", e.getMessage());
    	 }
     }
     
     /**
      * notifies this manager of a location update and which source it is from
      * @param accuracy
      * @param location
      */
     private void onLocationUpdate(int source, Location location){
    	 Log.d("GPS", "Enter Location Update");
    	 if(isRecording && location != null){
             if (source == SOURCE_FINE_GPS && location.hasAccuracy() && location.getAccuracy() <= minAccuracyMeters) {                	
//                 GregorianCalendar greg = new GregorianCalendar();
//                 TimeZone tz = greg.getTimeZone();
//                 int offset = tz.getOffset(System.currentTimeMillis());
//                 greg.add(Calendar.SECOND, (offset/1000) * -1);
                 locations.add(location);
	         }	
     	}
     	if(singleTimeout > 0){
     		if(source == SOURCE_FINE_GPS && location.hasAccuracy() && location.getAccuracy() <= minAccuracyMeters){
     			sendSingle(location, source);
     		}else{
     			if(location != null){
     				if(singleUpdate == null) singleUpdate = location;
     				else{ //set singleUpdate to most accurate found
     					if(location.getAccuracy() < singleUpdate.getAccuracy()) singleUpdate = location;
     				}
     			}
     			if(source == TIMEOUT){
     				singleTimeout = 1;
     			}
     			if(singleTimeout == 1){
     				//singleUpdate = location;
     				sendSingle(singleUpdate, source);
     			}
     		}
     	}
    	 if(isPassive){
     		sendPassive(location, source);
     	}
     }
     
     private void tryStopRequestingUpdates(){
    	 Log.d("gps","enter tryStopRequestingUpdates");
    	 if(isReqestingUpdates && ! (isPassive || isRecording || singleTimeout > 0)){
    		 Log.d("gps", "stopping updates");
    		 timeOutHandler.removeCallbacks(timeOutRunnable);
    		 locManager.removeUpdates(locListenerFine);
    		 locManager.removeUpdates(locListenerCoarse);
    		 isReqestingUpdates = false;
    	 }
     }
	
     private void registerLocationListeners(){
    	 Log.d("gps", "enter registerLocationListeners");
    	 if(isReqestingUpdates) return;
//    	 Criteria fine = new Criteria();
//    	 fine.setAccuracy(Criteria.ACCURACY_FINE);
//    	 Criteria coarse = new Criteria();
//    	 coarse.setAccuracy(Criteria.ACCURACY_COARSE);
    	 Log.d("gps", "requestingUpdates");
    	 Log.d("gps", "is gps enabled: " + Boolean.toString(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
    	 Log.d("gps", "is network enabled: " + Boolean.toString(locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
    	 locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMillis, minDistanceMeters, locListenerFine);
    	 locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeMillis, minDistanceMeters, locListenerCoarse);
    	 Log.d("gps", "is gps enabled: " + Boolean.toString(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
    	 Log.d("gps", "is network enabled: " + Boolean.toString(locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
    	 isReqestingUpdates = true;
    	 timeOutHandler.postDelayed(timeOutRunnable, 1000); //Give a second to fire things up
     }
     
     Handler timeOutHandler = new Handler();
     Runnable timeOutRunnable = new Runnable(){
    	 @Override
    	 public void run(){
    		 Log.d("gps", "enter run timer " + timeSinceUpdate);
    		 if(timeSinceUpdate++ >= timeOutInSeconds) onTimeOut();
    		 if(isReqestingUpdates) timeOutHandler.postDelayed(this, 1000);
    	 }
     };
     
     private void onTimeOut(){
    	 Log.d("gps", "enter onTimeOut");
    	 onLocationUpdate(TIMEOUT, null);
    	 timeSinceUpdate = 0;
     }
     
	public class GpsServiceMessageHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			try{
				switch(msg.what){
					case LOG_STOP:
						onLogStop(msg);
						break;
					case LOG_POINT:
						break;
					case LOG_LINE:
						break;
					case LOG_AREA:
						break;
					case GET_POINT:
						onGetPoint(msg);
						break;
					case PASSIVE_START:
						onPassiveStart(msg);
						break;
					case PASSIVE_STOP:
						onPassiveStop();
						break;
					case CANCEL:
						onCancel();
						break;
					default:
						break;
					}
			}catch(RemoteException e){
				Log.e("GPS", e.getMessage());
			}
		}			
	}
	
	private Bundle bundleLocations(){
		Bundle bundle = new Bundle();
		if(locations == null || locations.isEmpty()) bundle.putParcelable(Location.class.getName(), null);
		else{
			Location[] locs = (Location[]) locations.toArray();
			bundle.putParcelableArray(Location.class.getName(), locs);
		}
		return bundle;
	}
	
	public class GpsLocationListener implements LocationListener {
		
		private int accuracy;
		private Location previous;
		private int status;
		private String provider;
		
		public GpsLocationListener(int accuracy, String provider){
			this.accuracy = accuracy;
		}
		
		public Location getPrevious(){
			return previous;
		}
		
		public int getStatus(){
			return status;
		}
		
		public String getProvider(){ return provider; }
		
		@Override
        public void onLocationChanged(Location loc) {
			Log.d("gps", "onLocationChanged - " + provider);
        	onLocationUpdate(accuracy, loc);
        	previous = loc;        	
        } 

        @Override
        public void onProviderDisabled(String provider) {
            if(showStatusToasts) Toast.makeText(getBaseContext(), "onProviderDisabled: " + provider,
	            Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onProviderEnabled(String provider) {
        	if(showStatusToasts) Toast.makeText(getBaseContext(), "onProviderEnabled: " + provider,
            		Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        	Log.d("gps", "onStatusChanged - " + provider);
                String showStatus = null;
                if (status == LocationProvider.AVAILABLE)
                        showStatus = "Available";
                if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
                        showStatus = "Temporarily Unavailable";
                if (status == LocationProvider.OUT_OF_SERVICE)
                        showStatus = "Out of Service";
                if (status != this.status && showStatusToasts) {
                        Toast.makeText(getBaseContext(),
                                        "new status: " + showStatus,
                                        Toast.LENGTH_SHORT).show();
                }
                this.status = status;
        }
	}
	
}
