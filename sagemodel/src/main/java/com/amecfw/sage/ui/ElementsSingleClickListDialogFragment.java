package com.amecfw.sage.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.util.ActionEvent;
import com.amecfw.sage.util.OnItemSelectedHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amec on 2015-04-30.
 */
public class ElementsSingleClickListDialogFragment  extends DialogFragment implements ActionEvent.Listener {

    private List<Element> elements;
    private ListView list;
    private ElementsSingleClickListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.amecfw.sage.model.R.layout.simple_list_layout, container, false);
        initialize(view, savedInstanceState == null ? getArguments() : savedInstanceState);
        return view;
    }

    private void initialize(View view, Bundle args){
        if(elements == null) elements = new ArrayList<Element>();
        adapter = new ElementsSingleClickListAdapter(getActivity(), elements, ElementsMultiSelectListAdapter.DISPLAY_COMMON_SCIENTIFIC);
        list = (ListView) view.findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(itemClickListener);
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            view.callOnClick();
            if(externalItemClickListener != null) externalItemClickListener.onItemClick(parent, view, position, id);
            if(onItemSelectedHandler != null) onItemSelectedHandler.onItemSelected(adapter.get(position));
        }
    };
    private AdapterView.OnItemClickListener externalItemClickListener;
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        externalItemClickListener = listener;
    }

    public void setElements(List<Element> elements){
        this.elements = (elements == null ? new ArrayList<Element>() : elements);
        if(adapter != null) adapter.setItems(this.elements);
    }

    private OnItemSelectedHandler<Element> onItemSelectedHandler;
    public void setOnItemSelectedHandler(OnItemSelectedHandler<Element> handler){
        onItemSelectedHandler = handler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(e.getAction()){
            case ActionEvent.SAVE:

                break;
        }
    }

    public SearchView.OnQueryTextListener getOnQueryTextListener(){ return queryListener; }
    SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if(adapter != null){
                adapter.getFilter().filter(query);
                return true;
            }
            return false;
        }
        @Override
        public boolean onQueryTextChange(String newText) {
            if(adapter != null){
                adapter.getFilter().filter(newText);
                return true;
            }
            return false;
        }
    };

}
