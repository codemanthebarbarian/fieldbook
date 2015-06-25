package com.amecfw.sage.model.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.EntityBase;
import com.amecfw.sage.model.MetaElement;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.util.CollectionOperations;
import com.amecfw.sage.util.Convert;
import com.opencsv.CSVReader;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class CSVImportService extends IntentService {

	private static final int BUFFER = 16;
	private static final int notificationId = CSVImportService.class.getName().hashCode();
	private Notification.Builder nBuilder;
	private NotificationManager nManager;
	public static final String KEY_ZIP_FILE = "sage.model.service.CSVImportService.zipFile";
	private DaoSession session;
	private String[] objectHierarcy;
	private File tempFolder;
	private ZipEntry[] zipFiles;
	private ZipFile archive;
	
	public CSVImportService(){
		super("CSVImportService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent){
		session = SageApplication.getInstance().getDaoSession();
		try {
			//android.os.Debug.waitForDebugger();
			Log.d("CSV", "Starting Import");
			sendNotificationIndeterminate("Starting Import");
			String fileName = intent.getStringExtra(KEY_ZIP_FILE);
			archive = getImport(fileName);
			extractTables();
			importTables();
			archive.close();
			Log.d("CSV", "Completed Import");
			sendNotification("Import Completed");
		}catch(FileNotFoundException e){
			Log.d("CSV", "Import failed");
			sendNotification("Export Failed unable to locate import");
			Log.d("CSV", e.getClass().getName());
			Log.e("CSVExportService", e.getLocalizedMessage());
		}catch (IOException e) {
			Log.d("CSV", "Import failed");
			sendNotification("Export Failed file access error");
			Log.d("CSV", e.getClass().getName());
			Log.e("CSVExportService", e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			Log.d("CSV", "Import failed");
			sendNotification("Export Failed unrecognized data");
			Log.d("CSV", e.getClass().getName());
			Log.e("CSVExportService", e.getLocalizedMessage());
		}catch (Exception e){
			Log.d("CSV", "Import failed");
			sendNotification("Export unknown issue");
			Log.d("CSV", e.getClass().getName());
			Log.e("CSVExportService", e.getLocalizedMessage());
		}
	}
	
	private ZipFile getImport(String fileName) throws FileNotFoundException, IOException{
		File folder = null;
		if(fileName == null) fileName = "SageImport.zip";
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			folder = SageApplication.getInstance().getExternalStoragePublicDirectory();
			tempFolder = getExternalCacheDir();
			if(!folder.exists()) folder = null;
		}
		if(folder == null){
			folder = SageApplication.getInstance().getDirLocalAppRoot();
			tempFolder = getCacheDir();
			if(!folder.exists()) return null;
		}
		File file = new File(folder.getAbsolutePath() + "/" + fileName);
		if(! file.exists()) return null;
		return new ZipFile(file.getAbsolutePath());
	}
	
	private void extractTables() throws ClassNotFoundException, IOException{
		sendNotificationIndeterminate("Reading Zip Archive");
		objectHierarcy = getResources().getStringArray(com.amecfw.sage.model.R.array.objectTableHierarchy);
		zipFiles = new ZipEntry[objectHierarcy.length];	
		Enumeration<? extends ZipEntry> entries = archive.entries();
		while (entries.hasMoreElements()){
			ZipEntry ze = entries.nextElement();
			if(ze.isDirectory()){
				//Do nothing for now might need this for photos and files
			}else{
				String name = ze.getName();
				if(isCsv(name)){
					String className = name.substring(0, name.length() - 4);
					int i = CollectionOperations.indexOfStringArray(objectHierarcy, className, false);
					if(i == -1) throw new  ClassNotFoundException(name + " Not Found");
					zipFiles[i] = ze;
				}
			}
		}
	}
	
	private void importTables() throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, NoSuchFieldException{
		for(int i = 0 ; i < zipFiles.length ; i++){
			String obj = objectHierarcy[i];
			if(zipFiles[i] != null){
				sendNotificationIndeterminate(String.format("Processing %s records", obj));
				CsvRecordSet recordSet = buildRecordset(zipFiles[i], obj);
				if(obj.endsWith("Meta")) saveUpdateMetaRecords(recordSet);
				else saveUpdateRecords(recordSet);
			}			
		}
	}
	
	private <T extends EntityBase> void saveUpdateRecords(CsvRecordSet recordSet) 
			throws ClassNotFoundException, IOException, IllegalAccessException, IllegalArgumentException, NoSuchFieldException{
		if(recordSet == null || recordSet.records.size() == 0) return;
		int pmax = recordSet.records.size() /10;
		int pgress = 0;
		int i = 0;
		sendProgress(pmax, pgress, String.format("Saving %s records", recordSet.clazz.getSimpleName()));
		Log.d("CSV",  String.format("Saving %s records", recordSet.clazz.getSimpleName()));
		HashMap<Property, CsvRecordSet> joins = getJoins(recordSet.dao.getProperties(), recordSet);
		for(CsvRecord record: recordSet.records){
			T entity = convert(record, recordSet.clazz, recordSet.dao.getProperties());
			if(entity.getRowGuid() == null) entity.setRowGuid(); //no rowguid, imported from a fresh source
			if(joins != null) setJoins(entity, joins); ; //Set Joins
			EntityBase persistant = getPersistedRecord(entity.getRowGuid(), recordSet.dao);
			if(persistant != null){
				entity.setId(persistant.getId());
				updatePersistant(entity, persistant, recordSet.dao.getProperties());
				session.update(entity);
			} else {
				entity.setId(null); //clear out the ID
				session.insert(entity);
			}
			if(++i / 10 > pgress) sendProgress(pmax, pgress = (i / 10));
		}
	}
	
	private <T extends EntityBase> void setJoins(T entity, HashMap<Property, CsvRecordSet> joins) 
			throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException{
		if(entity == null || joins == null || joins.size() == 0) return;
		for(Property property: joins.keySet()){
			Field field = entity.getClass().getDeclaredField(property.name);
			field.setAccessible(true);
			Object o = field.get(entity);
			if(o != null){
				Long pID = (Long)o;
				if(pID != null && joins.get(property) != null) {
					Long persistedId = joins.get(property).getPersistedRecordId(pID);
					field.set(entity, persistedId);
				}
			}
		}
	}
	
	private boolean isFK(Property property){
		return property.name.endsWith("ID");
	}
	private boolean isSelfFK(Property property){
		return property.name.equals("rootID");
	}
	
	private HashMap<Property, CsvRecordSet> getJoins(Property[] properties, CsvRecordSet self) throws IOException, ClassNotFoundException{
		HashMap<Property, CsvRecordSet> joins = new HashMap<Property, CsvRecordSet>();
		for(Property property: properties){
			if(isFK(property)){
				CsvRecordSet records = null;
				if(!isSelfFK(property)) records = buildRecordset(property.name.substring(0, property.name.length() - 2), true);
				else records = self;
				joins.put(property, records);
			}
		}
		return joins.size() == 0 ? null : joins;
	}
	
	private <T extends EntityBase> void saveUpdateMetaRecords(CsvRecordSet metaRecords) throws ClassNotFoundException, IOException{
		if(metaRecords == null || metaRecords.records.size() == 0) return;
		String parentClass = metaRecords.clazz.getSimpleName().substring(0, metaRecords.clazz.getSimpleName().length() - 4);
		CsvRecordSet parents = buildRecordset(parentClass, false);
		if(parents == null || parents.records.size() == 0) return;
		int pmax = metaRecords.records.size() /10;
		int pgress = 0;
		int i = 0;
		sendProgress(pmax, pgress, String.format("Saving %s records", metaRecords.clazz.getSimpleName()));
		for(CsvRecord record: metaRecords.records){
			T entity = convert(record, metaRecords.clazz, metaRecords.dao.getProperties());
			if(entity.getRowGuid() == null) entity.setRowGuid(); //no rowguid, imported from a fresh source
			EntityBase persistant = getPersistedRecord(entity.getRowGuid(), metaRecords.dao);
			if(persistant != null){
				entity.setId(persistant.getId());
				updatePersistant(entity, persistant, metaRecords.dao.getProperties());
				session.update(entity);
			} else {
				//Link to the persisted parent
				Long parentID = parents.getPersistedRecordId(((MetaElement)entity).getParentID());
				if(parentID != null){
					((MetaElement) entity).setParentID(parentID);
					entity.setId(null); //clear out the ID
					session.insert(entity);
				}
			}
			if(++i / 10 > pgress) sendProgress(pmax, pgress = (i / 10));
		}
	}
	
	private LinkedList<CsvRecord> processTable(String className, ZipEntry file) throws IOException{
		LinkedList<CsvRecord> records = new LinkedList<CsvRecord>();
		if(file == null) return records; //no file = no records
		CSVReader reader = new CSVReader(new InputStreamReader(archive.getInputStream(file)));
		String[] columns;
		String[] nextLine;
		//first line should be columns
		columns = reader.readNext();
		nextLine = reader.readNext();
		while(nextLine != null){
			CsvRecord record = new CsvRecord(className, columns, nextLine);
			if(record.isValid()) records.add(record);
			nextLine = reader.readNext();
		}
		reader.close();
		return records;
	}	
	
	private static boolean isCsv(String name){
		if(name == null || name.isEmpty()) return false;
		return name.endsWith(".csv");
	}
	
	private void writeTemp(ZipInputStream zip, File file) throws IOException{
		FileOutputStream fout = new FileOutputStream(file);
		byte data[] = new byte[BUFFER];
 		int i;
		while((i = zip.read(data)) != -1){
			fout.write(data, 0, i);;
		}
		zip.closeEntry();
		fout.close();
	}
	

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
	
	private <T extends EntityBase> void updatePersistant(T trans, T persistant, Property[] properties) {
		for(Property property: properties){
			if(!(property.name.endsWith("ID") || property.name.equalsIgnoreCase("id") || property.name.equalsIgnoreCase("rowGuid"))) 
				updateFieldPersistant(trans, persistant, property);
		}
	}
	
	private void updateFieldPersistant(Object trans, Object persistant, Property property){
		Field field;
		try {
			field = trans.getClass().getDeclaredField(property.name);
			field.setAccessible(true);
			field.set(persistant, field.get(trans));
		} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException  e) {
			// TODO Auto-generated catch block
		}
	}
	
	private static Property findPropertyByFieldName(Property[] properties, String fieldName){
		for(Property p: properties){
			if(p.name.equalsIgnoreCase(fieldName)) return p;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends EntityBase> T convert(CsvRecord record, Class<?> clazz, Property[] properties){
		T result;
		try {
			result = (T) clazz.newInstance();
			for(int i = 0 ; i < record.getColumns().length ; i++){
				Property property = findPropertyByFieldName(properties, record.getColumns()[i]);
				if(property != null && record.getValues()[i] != null) {
					Field field = clazz.getDeclaredField(property.name);
					Object val = parseDefault(record.getValues()[i], property.type);
					if(val != null){
						field.setAccessible(true);
						field.set(result, parseDefault(record.getValues()[i], property.type));
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException | ClassCastException e) {
			return null;
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return result;
	}
	
	private static String getRowGuid(List<CsvRecord> records, Long id){
		if(records == null || records.size() < 1) return null;
		int idIndex = CollectionOperations.indexOfStringArray(records.get(0).getColumns(), "id", false);
		int guidIndex = CollectionOperations.indexOfStringArray(records.get(0).getColumns(), "rowGuid", false);
		for(CsvRecord record: records){
			Long parentId = Convert.tryParseLong(record.getValues()[idIndex]);
			if(parentId.equals(id)) return record.getValues()[guidIndex];
		}
		return null;
	}
	
	private static Object parseDefault(String value, Class<?> type) throws ParseException{
		if(value == null) return null;
		String name = value.trim();
		if(name.isEmpty()) return null;
		if(type.equals(String.class)) return name;
		if(type.equals(Date.class)) return DateFormat.getDateTimeInstance().parse(name);
		if(type.equals(Long.class) || type.equals(long.class)) return Long.parseLong(name);
		if(type.equals(Integer.class) || type.equals(int.class)) return Integer.parseInt(name);
		if(type.equals(UUID.class)) return UUID.fromString(name);
		return null;
	}
	
	private CsvRecordSet buildRecordset(String obj, boolean ignoreCase) throws ClassNotFoundException, IOException{
		int index = CollectionOperations.indexOfStringArray(objectHierarcy, obj, ignoreCase);
		if(index == -1) return null;
		String object = objectHierarcy[index];
		return buildRecordset(zipFiles[index], object);
	}
	
	private CsvRecordSet buildRecordset(ZipEntry zipEntry, String obj) throws IOException, ClassNotFoundException{
		LinkedList<CsvRecord> records = processTable(obj, zipEntry);
		if(records == null) return null;
		Class<?> clazz = Class.forName("com.amecfw.sage.model." + obj );
		AbstractDao<?, ?> dao = session.getDao(clazz);
		if(dao == null) return null;
		CsvRecordSet result = new CsvRecordSet();
		result.records = records;
		result.dao = dao;
		result.clazz = clazz;
		return result;
	}
	
	/**
	 * return the first persisted record with a matching rowGuid
	 * @param rowGuid The entity's rowguid
	 * @param dao the dao to search with
	 * @return
	 */
	private static EntityBase getPersistedRecord(String rowGuid, AbstractDao<?,?> dao){
		if(rowGuid == null) return null;
		List<?> results = dao.queryRaw(String.format("where %s = ?",  CSVImportService.findPropertyByFieldName(dao.getProperties(), "rowGuid").columnName)
				, rowGuid);
		if(results == null || results.size() < 1) return null;
		return (EntityBase) results.get(0);
	}
	
	private static class CsvRecordSet{
		private LinkedList<CsvRecord> records;
		private AbstractDao<?,?> dao;
		private Class<?> clazz;

		public Long getPersistedRecordId(Long recordsetID){
			CsvRecord record = getRecordByID(recordsetID);
			if(record == null) return -1L;
			int guidIndex = CollectionOperations.indexOfStringArray(records.get(0).getColumns(), "rowGuid", false);
			String rowGuid = record.getValues()[guidIndex];
			if(rowGuid == null) return -1L;
			EntityBase persisted = CSVImportService.getPersistedRecord(rowGuid, dao);
			return persisted == null ? 0l : persisted.getId();
		}
		
		public CsvRecord getRecordByID(Long id){
			if(records == null || records.size() < 1) return null;
			int idIndex = CollectionOperations.indexOfStringArray(records.get(0).getColumns(), "id", false);
			for(CsvRecord record: records){
				Long parentId = Convert.tryParseLong(record.getValues()[idIndex]);
				if(parentId.equals(id)) return record;
			}
			return null;
		}
		
		public CsvRecord getRecordByRowGuid(String rowGuid){
			if(records == null || records.size() < 1) return null;
			int guidIndex = CollectionOperations.indexOfStringArray(records.get(0).getColumns(), "rowGuid", false);
			for(CsvRecord record: records){
				String parentGuid = record.getValues()[guidIndex];
				if(rowGuid.equals(parentGuid)) return record;
			}
			return null;
		}
		
	}
	
	public static class CsvRecord{
		public static final String PACKAGE_NAME = "com.amecfw.sage.model";
		private String className;
		private String[] columns;
		private String[] values;
		
		public CsvRecord(String className, String[] columns, String[] values){
			this.className = className;
			this.columns = columns;
			this.values = values;
		}
		
		public String getClassName(){
			return className;
		}
		
		public String getFullClassName(){
			return PACKAGE_NAME + "." + className; 
		}
		
		public String[] getColumns(){
			return columns;
		}
		
		public String[] getValues(){
			return values;
		}
		
		/**
		 * Checks to see if the columns.length equals the values.length.
		 * If not, returns false. The number of values must equal the number of columns
		 * for a record to be valid
		 * @return
		 */
		public boolean isValid(){
			return columns.length == values.length;
		}
	}

}
