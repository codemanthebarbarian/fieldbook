package com.amecfw.sage.model;

import java.io.File;
import java.util.LinkedHashMap;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amecfw.sage.model.service.GpsLoggingService;
import com.amecfw.sage.persistence.DaoMaster;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.DatabaseManager;
import com.amecfw.sage.util.ApplicationCache;
import com.amecfw.sage.util.ApplicationGps;

/**
 * An interface that identifies a Sage application
 *
 */
public class SageApplication implements ApplicationCache, ApplicationGps {
	
	public static final String THEME_LIGHT = "LIGHT";
	public static final String THEME_DARK = "DARK";
	
	private static SageApplication instance;
	private Context context;
	private int themeID;
	private DaoMaster daoMaster;
	private ServiceConnection gpsServiceConnection;
	private Messenger gpsMessenger;
	private DaoSession daoSession;
	private LinkedHashMap<String, Object> cache;
	private File dirSDAppRoot;
	private File dirLocalAppRoot;
	private File dirSDTempExternal;
	private File dirSDTemp;
	private static final String DIRECTORY_SAGE_TEMP = "Sage" + File.separator + "Temp" + File.separator;
	private static final String DIRECTORY_SAGE = "Sage" + File.separator;
	
	private SageApplication(Context context){
		this.context = context;
		cache = new LinkedHashMap<String, Object>();
	}
	
	public static synchronized void initialize(Context applicationContext){
		if (instance != null) return;
		instance = new SageApplication(applicationContext);
		DatabaseManager dbManager = new DatabaseManager(applicationContext, applicationContext.getString(R.string.dbName), null);
		instance.daoMaster = new DaoMaster(dbManager.getWritableDatabase());
		instance.gpsServiceConnection = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				instance.gpsMessenger = new Messenger(service);				
			}
			@Override
			public void onServiceDisconnected(ComponentName name) {
				instance.gpsMessenger = null;			
			}	   		 
	   	 };
	   	instance.context.bindService(new Intent(instance.context, GpsLoggingService.class), instance.gpsServiceConnection, Context.BIND_AUTO_CREATE);
	   	instance.setTheme();
	   	PreferenceManager.getDefaultSharedPreferences(instance.context).registerOnSharedPreferenceChangeListener(instance.preferencesListener);
	}
	
	private void setTheme(){
		String key = context.getResources().getString(R.string.preferencesApp_themeKey);
		key = PreferenceManager.getDefaultSharedPreferences(context).getString(key, THEME_LIGHT);
		setTheme(key);
	}
	private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {		
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if(key.equals( context.getResources().getString(R.string.preferencesApp_themeKey))){
				String theme = sharedPreferences.getString(key, SageApplication.THEME_LIGHT);
				if(theme.equals(SageApplication.THEME_DARK)) themeID = R.style.AppTheme_Dark;
				else themeID = R.style.AppTheme_Light;
			}			
		}
	};
	
	public void setTheme(String value){
		if(value.equals(THEME_DARK)) SageApplication.getInstance().setThemeID(R.style.AppTheme_Dark);
		else SageApplication.getInstance().setThemeID(R.style.AppTheme_Light);
	}
	
	public void setThemeID(int themeID){
		this.themeID = themeID;
	}
	
	public static synchronized void dispose(){
		instance.context.unbindService(instance.gpsServiceConnection);
		instance = null;
	}
	
	public static SageApplication getInstance(){
		return instance;
	}
	
	public int getThemeID(){ return themeID; }

	public DaoMaster getDaoMaster(){
		return daoMaster;
	}
	
	public DaoSession getDaoSession(){
		if(daoSession == null) daoSession = daoMaster.newSession();
		return daoSession;
	}
	
	public SQLiteDatabase getDatabase(){
		return daoMaster.getDatabase();
	}
	
	@Override
	public Messenger getGpsMessenger(){
		return gpsMessenger;
	}
	
	@Override
	public ServiceConnection getGpsServiceConnection(){
		return gpsServiceConnection;
	}
	
	@Override
	public Object setItem(String key, Object item) {
		Object old = cache.get(key);
		cache.put(key, item);
		return old;
	}
	
	@Override
	public Object getObject(String key) {
		return cache.get(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getItem(String key){
		Object obj = getObject(key);
		if(obj == null) return null;
		T item;
		try{
			item = (T) obj;
		}catch(ClassCastException cce){
			return null;
		}
		return item;
	}
	
	@Override
	public Object remove(String key) {
		return cache.remove(key);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeItem(String key) {
		Object obj = remove(key);
		if(obj == null) return null;
		T item;
		try{
			item = (T) obj;
		}catch(ClassCastException cce){
			return null;
		}
		return item;
	}
	
	
	public File getDirSDTempExternal(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && 
				!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
			if (dirSDTempExternal == null) dirSDTempExternal = new File(context.getExternalFilesDir(null), DIRECTORY_SAGE_TEMP);
			if(! dirSDTempExternal.exists() && ! dirSDTempExternal.mkdir() ) dirSDTempExternal = null;
		} else dirSDTempExternal = null;
		return dirSDTempExternal;
	}
	
	
	/**
	 * convenience for context.getCacheDir();
	 * @return
	 */
	public File getCachedir(){ return context.getCacheDir(); }
	
	/**
	 * convenience for context.getExternalCachDir();
	 * @return
	 */
	public File getExternalCacheDir() { return context.getExternalCacheDir(); }
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public File getAvailableCacheDir(){
		File[] roots = context.getExternalCacheDirs();
		if(roots.length > 1){
			for(int i = roots.length -1 ; i >= 0 ; i--){
				if(Environment.getStorageState(roots[i]).equals(Environment.MEDIA_MOUNTED)) return roots[i];
			}
		}
		return roots[0];	
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public File getExternalStorageDirecotry(){
		File[] roots = context.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS);
		if(roots.length > 1){
			for(int i = roots.length -1 ; i >= 0 ; i--){
				if(Environment.getStorageState(roots[i]).equals(Environment.MEDIA_MOUNTED)) return roots[i];
			}
		}
		return roots[0];
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public File getPhotosDirectory(){
		File[] roots = context.getExternalFilesDirs(Environment.DIRECTORY_DCIM);
		if(roots.length > 1){
			for(int i = roots.length -1 ; i >= 0 ; i--){
				if(Environment.getStorageState(roots[i]).equals(Environment.MEDIA_MOUNTED)) return roots[i];
			}
		}
		return roots[0];
	}
	
	/**
	 * Tries to get a folder on a mounted SD card under the sage app, if no SD card mounted
	 * should return the public documents folder
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public File getExternalStoragePublicDirectory(){ 
		File[] roots = context.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS);
		if(roots.length > 1){
			for(int i = roots.length -1 ; i > 0 ; i--){
				if(Environment.getStorageState(roots[i]).equals(Environment.MEDIA_MOUNTED)) return roots[i];
			}
		}
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS); 
	}
	
	public File getDirHDTemp(){
		if(dirSDTemp == null){
			dirSDTemp = new File(context.getFilesDir(), DIRECTORY_SAGE_TEMP);
			if(! dirSDTemp.exists() && ! dirSDTemp.mkdir() ) dirSDTemp = null;
		}
		return dirSDTemp;
	}

	/**
	 * gets the folder for the application root directory on the SDCard, will be null
	 * if not SDCard is mounted.
	 * @return
	 */
	public File getDirSDAppRoot(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && 
				!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
			if (dirSDAppRoot == null){
				dirSDAppRoot = new  File(Environment.getExternalStoragePublicDirectory(null), DIRECTORY_SAGE);
//				if(! dirSDAppRoot.exists()) dirSDAppRoot = new File("/storage/sdcard1");
//				if(!dirSDAppRoot.exists()) dirSDAppRoot = new File("/storage/extSdCard");
//				if(!dirSDAppRoot.exists()) dirSDAppRoot = new File("/Removable/MicroSD");
				//TODO:need to figure out external storage fail safe Environment.getExternalStoragePublicDirectory
				if(!dirSDAppRoot.exists() && ! dirSDAppRoot.mkdir()){
					Log.e("FieldbookApplication", "Unable to locate external file system");
					dirSDAppRoot = null;
				} else{
					//Create the folder if it doesn't exist
					//dirSDAppRoot = new File(String.format("%s/%s", dirSDAppRoot.getAbsolutePath(), DIRECTORY_SAGE));
					//if(! dirSDAppRoot.exists()) dirSDAppRoot.mkdirs();
					dirSDAppRoot.setReadable(true, false);
					dirSDAppRoot.setWritable(true, false);
				}
			}
			return dirSDAppRoot;
		} else return null;
	}
	
	/**
	 * gets the folder for the applications accessible local root directory, can try to use this 
	 * if no SDCard is mounted, if there is an issue locating a directory, null will be returned
	 * @return
	 */
	public File getDirLocalAppRoot(){
		if(dirLocalAppRoot == null){
			dirLocalAppRoot = new File(Environment.getRootDirectory(), null);
			if(!dirLocalAppRoot.exists()) dirLocalAppRoot = new File("/");
			if(!dirLocalAppRoot.exists()){
				Log.e("FieldbookApplication", "Unable to locate external file system");
				dirLocalAppRoot = null;
			}
			if (dirLocalAppRoot.exists()){
				//Create the folder if it doesn't exist
				dirLocalAppRoot = new File(String.format("%s/%s", dirLocalAppRoot.getAbsolutePath(), "SageFieldbook"));
				if(! dirLocalAppRoot.exists()) dirLocalAppRoot.mkdirs();
				dirLocalAppRoot.setReadable(true, false);
				dirLocalAppRoot.setWritable(true, false);
			}
		}
		return dirLocalAppRoot;
	}
}
