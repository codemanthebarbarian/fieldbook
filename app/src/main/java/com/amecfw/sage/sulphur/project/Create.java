package com.amecfw.sage.sulphur.project;

import java.util.List;

import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.Project;
import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Site;
import com.amecfw.sage.proxy.Model;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.fieldbook.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class Create extends Activity implements OnItemSelectedListener {

	public final static String VIEW_STATE_EXTRA = "viewstate";
	public final static String PROJECT_SITE_CACHE_KEY = "ProjectSite.Create.ProjectSite";
	public final static String VMPROXY_CACHE_KEY = "ProjectSite.Create.VMProxy";
	private VMProxy proxy;
	private ViewState viewState;
	private EditText projectNumber;
	private EditText projectName;
	private ListView fieldCrew;
	private Spinner siteNames;
	private String selectedSite;
	private EditText siteName;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 setTheme(SageApplication.getInstance().getThemeID());
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.sulphur_create_project);
		 projectNumber = (EditText) findViewById(R.id.createProject_txtProjectNum);
		 projectName = (EditText) findViewById(R.id.createProject_txtProjectName);
		 fieldCrew = (ListView) findViewById(R.id.createProject_lstFieldCrew);
		 siteNames = (Spinner) findViewById(R.id.createProject_sponnerSiteName);
		 siteName = (EditText) findViewById(R.id.createProject_txtSiteName);
		 siteNames.setOnItemSelectedListener(this);
		 siteNames.setAdapter(getSiteAdapter());
		 if(savedInstanceState != null) initialize(savedInstanceState);
		 else initialize();
	  }
	 
	 private void initialize(){
		 viewState = getIntent().getParcelableExtra(VIEW_STATE_EXTRA);
		 if(viewState == null) viewState = ViewState.getViewStateAdd();
		 switch (viewState.getState()){
		 case ViewState.ADD:
			 initAdd();
			 break;
		 case ViewState.EDIT:
			 initEdit();
			 break;
		 default:
			 initAdd();
			 break;
		 }
	 }
	 
	 /**
	  * prepare the form for adding a new project
	  */
	 private void initAdd(){
		 proxy = new VMProxy();
	 }
	 
	 /**
	  * prepare the form for editing an existing project
	  */
	 private void initEdit(){
		 
	 }
	 
	 private void initialize(Bundle savedInstanceState){
		 viewState = savedInstanceState.getParcelable(VIEW_STATE_EXTRA);
		 if(viewState == null) viewState = ViewState.getViewStateAdd();
		 Object tmpProxy = SageApplication.getInstance().getObject(VMPROXY_CACHE_KEY);
		 if(tmpProxy == null) proxy = new VMProxy();
		 if(!(tmpProxy instanceof VMProxy)) proxy = new VMProxy();
		 else {
			 proxy = (VMProxy) tmpProxy;
			 updateView();
		 }
	 }
	 
	 private void updateView(){
		 ViewModel vm = proxy.getViewModel();
		 if(vm != null){
			 projectNumber.setText(vm.getProjectNumber());
			 projectName.setText(vm.projectName);
			 selectedSite = vm.siteName;
			 //Set selected Item and list of fieldcrew
		 }
	 }
	 
	 public void onClick_Save(View v) {	
		 proxy.setViewModel(getViewModel());
		 switch (viewState.getState()){
		 case ViewState.ADD:
			 createProjectSite();
			 break;
		 case ViewState.EDIT:
			 //updateProjectSite();
			 break;
		 }
		 finish();		 	
	 }
	 
	 private void createProjectSite(){
		 proxy.setViewModel(getViewModel());
		 Services service = new Services(SageApplication.getInstance().getDaoSession());
		 service.syncFromViewModel(proxy);
		 service.save(proxy);
	 }
	 
	 private ViewModel getViewModel(){
		 ViewModel vm = new ViewModel();
		 String tmp = projectNumber.getText().toString();
		 if(tmp != null && !tmp.isEmpty()) vm.setProjectNumber(tmp.trim());
		 tmp = projectName.getText().toString();
		 if(tmp != null && !tmp.isEmpty()) vm.setProjectName(tmp.trim());
		 tmp = siteName.getText().toString();
		 if(tmp != null && !tmp.isEmpty()) vm.setSiteName(tmp.trim());
		 return vm;
	 }
	 
	 @Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		int viewId = parent.getId();
		if(viewId == R.id.createProject_sponnerSiteName){
			siteName.setText(parent.getItemAtPosition(position).toString());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		//Don't do anything		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		 outState.putParcelable(VIEW_STATE_EXTRA, viewState);
		 SageApplication.getInstance().setItem(VMPROXY_CACHE_KEY, proxy);
		 super.onSaveInstanceState(outState);
	}
	
	private ArrayAdapter<String> getSiteAdapter(){
		Services services = new Services(SageApplication.getInstance().getDaoSession());
		ArrayAdapter<String> siteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, services.getSiteNames());
		return siteAdapter;
	}
	 
	 public class VMProxy extends Model<ViewModel, ProjectSite>{
		 private List<ObservationGroup> observationGroups;
		 private Site site;
		 private Project project;
		 
		 public List<ObservationGroup> getObservationGroups(){
			 return observationGroups;
		 }
		 
		 public void setObservationGroups(List<ObservationGroup> observationGroups){
			 this.observationGroups = observationGroups;
		 }

		public Site getSite() {
			return site;
		}

		public void setSite(Site site) {
			this.site = site;
		}

		public Project getProject() {
			return project;
		}

		public void setProject(Project project) {
			this.project = project;
		}

		@Override
		public void buildViewModel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void buildModel() {
			// TODO Auto-generated method stub
			
		}
		 
		 
	 }

	public class ViewModel{
		 
		 private String projectName;
		 private String projectNumber;
		 private String siteName;
		 private String[] fieldCrew;
		public String getProjectName() {
			return projectName;
		}
		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}
		public String getProjectNumber() {
			return projectNumber;
		}
		public void setProjectNumber(String projectNumber) {
			this.projectNumber = projectNumber;
		}
		public String getSiteName() {
			return siteName;
		}
		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}
		public String[] getFieldCrew() {
			return fieldCrew;
		}
		public void setFieldCrew(String[] fieldCrew) {
			this.fieldCrew = fieldCrew;
		}
		 
		 
 	 }
}
