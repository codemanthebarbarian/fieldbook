package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;
import java.util.Arrays;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.CollectionOperations;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.OnItemSelectedHandler;
import com.amecfw.sage.fieldbook.R;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A fragment for displaying the categories associated with a rare plant survey
 */
public class CategoryFragment extends Fragment implements ActionEvent.Listener {	
	
	private ViewModel[] categoryViewModels;
	/** The key for putting a single category as a ViewModel in the command argument bundle (see COMMAND_UPDATE_CATEGORY) */
	public static final String ARG_CATEGORY_VIEW_MODEL = "vegetation.rareplant.CategroyFragment.viewModel";
	/** The key for putting all the category ViewModels in the command argument bundle (see COMMAND_UPDATE_ALL) */
	public static final String ARG_ALL_CATEGORY_VIEW_MODELS = "vegetation.rareplant.CategroyFragment.categoryViewModels";
	private Button[] categoryButtons;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.category_fragment, container, false);
		if(savedInstanceState != null) {
			categoryViewModels = Convert.convert(savedInstanceState.getParcelableArray(ARG_ALL_CATEGORY_VIEW_MODELS), ViewModel[].class);
		}
		generateCategories(inflater, view);
		return view;
	}
	
	private void generateCategories(LayoutInflater inflater, View view){
		if(categoryViewModels == null) return;
		categoryButtons = new Button[categoryViewModels.length];
		ViewGroup container = (ViewGroup) view.findViewById(R.id.categoryFragment_container);
		for(int i = 0 ;i < categoryViewModels.length ; i++ ){
			Button v = (Button) inflater.inflate(R.layout.category_fragment_button, container, false);
			v.setText(categoryViewModels[i].toString());
			v.setTag(categoryViewModels[i]);
			v.setOnClickListener(onCategroyClickHandeler);
			categoryButtons[i] = v;
			container.addView(v);
		}
	}
	
	public void setCategoryViewModels(ViewModel[] viewModels){
		categoryViewModels = viewModels;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArray(ARG_ALL_CATEGORY_VIEW_MODELS, categoryViewModels);
		super.onSaveInstanceState(outState);
	}

	
	/**
	 * Supports ActionEvent.DO_COMMAND (other actions are ignored)
	 * to update a single category use CategoryFragment.COMMAND_UPDATE_CATEGORY for the ActionEvent.ARG_COMMAND
	 * an the ViewModel with key CategoryFragment.ARG_CATEGORY_VIEW_MODEL
	 * 
	 * to update all categories use CategoryFragment.COMMAND_UPDATE_ALL for the ActionEvent.ARG_COMMAND key
	 * and an ArrayList[ViewModel]  with key CategoryFragment.ARG_ALL_CATEGORY_VIEW_MODELS 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getAction()){
		case ActionEvent.DO_COMMAND:
			Bundle args = e.getArgs();
			if(args != null){
				int command = args.getInt(ActionEvent.ARG_COMMAND);
				switch (command){
				case COMMAND_UPDATE_ALL:
					doUpdateAll(args);
					break;
				case COMMAND_UPDATE_CATEGORY:
					doUpdateCategory(args);
					break;
				}
			}
			break;
		}		
	}
	/** The argument for the DO_COMMAND for updating a single category
	 * the ViewModel to update must be included in the bundle (see ARG_CATEGORY_VIEW_MODEL) */
	public static final int COMMAND_UPDATE_CATEGORY = 1;
	private void doUpdateCategory(Bundle args){
		ViewModel category = args.getParcelable(ARG_CATEGORY_VIEW_MODEL);
		if(category != null) doUpdateCategory(category);
	}
	
	private void doUpdateCategory(ViewModel viewModel){
		int index = CollectionOperations.indexOf(Arrays.asList(categoryViewModels), viewModel, new ViewModelNameComparator());
		if(index < 0) return;
		categoryViewModels[index].setElementCount(viewModel.getElementCount());
		categoryButtons[index].setText(viewModel.toString());
	}
	
	/** The argument for the DO-_COMMAND for updating all the category,
	 * an array of ViewModels must be included in the bundle (see COMMAND_UPDATE_ALL) */
	public static final int COMMAND_UPDATE_ALL = 2;
	private void doUpdateAll(Bundle args){
		ArrayList<ViewModel> categories = args.getParcelableArrayList(ARG_ALL_CATEGORY_VIEW_MODELS);
	}
	
	private OnClickListener onCategroyClickHandeler = new OnClickListener(){
		@Override
		public void onClick(View v) { if( categorySelectedHandler != null) categorySelectedHandler.onItemSelected((ViewModel) v.getTag()); }		
	};

	private OnItemSelectedHandler<ViewModel> categorySelectedHandler;
	public void setOnCategorySelectedHandler(OnItemSelectedHandler<ViewModel> handler){
		categorySelectedHandler = handler;
	}
	
	public static class ViewModel extends com.amecfw.sage.proxy.ViewModelBaseEquatable{
		private String categoryName;
		private int elementCount;
		
		public ViewModel(){}
		
		public String getCategoryName() {
			return categoryName;
		}


		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}


		public int getElementCount() {
			return elementCount;
		}


		public void setElementCount(int elementCount) {
			this.elementCount = elementCount;
		}


		public ViewModel(Parcel in){
			categoryName = in.readString();
			elementCount = in.readInt();
		}
		
		public static final Parcelable.Creator<ViewModel> CREATOR = 
				new Parcelable.Creator<ViewModel>(){
			@Override
			public ViewModel createFromParcel(Parcel in) {return new ViewModel(in); }
			@Override
			public ViewModel[] newArray(int size) { return new ViewModel[size]; }
		};

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(categoryName);
			dest.writeInt(elementCount);
		}

		@Override
		public String toString() {
			return elementCount < 1 ? categoryName : String.format("%s (%d)", categoryName, elementCount);
		}
	}
	
	public static class ViewModelNameComparator implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null ) return false;
			if(!(objA instanceof ViewModel)) return false;
			if(!(objB instanceof ViewModel)) return false;
			ViewModel a = (ViewModel) objA;
			ViewModel b = (ViewModel) objB;
			if(a.categoryName == null || b.categoryName == null) return false;
			return a.categoryName.equalsIgnoreCase(b.categoryName);
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null)	return 0;
			if(!(obj instanceof ViewModel)) return obj.hashCode();
			ViewModel o = (ViewModel) obj;
			if(o.categoryName == null) return 0;
			return o.categoryName.toUpperCase().hashCode();
		}
		
	}
	
}
