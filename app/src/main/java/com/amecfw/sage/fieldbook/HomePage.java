package com.amecfw.sage.fieldbook;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amecfw.sage.fieldbook.R;
//import com.amecfw.sage.soil.SoilsMainActivity;
//import com.amecfw.sage.sulphur.SulphurMainActivity;
import com.amecfw.sage.vegetation.VegetationMainActivity;


public class HomePage extends Fragment implements OnClickListener{
	
	private TextView sulphurButton;
	private TextView vegButton;
	private TextView soilButton;
	
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
		 	View view = inflater.inflate(R.layout.home_page, container, false);
		 	sulphurButton = (TextView) view.findViewById(R.id.sulphurApp_welcomeMsg);
		 	sulphurButton.setOnClickListener(this);
		 	vegButton = (TextView) view.findViewById(R.id.vegApp_welcomeMsg);
		 	vegButton.setOnClickListener(this);
		 	soilButton = (TextView) view.findViewById(R.id.soilApp_welcomeMsg);
		 	soilButton.setOnClickListener(this);
	    return view;
	}
	
	@Override
	public void onClick(View v) {
        if (v.getId() == vegButton.getId()) {
            Intent intent = new Intent(getActivity(), VegetationMainActivity.class);
            getActivity().startActivity(intent);

        }
//		if(v.getId() == sulphurButton.getId()){
//			Intent intent = new Intent(getActivity(), SulphurMainActivity.class);
//			getActivity().startActivity(intent);
//		}else if (v.getId() == vegButton.getId()){
//			Intent intent = new Intent(getActivity(), VegetationMainActivity.class);
//			getActivity().startActivity(intent);
//		}else if(v.getId() == soilButton.getId()){
//			Intent intent = new Intent(getActivity(), SoilsMainActivity.class);
//			getActivity().startActivity(intent);
//		}
	}
	
}
