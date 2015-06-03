package com.amecfw.sage.ui;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.util.ListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by amec on 2015-04-30.
 */
public class ElementsSingleClickListAdapter extends ListAdapter<Element> implements Filterable {

    public static final int DISPLAY_SCODE_SCIENTIFIC = 1;
    public static final int DISPLAY_SCODE_COMMON = 2;
    public static final int DISPLAY_SCIENTIFIC_COMMON = 3;
    public static final int DISPLAY_COMMON_SCIENTIFIC = 4;

    private int displayMode = 1;

    public ElementsSingleClickListAdapter(Context context, List<Element> elements, int displayMode){
        super(context, elements);
        if(displayMode < 1 || displayMode > 4) this.displayMode = 1;
        else this.displayMode = displayMode;
        filter = new ElementFilter();
        original = new ArrayList<Element>(elements);
    }

    @Override
    public void setItems(List<Element> items) {
        super.setItems(items);
        original.clear();
        original.addAll(items);
    }

    public void setDisplayMode(int displayMode){
        if(displayMode < 1 || displayMode > 4) this.displayMode = 1;
        else this.displayMode = displayMode;
    }

    private static class ViewHolder{
        public TextView text1;
        public TextView text2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            LayoutInflater layout = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layout.inflate(android.R.layout.simple_list_item_2, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder, viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag(com.amecfw.sage.model.R.id.sage_tag_list_viewHolder);
        Element item = get(position);
        setHolder(viewHolder, item);
        //convertView.setTag(com.amecfw.sage.model.R.id.sage_tag_list_position, position);
        return convertView;
    }


    private void setHolder(ViewHolder holder, Element item){
        switch(displayMode){
            case DISPLAY_SCODE_SCIENTIFIC:
                setText(holder, item.getScode(), item.getScientificName());
                break;
            case DISPLAY_SCODE_COMMON:
                setText(holder, item.getScode(), item.getCommonName());
                break;
            case DISPLAY_SCIENTIFIC_COMMON:
                setText(holder, item.getScientificName(), item.getCommonName());
                break;
            case DISPLAY_COMMON_SCIENTIFIC:
                setText(holder, item.getCommonName(), item.getScientificName());
                break;
        }
    }

    private void setText(ViewHolder holder, String text1, String text2){
        holder.text1.setText(text1);
        holder.text2.setText(text2);
    }

    // Filter code

    @Override
    public Filter getFilter() {
        return filter;
    }

    private ElementFilter filter;
    ArrayList<Element> original;

    private class ElementFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String trimmed = constraint.toString().trim();
            FilterResults retVal = new FilterResults();
            ArrayList<Element> results = new ArrayList<Element>();
            if (trimmed != null) {
                if (original != null && original.size() > 0) {
                    // check SCODE, scientific name and common name
                    switch (displayMode){
                        case DISPLAY_SCIENTIFIC_COMMON:
                            for (Element element : original) {
                                if (matches(element.getScientificName(), trimmed)
                                        || matches(element.getCommonName(), trimmed))
                                    results.add(element);
                            }
                            break;
                        case DISPLAY_COMMON_SCIENTIFIC:
                            for (Element element : original) {
                                if (matches(element.getCommonName(), trimmed)
                                        || matches(element.getScientificName(), trimmed))
                                    results.add(element);
                            }
                            break;
                        case DISPLAY_SCODE_SCIENTIFIC:
                            for (Element element : original) {
                                if (matches(element.getScode(), trimmed)
                                        || matches(element.getScientificName(), trimmed))
                                    results.add(element);
                            }
                            break;
                        case DISPLAY_SCODE_COMMON:
                            for (Element element : original) {
                                if (matches(element.getScode(), trimmed)
                                        || matches(element.getCommonName(), trimmed))
                                    results.add(element);
                            }
                            break;
                        default:
                            for (Element element : original) {
                                if (matches(element.getScode(), trimmed)
                                        || matches(element.getScientificName(), trimmed)
                                        || matches(element.getCommonName(), trimmed))
                                    results.add(element);
                            }
                    }
                }
            }
            retVal.values = results;
            retVal.count = results.size();
            return retVal;
        }

        private Boolean matches(String code, CharSequence value) {
            if (code.toLowerCase(Locale.getDefault()).startsWith(value.toString().toLowerCase(Locale.getDefault())))
                return true;

            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items.clear();
            items.addAll((ArrayList<Element>) results.values);
            notifyDataSetChanged();
        }
    }
}
