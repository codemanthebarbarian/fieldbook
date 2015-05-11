package com.amecfw.sage.ui;

import java.io.IOException;
import java.util.Date;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.util.ViewState;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

/**
 * The activity to associate pictures with entities. 
 * Must provide extras with a key to the SageApplication Cache for the PhotoProxy that contains the parent to 
 * associate the photo with (ARG_VIEW_PROXY_CACHE_KEY). Use PhotoService.createProxy() to get a proxy for a new
 * image.
 *
 */
public class PhotoActivity extends Activity {

	public static final String ARG_VIEW_STATE = "com.amecfw.sage.ui.PhotoActivity.viewState";
	public static final String ARG_VIEW_PROXY_CACHE_KEY = "com.amecfw.sage.ui.PhotoActivity.proxy";
	private static final int CAPTURE_IMAGE_REQUEST_CODE = 100;

	private ImageView imageView;
	private MetaDataListDialogFragment metaDataFragment;
	private PhotoProxy proxy;
	private ViewState viewState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(SageApplication.getInstance().getThemeID());
		setContentView(com.amecfw.sage.model.R.layout.photo_preview_edit);
		imageView = (ImageView) findViewById(com.amecfw.sage.model.R.id.photoActivity_imagepreview);
		if(savedInstanceState == null) initialize(getIntent().getExtras());
	}
	
	private void initialize(Bundle args){
		if(args.containsKey(ARG_VIEW_PROXY_CACHE_KEY)) 
			proxy =  SageApplication.getInstance().removeItem(args.getString(ARG_VIEW_PROXY_CACHE_KEY));
		viewState = args.getParcelable(ARG_VIEW_STATE);
		Bundle fragmentArgs = new Bundle();
		fragmentArgs.putInt(MetaDataListDialogFragment.ARG_EDIT_MODE, MetaDataListAdapter.VIEW_MODE_EDIT_VALUE);
		fragmentArgs.putBoolean(MetaDataListDialogFragment.ARG_ADD_BLANK, false);
		metaDataFragment = new MetaDataListDialogFragment();
		metaDataFragment.setMetaElements(PhotoService.convertFromProxy(proxy, null));
		metaDataFragment.setArguments(fragmentArgs);
		getFragmentManager()
			.beginTransaction()
			.add(com.amecfw.sage.model.R.id.photoActivity_metaData, metaDataFragment, MetaDataListDialogFragment.class.getName())
			.commit();
		if(viewState.getState() == ViewState.ADD) getNewPhoto();
		else editPhoto();
	}
	
	private void getNewPhoto(){
		PhotoService.setTempFile(proxy);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(proxy.getFile()));		
		startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
	}
	
	private void editPhoto(){
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(com.amecfw.sage.model.R.menu.add_delete_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == com.amecfw.sage.model.R.id.addDeleteMenu_save){
			doSave();
		}else if (id == com.amecfw.sage.model.R.id.addDeleteMenu_delete){
			doDelete();
		}else if(id == com.amecfw.sage.model.R.id.addDeleteMenu_add){
			doAdd();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void doDelete(){
		if(proxy.isTemporary()){
			doCancel(); //new item same as cancel
		} else { //Was an update, but no 
			//Need to delete the item
			setResult(RESULT_OK);
			this.finish();
		}
	}
	
	private void doAdd(){
		//Should send the item back and take another picture
	}
	
	private void doSave(){
		SageApplication.getInstance().setItem(ARG_VIEW_PROXY_CACHE_KEY, proxy);
		Intent intent = new Intent();
		intent.putExtra(ARG_VIEW_PROXY_CACHE_KEY, ARG_VIEW_PROXY_CACHE_KEY);
		setResult(RESULT_OK, intent);
		isDirty = false;
		this.finish();
	}
	
	private void doCancel(){
		if(proxy.isTemporary()){
			if(proxy.getFile() != null && proxy.getFile().exists()) proxy.getFile().delete();
			setResult(RESULT_CANCELED);
			this.finish();
		} else { //Was an update
			setResult(RESULT_CANCELED);
			this.finish();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		viewState = savedInstanceState.getParcelable(ARG_VIEW_STATE);
		proxy = SageApplication.getInstance().removeItem(ARG_VIEW_PROXY_CACHE_KEY);
		setImage();
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		SageApplication.getInstance().setItem(ARG_VIEW_PROXY_CACHE_KEY, proxy);
		outState.putParcelable(ARG_VIEW_STATE, viewState);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent){
		if(requestCode == CAPTURE_IMAGE_REQUEST_CODE){
			isDirty = true;
			setImage();
		}
	}
	
	private void setImage(){
		PhotoService.setImage(imageView, proxy);
	}

	public PhotoProxy getProxy() {
		return proxy;
	}

	public void setProxy(PhotoProxy proxy) {
		this.proxy = proxy;
	}

	
	private boolean isDirty = false;
	private boolean exit = false;
	private CancelSaveExitDialog.Listener cancelSaveExitDialogListener = new CancelSaveExitDialog.Listener() {		
		@Override
		public void onSave(CancelSaveExitDialog dialog) {
			doSave();
		}		
		@Override
		public void onExit(CancelSaveExitDialog dialog) {
			doCancel();		
		}		
		@Override
		public void onCancel(CancelSaveExitDialog dialog) { 
			//do nothing, just dismiss the dialog
		}
	};
	@Override
	public void onBackPressed() {
		if(exit){
			exit = false;
			super.onBackPressed();
		} else if(isDirty){
			CancelSaveExitDialog dialog = new CancelSaveExitDialog();
			dialog.setListener(cancelSaveExitDialogListener);
			dialog.show(getFragmentManager(), CancelSaveExitDialog.class.getName());
		} else super.onBackPressed();
	}

	@Override
	public boolean onNavigateUp() {
		if(exit){
			exit = false;
		}
		else if(isDirty) {
			CancelSaveExitDialog dialog = new CancelSaveExitDialog();
			dialog.setListener(cancelSaveExitDialogListener);
			dialog.show(getFragmentManager(), CancelSaveExitDialog.class.getName());
			return false;
		} 
		return super.onNavigateUp();
	}	
	
}
