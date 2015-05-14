package com.amecfw.sage.model.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.model.EntityBase;
import com.amecfw.sage.model.Photo;
import com.amecfw.sage.model.PhotoMeta;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.persistence.PersistanceUtilities;
import com.amecfw.sage.persistence.PhotoDao;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.ui.MetaDataListDialogFragment;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.CollectionOperations;
import com.amecfw.sage.model.EqualityComparatorOf;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

public class PhotoService {

	public static final String PHOTO_EXTENSION = Photo.PHOTO_EXTENSION;
	private DaoSession session;
	
	public PhotoService(DaoSession session){
		this.session = session;
	}
	
	public boolean saveOrUpdateInTransaction(PhotoProxy proxy){
		boolean result;
		SQLiteDatabase db = session.getDatabase();
		db.beginTransaction();
		try{
			saveOrUpdate(proxy);
			db.setTransactionSuccessful();
			result = true;
		}catch(Exception ex){
			Log.e(this.getClass().getName() ,ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
			result = false;
		}finally{
			db.endTransaction();
		}		
		return result;
	}
	
	public void saveOrUpdate(List<PhotoProxy> photos, EntityBase parent){
		List<PhotoProxy> persisted = convertToProxy(find(parent), parent);
		if(photos != null && photos.size() > 0){
			if(persisted == null || persisted.size() == 0){
				for(PhotoProxy pp : photos){
					pp.setParent(parent);
					saveOrUpdate(pp);
				}				
			}else{
				PhotoProxyRowGuidComparator comparator = new PhotoProxyRowGuidComparator();
				List<PhotoProxy> forDelete = CollectionOperations.except(persisted, photos, comparator);
				List<PhotoProxy> forAdd = CollectionOperations.except(photos, persisted, comparator);
				List<PhotoProxy> forUpdate = CollectionOperations.except(photos, forAdd, comparator);
				if(forAdd != null && forAdd.size() > 0){
					for(PhotoProxy pp : forAdd) {
						pp.setParent(parent);
						save(pp);
					}
				}
				if(forUpdate != null && forUpdate.size() > 0){
					for(PhotoProxy pp: forUpdate){
						PhotoProxy persistant = CollectionOperations.find(persisted, pp, comparator);
						persistant.getModel().setMetaData(pp.getModel().getMetaData());
						persistant.setFile(pp.getFile());
						persistant.setTemporary(pp.isTemporary());
						persistant.getModel().setDescription(pp.getModel().getDescription());
						persistant.getModel().setDateCollected(pp.getModel().getDateCollected());
						persistant.setCoordinate(pp.getCoordinate());
						update(persistant);
					}
				}
				if(forDelete != null && forDelete.size() > 0){
					for(PhotoProxy pp: forDelete)
						delete(pp);
				}
			}
		}else{
			if(persisted != null && persisted.size() > 0) for (PhotoProxy pp : persisted) delete(pp);
		}
	}
	
	public void saveOrUpdate(PhotoProxy proxy){
		if(proxy.getModel().getId() == null || proxy.getModel().getId() == 0) save(proxy);
		else update(proxy);
	}
	
	public void save(PhotoProxy proxy){
		if(proxy.isTemporary()){
			finalize(proxy);
		}
		save(proxy.getModel());
	}
	
	public void update(PhotoProxy proxy){
		if(proxy.isTemporary() && proxy.getFile() != null){
			moveImage(proxy.getFile()
					, new File(String.format("%s/%s", getPhotoDirectory().getAbsolutePath(), proxy.getModel().buildPath()))
					, proxy.getModel().buildFileName()
					, true);
		}
		update(proxy.getModel());
	}
	
	public int delete(EntityBase parent){
		int result = 0;
		List<Photo> photos = find(parent);
		if(photos == null) return result;
		result = photos.size();
		if(result > 0){
			for(Photo photo: photos){
				delete(photo);
			}
		}
		return result;
	}
	
	public void delete(List<PhotoProxy> proxies){
		if(proxies == null || proxies.size() < 1) return;
		for(PhotoProxy proxy: proxies) delete(proxy);
	}
	
	public void delete(PhotoProxy proxy){
		if(proxy.isTemporary() && proxy.getFile() != null){
			if(proxy.getFile().exists()) proxy.getFile().delete();
		}
		if(proxy.getModel().getId() != null && proxy.getModel().getId() > 0){
			delete(proxy.getModel());
		}
	}
	
	public void delete(Photo photo){
		File jpg = new File(photo.getPath());
		if(jpg.exists()) jpg.delete();
		MetaDataService.delete(photo, session.getPhotoMetaDao());
		session.getPhotoDao().delete(photo);
	}
	
	public void save(Photo photo){
		photo.setId(null);
		session.getPhotoDao().insert(photo);
		MetaDataService.save(photo, session.getPhotoMetaDao());
	}
	
	public void update(Photo photo){
		session.getPhotoDao().update(photo);
		MetaDataService.update(photo, session.getPhotoMetaDao());
	}
	
	public List<Photo> find(EntityBase parent){
		PhotoDao dao = session.getPhotoDao();
		return dao.queryBuilder().where(PhotoDao.Properties.ParentTable.eq(PersistanceUtilities.getTableName(parent))
				, PhotoDao.Properties.ParentID.eq(parent.getId())).list();
	}
	
	public static void finalize(PhotoProxy proxy){
		proxy.getModel().setParentID(proxy.getParent().getId());
		proxy.getModel().setParentTable(PersistanceUtilities.getTableName(proxy.getParent()));
		if(proxy.isTemporary()){
			File tmpLoc = proxy.getFile();
			File dest = new File(String.format("%s/%s", getPhotoDirectory().getAbsolutePath(), proxy.getModel().buildPath()));
			moveImage(tmpLoc, dest, proxy.getModel().buildFileName(), true);
			proxy.getModel().setPath(String.format("%s/%s", dest.getAbsolutePath(), proxy.getModel().buildFileName()));
			proxy.setTemporary(false);
		}
	}
	
	private static final int BUFFER = 16;
	/**
	 * 
	 * @param source the fully qualified path to the source
	 * @param toFolder the folder to move to (structure will be created if not existing)
	 * @param fileName the file name
	 * @param deleteSource true to delete the source file
	 * @return true if successful otherwise false
	 */
	private static boolean moveImage(File source, File toFolder, String fileName, boolean deleteSource){
		try {
			FileInputStream fis = new FileInputStream(source);
			BufferedInputStream input = new BufferedInputStream(fis, BUFFER);
			if(! toFolder.exists()) toFolder.mkdirs();
			File destination = new File(String.format("%s/%s", toFolder.getAbsolutePath(), fileName));
			FileOutputStream fout = new FileOutputStream(destination);
			byte data[] = new byte[BUFFER];
	 		int i;
	 		while((i = input.read(data, 0, BUFFER)) != -1){
				fout.write(data, 0, i);
			}
	 		input.close();
	 		fout.close();
	 		if(deleteSource) source.delete();
	 		return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		
	}
	
	public static List<PhotoProxy> convertToProxy(List<Photo> photos, EntityBase parent){
		if(photos == null || photos.size() == 0) return new ArrayList<PhotoProxy>();
		List<PhotoProxy> results = new ArrayList<PhotoProxy>(photos.size());
		for(Photo photo: photos){
			PhotoProxy proxy = new PhotoProxy();
			proxy.setModel(photo);
			proxy.setParent(parent);
			results.add(proxy);
		}
		return results;
	}

	public static String[] convertProxiesToFilePathArray(List<PhotoProxy> proxies){
		if(proxies == null) return null;
		if(proxies.size() == 0) return new String[0];
		String base = getPhotoDirectory().getAbsolutePath();
		String[] results = new String[proxies.size()];
		for(int i = 0 ; i < proxies.size() ; i++){
			PhotoProxy pp = proxies.get(i);
			if(pp.getFile() != null) results[i] =  pp.getFile().getAbsolutePath();
			else results[i] = String.format("%s/%s", base, pp.getModel().buildFilePath());
		}
		return results;
	}

	public static File getPhotoDirectory(){
		return SageApplication.getInstance().getPhotosDirectory();
	}
	
	public static void setTempFile(PhotoProxy proxy){
		File cacheFolder = SageApplication.getInstance().getAvailableCacheDir();
		proxy.setFile(new File(cacheFolder, proxy.getModel().getRowGuid() + PHOTO_EXTENSION));
	}
	
	public static void clearTemp(List<PhotoProxy> proxies){
		if(proxies == null || proxies.size() == 0) return;
		for(PhotoProxy pp: proxies){
			if(pp.isTemporary() && pp.getFile() != null && pp.getFile().exists()) pp.getFile().delete();
		}
	}
	
	public static PhotoProxy createProxy(EntityBase parent){
		PhotoProxy proxy = new PhotoProxy();
		Photo photo = new Photo();
		photo.setRowGuid();
		if(parent != null){
			proxy.setParent(parent);
			photo.setParentTable(PersistanceUtilities.getTableName(parent));
			if(parent.getId() != null && parent.getId() > 0) photo.setParentID(parent.getId());
		}
		proxy.setTemporary(true);
		proxy.setModel(photo);
		return proxy;
	}
	
	public static List<MetaDataListDialogFragment.ViewModel> convertFromProxy(PhotoProxy proxy, String[] definedMetaElements){
		List<MetaDataListDialogFragment.ViewModel> results = new ArrayList<MetaDataListDialogFragment.ViewModel>();
		if(proxy.getModel().hasMetaData()) 
			results.addAll(MetaDataService.convertToViewModel(proxy.getModel().getMetaData()));
		MetaDataService.MetaViewModelNameComparor comparator = new MetaDataService.MetaViewModelNameComparor();
		MetaDataListDialogFragment.ViewModel name = new MetaDataListDialogFragment.ViewModel();
		name.name = "Name";
		name.value = proxy.getModel().getName();
		CollectionOperations.addOrReplace(results, name, comparator);
		MetaDataListDialogFragment.ViewModel description = new MetaDataListDialogFragment.ViewModel();
		description.name = "Description";
		description.value = proxy.getModel().getDescription();		
		CollectionOperations.addOrReplace(results, description, comparator);		
		if(definedMetaElements != null){
			for(String metaName: definedMetaElements){
				MetaDataListDialogFragment.ViewModel tmp = new MetaDataListDialogFragment.ViewModel();
				tmp.name = metaName;
				int index = CollectionOperations.indexOf(results, tmp, comparator);
				if(index < 0) results.add(tmp);
			}
		}
		return results;
	}

	public static void setImageFromPath(ImageView container, String path){
		if(path == null) return;
		File f = new File(path);
		if(! f.exists()) return;
		Bitmap photo = BitmapFactory.decodeFile(path);
		if(photo == null) return;
		int rotate = 0;
		try {
			ExifInterface exif = new ExifInterface(path);
			float[] latLong = new float[2];
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch(orientation){
				case ExifInterface.ORIENTATION_ROTATE_90: rotate = 90 ; break;
				case ExifInterface.ORIENTATION_ROTATE_180: rotate = 180; break;
				case ExifInterface.ORIENTATION_ROTATE_270: rotate = 270; break;
			}
		} catch (IOException e) {
			Log.e("CameraActivity", "Unable to read exif tags.");
		}
		if(rotate > 0 ){
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
		}
		container.setImageBitmap(photo);
	}

	public static void setImage(ImageView container, PhotoProxy proxy){
		if(proxy == null) return;
		if(proxy.getFile() == null){
			if(proxy.getModel() == null || proxy.getModel().getPath() == null) return;
			File f = new File(proxy.getModel().getPath());
			if(f.exists()) proxy.setFile(f);
			else return;
		}
		Bitmap result = BitmapFactory.decodeFile(proxy.getFile().getAbsolutePath());
		proxy.getModel().setDateCollected(new Date(proxy.getFile().lastModified()));
		int rotate = 0;
		try {
			ExifInterface exif = new ExifInterface(proxy.getFile().getAbsolutePath());				
			float[] latLong = new float[2];
			if (exif.getLatLong(latLong)){
				Coordinate coord = new Coordinate();
				coord.setLatitude(Double.parseDouble(Float.toHexString(latLong[0])));
				coord.setLongitude(Double.parseDouble(Float.toString(latLong[1])));
				if(exif.getAltitude(Double.MIN_VALUE) > Double.MIN_VALUE){
					coord.setElevation(exif.getAltitude(Double.MIN_VALUE));
				}
				proxy.setCoordinate(coord);
				coord.setRowGuid();
			}
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch(orientation){
			case ExifInterface.ORIENTATION_ROTATE_90: rotate = 90 ; break;
			case ExifInterface.ORIENTATION_ROTATE_180: rotate = 180; break;
			case ExifInterface.ORIENTATION_ROTATE_270: rotate = 270; break;
			}
		} catch (IOException e) {
			Log.e("CameraActivity", "Unable to read exif tags.");
		}
		if(rotate > 0 ){
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
		}
		container.setImageBitmap(result);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ACTION EVENT

	public static final int PHOTO_RESULT_CODE = 100;
	public static final int COMMAND_TAKE_PHOTO = PHOTO_RESULT_CODE;
	public static final String ARG_PHOTO_PROXY_CACHE_KEY = "com.amecfw.sage.model.service.PhotoService.proxy";

	/**
	 * Create an ActionEvent with a ARG_COMMAND of COMMAND_TAKE_PHOTO.
	 * If the args is provided, will add the ARG_COMMAND, otherwise will
	 * create a new bundle with the command.
	 * @param args the arguments without the ARG_COMMAND
	 * @return the ActionEvent with the arg to take a photo
	 */
	public static ActionEvent takePhoto(Bundle args){
		if(args == null) args = new Bundle();
		args.putInt(ActionEvent.ARG_COMMAND, COMMAND_TAKE_PHOTO);
		return ActionEvent.getActionDoCommand(args);
	}

	/**
	 * Create an ActionEvent with an ARG_COMMAND of PHOTO_RESULT_CODE.
	 * Puts an argument with key of ARG_PHOTO_PROXY_CACHE_KEY to retrieve the photo proxy
	 * from the SageApplication cache. Use remove to get the proxy.
	 * @param args the argument with additional values.
	 * @param proxy the photoproxy
	 * @return an actionevent
	 */
	public static ActionEvent addProxy(Bundle args, PhotoProxy proxy){
		if(args == null) args = new Bundle();
		args.putString(ARG_PHOTO_PROXY_CACHE_KEY, ARG_PHOTO_PROXY_CACHE_KEY);
		SageApplication.getInstance().setItem(ARG_PHOTO_PROXY_CACHE_KEY, proxy);
		args.putInt(ActionEvent.ARG_COMMAND, PHOTO_RESULT_CODE);
		return ActionEvent.getActionDoCommand(args);
	}

	// END ACTION EVENT
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class PhotoProxyRowGuidComparator implements EqualityComparatorOf<PhotoProxy>{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof PhotoProxy) || !(objB instanceof PhotoProxy)) return false;
			PhotoProxy a = (PhotoProxy) objA;
			PhotoProxy b = (PhotoProxy) objB;
			return equals(a, b);
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null)	return 0;
			if(!(obj instanceof PhotoProxy)) return 0;
			PhotoProxy o = (PhotoProxy) obj;
			return getHash(o);
		}

		@Override
		public boolean equals(PhotoProxy a, PhotoProxy b) {
			if(a == null || b == null) return false;
			if(a.getModel() == null || b.getModel() == null) return false;
			return a.getModel().getRowGuid().equals(b.getModel().getRowGuid());
		}

		@Override
		public int getHash(PhotoProxy obj) {
			if(obj == null)	return 0;
			if(obj.getModel() == null) return 0;
			return obj.getModel().getRowGuid().hashCode();
		}
		
	}
	
}
