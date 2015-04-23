package com.amecfw.sage.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amecfw.sage.model.service.PhotoService;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.util.ListAdapter;

public class PhotoListAdapter extends ListAdapter<PhotoProxy> {

	public PhotoListAdapter(Context context, List<PhotoProxy> items) {
		super(context, items);
		// TODO Auto-generated constructor stub
	}
	
	private static class ViewHolder{
		ImageView image;
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
		PhotoProxy item = get(position);
		PhotoService.setImage(viewHolder.image, item);
		return convertView;
	}

}
