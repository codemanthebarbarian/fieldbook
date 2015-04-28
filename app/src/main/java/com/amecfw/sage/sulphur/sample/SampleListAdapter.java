package com.amecfw.sage.sulphur.sample;

import java.util.List;

import com.amecfw.sage.fieldbook.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SampleListAdapter extends ArrayAdapter<SampleList.LocationProxy> {
	public SampleListAdapter(Context context, List<SampleList.LocationProxy> locations){
		super(context, R.layout.sulphur_location_list_item, locations);
	}
	
	static class ViewHolder {
		public TextView name;
		public TextView depths;
		public ImageView completed;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		LayoutInflater layout = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(convertView == null){
			convertView = layout.inflate(R.layout.sulphur_location_list_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.locationlistitem_name);
			holder.depths = (TextView) convertView.findViewById(R.id.locationlistitem_depths);
			holder.completed = (ImageView) convertView.findViewById(R.id.locationlistitem_completed);
			convertView.setTag(holder);
		}else holder = (ViewHolder) convertView.getTag();
		SampleList.LocationProxy proxy = getItem(position);
		if(proxy.getViewModel() == null){
			holder.name.setText("-");
			holder.depths.setText("-");
			holder.completed.setVisibility(ImageView.INVISIBLE);
		}else{
			holder.name.setText(proxy.getViewModel().getName());
			holder.depths.setText(proxy.getViewModel().getDepths());
			holder.completed.setVisibility(proxy.getStation() == null ? ImageView.INVISIBLE : ImageView.VISIBLE);
		}
		return convertView;
	}
	
}
