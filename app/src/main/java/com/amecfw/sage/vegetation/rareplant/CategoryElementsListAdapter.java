package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.model.FieldDescriptor;
import com.amecfw.sage.model.MetaDataDescriptor;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.StationElement;
import com.amecfw.sage.model.StationElementMeta;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.model.service.GpsLoggingService;
import com.amecfw.sage.model.service.LocationService;
import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.proxy.ViewModelBaseEquatable;
import com.amecfw.sage.ui.ElementsMultiSelectListAdapter;
import com.amecfw.sage.ui.PhotoHorizontalListFragment;
import com.amecfw.sage.ui.PhotoListAdapter;
import com.amecfw.sage.ui.PhotoPathListAdapter;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.ListAdapter;
import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.util.OnEditListener;
import com.amecfw.sage.vegetation.transect.TransectEndEditFragment;

public class CategoryElementsListAdapter extends ListAdapter<CategoryElementsListAdapter.ViewModel> {
	
	private int viewMode;
	private boolean mIsDirty;
	
	public CategoryElementsListAdapter(Context context, List<ViewModel> elements, int elementListViewMode){
		super(context, elements);
		viewMode = elementListViewMode;
		mIsDirty = false;
		Collections.sort(elements, new ViewModelComparator(viewMode));
	}
	
	@Override
	public void setItems(List<ViewModel> items) {
		if(currentFocus != null) currentFocus.clearFocus();
		mIsDirty = false;
		Collections.sort(items, new ViewModelComparator(viewMode));
		super.setItems(items);
	}

	public boolean isDirty(){
		return mIsDirty;
	}
	
	@Override
	public long getItemId(int position) {
		return UUID.fromString(items.get(position).rowGuid).getMostSignificantBits();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.category_element_listitem, parent, false); //TODO: set the correct view
			holder = new ViewHolder();
			holder.displayName = (TextView) convertView.findViewById(android.R.id.text1);
			holder.notes = (EditText) convertView.findViewById(android.R.id.text2);
			holder.notes.setOnEditorActionListener(doneListener);
			holder.notes.setOnFocusChangeListener(focusChangeListener);
			holder.cover = (SeekBar) convertView.findViewById(R.id.category_elementView_seekbar);
			holder.cover.setOnSeekBarChangeListener(coverListener);
			holder.coverText = (TextView) convertView.findViewById(R.id.category_elementView_cover);
			holder.cover.setTag(R.id.sage_tag_list_view, holder.coverText);
			holder.coordinateText = (TextView) convertView.findViewById(R.id.category_elementView_coordinateText);
			holder.getCoordinate = (ImageButton) convertView.findViewById(R.id.category_elementView_locationButton);
			holder.getCoordinate.setOnClickListener(gpsButtonClickListener);
			convertView.setTag(R.id.sage_tag_list_viewHolder, holder);
			holder.getCoordinate.setTag(R.id.sage_tag_list_viewHolder, holder);
			holder.getPhoto = (ImageButton) convertView.findViewById(R.id.category_elementView_takePhoto);
			holder.photos = (LinearLayout) convertView.findViewById(android.R.id.list);
			holder.getPhoto.setOnClickListener(photoListener);
			holder.adapter = new PhotoPathListAdapter(context, new ArrayList<String>());
		}else holder = (ViewHolder) convertView.getTag(R.id.sage_tag_list_viewHolder);
		holder.notes.setTag(R.id.sage_tag_list_position, position);
		holder.cover.setTag(R.id.sage_tag_list_position, position);
		holder.getCoordinate.setTag(R.id.sage_tag_list_position, position);
		holder.getPhoto.setTag(R.id.sage_tag_list_position, position);
		ViewModel current = items.get(position);
		holder.displayName.setText(getDisplayName(current));
		holder.notes.setText(current.getComment());
		holder.cover.setProgress(current.getCover() / 5);
		holder.coverText.setText(Integer.toString(current.getCover()));
		if(current.location != null) holder.coordinateText.setText(LocationService.formatLocationText(current.location));
		setPhotos(current, holder);
		return convertView;
	}
	
	private String getDisplayName(ViewModel vm){
		switch (viewMode){
		case ElementsMultiSelectListAdapter.DISPLAY_SCODE_COMMON:
		case ElementsMultiSelectListAdapter.DISPLAY_SCODE_SCIENTIFIC:
			return vm.getScode();
		case ElementsMultiSelectListAdapter.DISPLAY_SCIENTIFIC_COMMON:
			return vm.getScientificName();
		default:
			return vm.getCommonName();
		}
	}

	public String getDisplayName(int position){ return getDisplayName(get(position)); }
	
	private View currentFocus;
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {	
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				currentFocus = v;
			}else{
				EditText editText = (EditText) v;
				updateNote((int)editText.getTag(R.id.sage_tag_list_position), Convert.toStringOrNull(editText));
				currentFocus = null;
			}
		}
	};
	
	private OnEditorActionListener doneListener = new OnEditorActionListener(){
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_DONE){
				EditText editText = (EditText)v;
				updateNote((int)editText.getTag( R.id.sage_tag_list_position), Convert.toStringOrNull(editText));
				return true;
			} else return false;
		}		
	};
	
	private void updateNote(int position, String text){
		ViewModel item = items.get(position);
		item.setComment(text);
		mIsDirty = true;
		onEdit(item);
	}
	
	private SeekBar.OnSeekBarChangeListener coverListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {	}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {	}
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if(fromUser){
				int position = (int) seekBar.getTag(R.id.sage_tag_list_position);
				updateCover(position, progress * 5, (TextView)seekBar.getTag(R.id.sage_tag_list_view));
			}
		}
	};
	
	private void updateCover(int postition, int cover, TextView textView){
		ViewModel item = items.get(postition);
		item.setCover(cover);
		textView.setText(Integer.toString(cover));
		mIsDirty = true;
		onEdit(item);
	}
	
	private void onEdit(ViewModel viewModel) { if(editListener != null) editListener.onEdit(viewModel); }
	private OnEditListener editListener;
	/**
	 * Set the edit listener
	 * @param listener
	 */
	public void setEditListener(OnEditListener listener){ editListener = listener; }
	/** An interface to allow watching listeners know when a list item has been edited. */
	public interface OnEditListener {
		/**
		 * the method raised when an item has been editied
		 * @param viewModel that was edited
		 */
		public void onEdit(ViewModel viewModel);
	}
	
	public static class ViewHolder{
		public TextView displayName;
		public EditText notes;
		public SeekBar cover;
		public TextView coverText;
		public ImageButton getCoordinate;
		public TextView coordinateText;
		public ImageButton getPhoto;
		public LinearLayout photos;
		public PhotoPathListAdapter adapter;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// GPS

	private GpsIncomingHandler gpsHandler;
	private void getCoordinate(ViewHolder view, int position){
		if(gpsHandler == null) gpsHandler = new GpsIncomingHandler(this);
		gpsHandler.setOnEditListener(gpsOnEditListener);
		gpsHandler.getCoordinate(view, get(position));
	}

	public void destroy() {
		if(gpsHandler != null){
			gpsHandler.destroy();
			gpsHandler = null;
		}
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

	private View.OnClickListener gpsButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			getCoordinate((ViewHolder) v.getTag(R.id.sage_tag_list_viewHolder), (int)v.getTag(R.id.sage_tag_list_position));
		}
	};

	static class GpsIncomingHandler extends Handler {

		private Location location;
		private ViewHolder viewHolder;
		private ViewModel viewModel;
		private Messenger gpsMessenger;
		public boolean isWaitingForGpsResponse;
		public CategoryElementsListAdapter adapter;
		private com.amecfw.sage.util.OnEditListener onEditListener;

		public GpsIncomingHandler(CategoryElementsListAdapter adapter) {
			gpsMessenger = SageApplication.getInstance().getGpsMessenger();
			this.adapter = adapter;
		}

		public void getCoordinate(ViewHolder holder, ViewModel viewModel){
			viewHolder = holder;
			this.viewModel= viewModel;
			sendCoordinateRequest();
		}

		public void setOnEditListener(com.amecfw.sage.util.OnEditListener listener){onEditListener = listener; }

		@Override
		public void handleMessage(Message m) {
			try {
				if (m.what == GpsLoggingService.GET_POINT) {
					location = (Location) m.getData().get(Location.class.getName());
					if (location == null)
						viewHolder.coordinateText.setText("GPS UNAVAILABLE");
					else {
						viewHolder.coordinateText.setText(LocationService.formatLocationText(location));
						viewModel.location = location;
						if(onEditListener != null) onEditListener.onDirty();
					}
				}
			} catch (Exception e) {
				viewHolder.coordinateText.setText("GPS ERROR");
			}
			isWaitingForGpsResponse = false;
		}

		public void sendCoordinateRequest() {
			Message msg = Message.obtain(null, GpsLoggingService.GET_POINT);
			msg.replyTo = new Messenger(this);
			try {
				isWaitingForGpsResponse = true;
				viewHolder.coordinateText.setText("Getting Location");
				gpsMessenger.send(msg);
			} catch (RemoteException re) {
				viewHolder.coordinateText.setText("GPS ERROR");
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
					viewHolder.coordinateText.setText("GPS ERROR");
					isWaitingForGpsResponse = true;
					Log.e(this.getClass().getSimpleName(), re.getMessage());
				}
			}
		}

		public void destroy(){
			if(isWaitingForGpsResponse) sendGpsCancel();
			gpsMessenger = null;
			location = null;
			viewHolder = null;
			onEditListener = null;
			adapter = null;
		}
	}

	// END GPS
	////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////
	// PHOTO

	private ActionEvent.Listener photoActionListener;

	/**
	 * An ActionEvent.Listener to provide photos. Should return the path to the photo with the
	 * provided position see addPhoto. The Postion of the item requesting the photo is provided
	 * int the ActionEvent arguments with a key of SageApplication.KEY_POSITION
	 * @param listener the action event listener
	 */
	public void setPhotoActionListener(ActionEvent.Listener listener){ photoActionListener = listener; }

	private View.OnClickListener photoListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(photoActionListener != null){
				Bundle args = new Bundle();
				args.putInt(SageApplication.KEY_POSITION, (int) v.getTag(R.id.sage_tag_list_position));
				photoActionListener.actionPerformed(PhotoService.takePhoto(args));
			}
		}
	};

	public void addPhoto(int postion, PhotoProxy proxy){
		ViewModel vm = get(postion);
		if(vm.photos == null) vm.photos = new String[]{proxy.getFile().getAbsolutePath()};
        else{
            vm.photos = Arrays.copyOf(vm.photos, vm.photos.length + 1);
            vm.photos[vm.photos.length -1] = proxy.getFile().getAbsolutePath();
        }
        onEdit(vm);
	}

	private void setPhotos(ViewModel vm, ViewHolder holder){
		if(vm.photos != null && vm.photos.length > 0){
			holder.adapter.setItems(Arrays.asList(vm.photos));
			holder.photos.removeAllViews();
			for(int i = 0 ; i < holder.adapter.getCount() ; i++){
				View v = holder.adapter.getView(i, null, holder.photos);
				holder.photos.addView(v);
			}
		}
	}

	// END PHOTO
	////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Sorting

	public static class ViewModelComparator implements Comparator<ViewModel> {

		private int viewMode;

		public ViewModelComparator(int viewMode){
			this.viewMode = viewMode;
		}

		@Override
		public int compare(ViewModel viewModel, ViewModel t1) {
			switch (viewMode){
				case ElementsMultiSelectListAdapter.DISPLAY_SCODE_COMMON:
				case ElementsMultiSelectListAdapter.DISPLAY_SCODE_SCIENTIFIC:
					return compareByScode(viewModel, t1);
				case ElementsMultiSelectListAdapter.DISPLAY_SCIENTIFIC_COMMON:
					return compareBySci(viewModel, t1);
				default:
					return compareByComm(viewModel, t1);
			}
		}

		private int compareByScode(ViewModel a, ViewModel b){
			return a.sCode.compareToIgnoreCase(b.sCode);
		}

		private int compareBySci(ViewModel a, ViewModel b){
			return a.scientificName.compareToIgnoreCase(b.scientificName);
		}

		private int compareByComm(ViewModel a, ViewModel b){
			return b.commonName.compareToIgnoreCase(b.commonName);
		}
	}

	// end sorting
	////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////
	// VIEW_MODEL
	
	public static class ViewModel extends ViewModelBaseEquatable{
		
		@FieldDescriptor(clazz=Element.class, targetGetter = "getId", targetSetter = "setId", type=DescriptorServices.TYPE_LONG, defaultValue = "0")
		private Long elementId;
		@FieldDescriptor(clazz=Element.class, targetGetter = "getScode", targetSetter = "setScode")
		private String sCode;
		@FieldDescriptor(clazz=Element.class, targetGetter = "getScientificName", targetSetter = "setScientificName")
		private String scientificName;
		@FieldDescriptor(clazz=Element.class, targetGetter = "getCommonName", targetSetter = "setCommonName")
		private String commonName;
		@MetaDataDescriptor(clazz=StationElementMeta.class, metaDataName="Comment")
		private String comment;
		@FieldDescriptor(clazz=StationElement.class, targetGetter = "getCount", targetSetter = "setCount")
		private String cover;
		public Location location;
		@FieldDescriptor(clazz=StationElement.class, targetGetter = "getRowGuid", targetSetter = "setRowGuid")
		private String rowGuid;
		/** a string array of photo paths */
		public String[] photos;

		public long getElementId() {
			return elementId;
		}

		public void setElementId(long elementId) {
			this.elementId = elementId;
		}

		public String getScode() {
			return sCode;
		}

		public void setScode(String sCode) {
			this.sCode = sCode;
		}

		public String getScientificName() {
			return scientificName;
		}

		public void setScientificName(String scientificName) {
			this.scientificName = scientificName;
		}

		public String getCommonName() {
			return commonName;
		}

		public void setCommonName(String commonName) {
			this.commonName = commonName;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public int getCover() {
			return cover == null || cover == new String() ? 0 : Integer.parseInt(cover);
		}

		public void setCover(int cover) {
			this.cover = Integer.toString(cover);
		}
		
		public String getRowGuid() {
			if(rowGuid == null || rowGuid == new String()) rowGuid = UUID.randomUUID().toString();
			return rowGuid;
		}

		public void setRowGuid(String rowGuid) {
			this.rowGuid = rowGuid;
		}

		public ViewModel(){}
		
		public ViewModel(Parcel in){
			elementId = in.readLong();
			sCode = in.readString();
			scientificName = in.readString();
			commonName = in.readString();
			comment = in.readString();
			cover = in.readString();
			rowGuid = in.readString();
			photos = new String[in.readInt()];
			in.readStringArray(photos);
			location = in.readParcelable(Location.class.getClassLoader());
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(elementId);
			dest.writeString(sCode);
			dest.writeString(scientificName);
			dest.writeString(commonName);
			dest.writeString(comment);
			dest.writeString(cover);
			dest.writeString(getRowGuid());
			dest.writeInt(photos == null ? 0 : photos.length );
			dest.writeStringArray(photos);
			dest.writeParcelable(location, PARCELABLE_WRITE_RETURN_VALUE);
		}
	}

	// END VIEW_MODEL
	////////////////////////////////////////////////////////////////////////////////////////////////
}
