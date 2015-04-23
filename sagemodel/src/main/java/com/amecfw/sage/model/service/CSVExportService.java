package com.amecfw.sage.model.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.Photo;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.persistence.DaoSession;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class CSVExportService extends IntentService {
	private static final int BUFFER = 16;
	private String[] objectHierarcy;
		
	public CSVExportService(){
		super("CSVExportService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent){
		DaoSession session = SageApplication.getInstance().getDaoSession();
		ZipOutputStream output = null;
		try {
			//android.os.Debug.waitForDebugger();
			Log.d("CSV", "Starting Export");
			sendNotificationIndeterminate("Starting Export");
			output = createWriter();
			writeDatabase(session, output);
			Log.d("CSV", "Completed Export");
			sendNotification("Export Completed");
		}catch(ClassNotFoundException e){
			Log.d("CSV", "Export failed");
			sendNotification("Export Failed: Unknown Class");
			Log.d("CSV", e.getClass().getName());
			Log.e("CSVExportService", e.getMessage() == null ? e.toString() : e.getMessage());
		}catch (IOException e) {
			Log.d("CSV", "Export failed");
			sendNotification("Export Failed");
			Log.d("CSV", e.getClass().getName());
			Log.e("CSVExportService", e.getMessage() == null ? e.toString() : e.getMessage());
		}finally{
			if(output != null)
				try{
					output.close();
				}catch(IOException ioe){}
		}
	}
	
	
	private void writeDatabase(DaoSession session, ZipOutputStream zipStream) throws IOException, ClassNotFoundException{
		objectHierarcy = getResources().getStringArray(com.amecfw.sage.model.R.array.objectTableHierarchy);
		File temp = File.createTempFile("DataExport", null);
		Log.d("CSV", "Temp at: " + temp.getAbsolutePath());
		for(String obj: objectHierarcy){
			sendNotificationIndeterminate(String.format("Exporting %s records", obj));
			Class<?> clazz = Class.forName("com.amecfw.sage.model." + obj );
			AbstractDao<?, ?> dao = session.getDao(clazz);
			zipStream.putNextEntry(writeTable(clazz.getSimpleName(), dao.loadAll(), dao.getProperties(), temp));
			putFile(zipStream, temp);
		}
		sendNotificationIndeterminate("Exporting photos");
		putPhotos(zipStream, session);
		zipStream.finish();
		zipStream.close();
		if(temp.exists()) temp.delete();
	}
	
	private void putFile(ZipOutputStream zipStream, File file) throws IOException{
		try{
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream input = new BufferedInputStream(fis, BUFFER);
			byte data[] = new byte[BUFFER];
			int i;
			while ((i = input.read(data, 0, BUFFER)) != -1) {
				zipStream.write(data, 0, i);
			}
			input.close();
			zipStream.closeEntry();
		}catch(FileNotFoundException fnf){
			zipStream.closeEntry();
		}
	}
	
	private ZipOutputStream createWriter() throws IOException{
		File folder = SageApplication.getInstance().getExternalStoragePublicDirectory();
		if(!folder.exists()) folder.createNewFile();
		File file = new File(folder, "SageExport.zip");
		if(!file.exists())file.createNewFile();
//		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//			//Write to External storage
//			file = SageApplication.getInstance().getExternalStoragePublicDirectory();
//			Log.d("CSV", "Can write to folder " + Boolean.toString(file.canWrite()));
//			if(!file.exists()) file.mkdirs();
//			file = new File(file.getAbsolutePath() + "/SageExport.zip");
//			try{
//				file.createNewFile();
//			}catch(IOException ioe){
//				//issue with external try internal
//				Log.e("CSV", "Error writing to SD");
//				file = null;
//			}
//		}
//		if(file == null){
//			//Write to local data directory
//			file = SageApplication.getInstance().getDirLocalAppRoot();	
//			if(!file.exists()) file.mkdirs();
//			file = new File(file.getAbsolutePath() + "/SageExport.zip");
//			file.createNewFile();
//		}
		FileOutputStream dest = new FileOutputStream(file.getAbsolutePath());
		return new ZipOutputStream(new BufferedOutputStream(dest));
	}
	
	private void putPhotos(ZipOutputStream zipStream, DaoSession session) throws IOException {
		List<Photo> photos = session.getPhotoDao().loadAll();
		if(photos == null) return;
		int pmax = photos.size();
		int pgress = 0;
		sendProgress(pmax, pgress, "Exporting photos");
		for (Photo photo : photos) {
			if(photo.getPath() != null){
				File pic = new File(photo.getPath());
				Log.d("CSV", "Photo - " + pic.getAbsolutePath());
				Log.d("CSV", "Photo Exists " + Boolean.toString(pic.exists()));
				if(pic.exists()){
					zipStream.putNextEntry(new ZipEntry(photo.buildFilePath()));
					putFile(zipStream, pic);
				}
			}
			sendProgress(pmax, ++pgress);
		}

	}
	
	private <TObject, TPropertyClass> ZipEntry writeTable(String tableName, List<TObject> items, Property[] properties, File tempFile) throws IOException{
		Log.d("CSV", "Writing Table: " + tableName);
		FileWriter writer = new FileWriter(tempFile);
		writer.write(writeProperties(properties));
		if(items != null){
			int pmax = items.size() / 10;
			int pgress = 0;
			int i = 0;
			for (TObject obj : items) {
				writer.write(writeValues(properties, obj));
				if(++i / 10 > pgress) sendProgress(pmax, pgress = (i / 10));
			}
		}
		//writer.flush();
		writer.close();
		return new ZipEntry(tableName + ".csv");
	}
	
	private static String writeProperties(Property[] properties){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < properties.length; i++) {
			sb.append(writeValue(properties[i].columnName));
			if(i < properties.length - 1) sb.append(writeValue());
			else sb.append(writeEnd());
		}
		return sb.toString();
	}
	
	private static <TObject> String writeValues(Property[] properties, TObject source){
		StringBuilder sb = new StringBuilder();
		try {
			for (int i = 0; i < properties.length; i++) {
				Property p = properties[i];
				Field f = source.getClass().getDeclaredField(p.name);
				f.setAccessible(true);
				if(p.type.equals(Long.class)) sb.append(writeValue((Long) f.get(source)));
				else if(p.type == String.class) sb.append(writeValue((String)f.get(source)));
				else if(p.type == Date.class){
					Date date = (Date)f.get(source);
					sb.append(String.format("%s %s", writeDateValue(date), writeTimeValue(date)));
				}else if (p.type == Float.class) sb.append(writeValue((Float) f.get(source)));
				else sb.append(writeValue(f.get(source)));
				if(i < properties.length - 1) sb.append( writeValue());
				else sb.append(writeEnd());
			}
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
			Log.d("CSVExportService", String.format("Error Processing Values: %s", e.getMessage()));
			return String.format("%s%s",writeValue("ERROR PROCESSING VALUES"), writeEnd());
		}
		return sb.toString();
	}
	
	private static String writeValue(){
		return ",";
	}
	private static String writeEnd(){
		return "\r\n";
	}
	
	private static String writeValue(String string){
		if (string == null) return String.format("%s", "NULL");
		return String.format("\"%s\"", string);
	}
	
	private static String writeValue(Object obj){
		if(obj == null) return String.format("%s", "NULL");
		return String.format("%s", obj.toString());
	}
	
	private static String writeValue(Float number){
		if(number == null) return String.format("%s", "NULL");
		return String.format("%f", number);
	}
	
	private static String writeValue(Long number){
		if (number == null) return String.format("%s", "NULL");
		return String.format("%d", number);
	}
	
	private static String writeDateValue(Date date){
		if (date == null) return String.format("%s", "NULL");
		return String.format("%td-%tb-%tY", date, date, date);
	}
	
	private static String writeTimeValue(Date date){
		if (date == null) return String.format("%s", "NULL");
		return String.format("%tH:%tM:%tS", date, date, date);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// NOTIFICATIONS
	private Notification.Builder nBuilder;
	private NotificationManager nManager;
	private static final int notificationId = CSVExportService.class.getName().hashCode();

	private void sendNotification(String message){
		if(nBuilder == null) buildNotificationBldr();
		nBuilder.setContentText(message);
		nBuilder.setProgress(0, 0, false);//remove any progress
		nManager.notify(notificationId, nBuilder.build());
	}
	
	private void sendProgress(int max, int progress){
		if(nBuilder == null) buildNotificationBldr();
		nBuilder.setProgress(max, progress, false);
		nManager.notify(notificationId, nBuilder.build());
	}
	
	private void sendProgress(int max, int progress, String message){
		if(nBuilder == null) buildNotificationBldr();
		nBuilder.setContentText(message);
		nBuilder.setProgress(max, progress, false);
		nManager.notify(notificationId, nBuilder.build());
	}
	
	private void sendNotificationIndeterminate(String message){
		if(nBuilder == null) buildNotificationBldr();
		nBuilder.setContentText(message);
		nBuilder.setProgress(0, 0, true);//remove any progress
		nManager.notify(notificationId, nBuilder.build());
	}
	
	private void buildNotificationBldr(){
		if(nManager == null) nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nBuilder = new Notification.Builder(this)
			.setContentTitle("Sage Fieldbook")
			.setSmallIcon(R.drawable.afw);
		//Set a non intent (we don't want to have a specific return intent when the user clicks the notifiation
		PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
		nBuilder.setContentIntent(resultPendingIntent);
	}	
}
