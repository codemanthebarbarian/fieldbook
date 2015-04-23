package com.amecfw.sage.ui;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.R;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.service.CSVExportService;
import com.amecfw.sage.model.service.CSVImportService;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ManageDatabase extends ListActivity {

	private DatabaseManagementOptionsArrayAdapter adapter;
	private ListView optionsList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(SageApplication.getInstance().getThemeID());
		setContentView(android.R.layout.list_content);
		optionsList = (ListView) findViewById(android.R.id.list);
		adapter = new DatabaseManagementOptionsArrayAdapter(this, getOptions());
		optionsList.setAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(l != null){
			DatabaseManagementOptionsArrayAdapter.DatabaseOption option = adapter.getItem(position);
			switch(option.getCode()){ //See string resources file for codes
			case 0: //observationGroups
				Intent intent = new Intent(this, GroupObservationManagement.class);
				intent.putExtra(GroupObservationManagement.EXTRA_INITIAL_VIEW, GroupObservationManagement.VIEW_OBSERVATION_GROUPS);
				startActivity(intent);
				break;
			case 1://projects
				break;
			case 2:
				importData();
				break;
			case 3:
				exportData();
				break;
			}
		}
	}
	
	private void importData(){
		//Import elements for now
		Intent intent = new Intent(this, CSVImportService.class);
		intent.putExtra(CSVImportService.KEY_ZIP_FILE, "SageImport.zip");
		startService(intent);
	}
	
	private void exportData(){
		Intent intent = new Intent(this, CSVExportService.class);
		startService(intent);
	}

	private List<DatabaseManagementOptionsArrayAdapter.DatabaseOption> getOptions(){
		String[] optionNames = getResources().getStringArray(R.array.databaseManagement_options);
		int[] optionCodes = getResources().getIntArray(R.array.databaseManagement_optionCodes);
		List<DatabaseManagementOptionsArrayAdapter.DatabaseOption> items = 
				new ArrayList<DatabaseManagementOptionsArrayAdapter.DatabaseOption>(optionNames.length);
		for(int i = 0 ; i < optionNames.length ; i++){
			DatabaseManagementOptionsArrayAdapter.DatabaseOption option = new DatabaseManagementOptionsArrayAdapter.DatabaseOption();
			option.setName(optionNames[i]);
			option.setCode(optionCodes[i]);
			items.add(option);
		}
		return items;
	}
	
	
	
}
