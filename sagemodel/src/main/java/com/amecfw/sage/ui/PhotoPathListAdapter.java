package com.amecfw.sage.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.util.ListAdapter;

import java.util.List;

/**
 * Created by amec on 2015-05-13.
 */
public class PhotoPathListAdapter extends ListAdapter<String> {

    public PhotoPathListAdapter(Context context, List<String> elements){
        super(context, elements);
    }

    private class ViewHolder{
        public ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(com.amecfw.sage.model.R.layout.photo_horizontal_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) convertView;
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();
        String item = get(position);
        PhotoService.setImageFromPath(viewHolder.image, item);
        return convertView;
    }


}
