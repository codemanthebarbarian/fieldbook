package com.amecfw.sage.fieldbook;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.amecfw.sage.util.ListAdapter;
import com.amecfw.sage.util.OnItemSelectedHandler;

import java.util.ArrayList;
import java.util.List;

/**
 *  A dialog fragment for selecting survey types
 */
public class SurveySelectorFragment extends DialogFragment {

    public static final String ARG_ARRAY_LIST_VIEW_MODEL = "com.amecfw.sage.fieldbookSurveySelectorFragment.viewModels";

    private ListView list;
    private ViewModelAdapter adapter;
    private ArrayList<ViewModel> viewModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(android.R.layout.list_content, container, false);
        list = (ListView) view.findViewById(android.R.id.list);
        if(savedInstanceState == null) initialize(getArguments());
        else restoreFromInstanceState(savedInstanceState);
        return view;
    }

    private void initialize(Bundle args){
        if(args == null) if(viewModels == null) viewModels = new ArrayList<>();
        else viewModels = args.getParcelableArrayList(ARG_ARRAY_LIST_VIEW_MODEL);
        adapter = new ViewModelAdapter(getActivity(), viewModels);
        list.setAdapter(adapter);
        list.setOnItemClickListener(itemClickListener);
    }

    private void restoreFromInstanceState(Bundle savedInstanceState){
        viewModels = savedInstanceState.getParcelableArrayList(ARG_ARRAY_LIST_VIEW_MODEL);
        if(adapter == null) adapter = new ViewModelAdapter(getActivity(), viewModels);
        else adapter.setItems(viewModels);
        list.setAdapter(adapter);
        list.setOnItemClickListener(itemClickListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_ARRAY_LIST_VIEW_MODEL, new ArrayList<>(adapter.getItems()));
    }

    /**
     * Set the viewModels for the fragment
     * @exception NullPointerException if viewModels is null
     * @param viewModels
     */
    public void setViewModels(List<ViewModel> viewModels){
        if(adapter == null) adapter = new ViewModelAdapter(getActivity(), viewModels);
        else adapter.setItems(viewModels);
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(onItemSelectedHandler != null) onItemSelectedHandler.onItemSelected(adapter.get(i));
        }
    };

    private OnItemSelectedHandler<ViewModel> onItemSelectedHandler;

    /**
     * Set the OnItemSelectedHandler to respond to what is selected.
     * @param handler
     */
    public void setOnItemSelectedHandler(OnItemSelectedHandler<ViewModel> handler){
        onItemSelectedHandler = handler;
    }

    /**
     * a convenience method to generate the viewmodels.
     * The calling activity would likely have the viewmodels as a string-array resource.
     * the string would be the name and the index would be the id.
     * @param source the viewmodel source (names)
     * @return the generated viewmodels
     */
    public static ArrayList<ViewModel> generateViewModels(String[] source){
        ArrayList<ViewModel> result = new ArrayList<>(source.length);
        for(int i = 0 ; i < source.length ; i++){
            result.add(new ViewModel(i, source[i]));
        }
        return result;
    }

    public static class ViewModel implements com.amecfw.sage.proxy.ViewModel{
        public int id;
        public String text;

        public ViewModel(){}

        public ViewModel(int id, String text){
            this.id = id;
            this.text = text;
        }

        public ViewModel(Parcel in){
            id = in.readInt();
            text = in.readString();
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
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(id);
            parcel.writeString(text);
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private class ViewModelAdapter extends ListAdapter<ViewModel>{

        public ViewModelAdapter(Context context, List<ViewModel> viewModels){ super(context, viewModels); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}
