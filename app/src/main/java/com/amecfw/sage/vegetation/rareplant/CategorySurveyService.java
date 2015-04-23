package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.model.Location;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.StationElement;
import com.amecfw.sage.model.StationElementMeta;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.model.service.ElementService;
import com.amecfw.sage.model.service.LocationService;
import com.amecfw.sage.model.service.MetaDataService;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.model.service.SurveyService;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.proxy.LocationProxy;
import com.amecfw.sage.proxy.StationElementProxy;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.util.CollectionOperations;
import com.amecfw.sage.vegetation.VegetationGlobals;
import com.amecfw.sage.vegetation.VegetationSurveyProxy;

public class CategorySurveyService {
	
	public static void updateFromProxy(StationProxy proxy, StationEditFragment.ViewModel viewModel, DaoSession session){
		new StationService(session).updateFromProxy(proxy, viewModel);
		viewModel.location = proxy.getGpsLocation();
	}
	
	public static CategorySurveyProxy create(Station station, String[] canopyNames){
		CategorySurveyProxy proxy = new CategorySurveyProxy();
		proxy.setModel(station);
		if(station.getId() == null || station.getId() < 1) proxy.setCanopies(generateCategories(canopyNames));
		else proxy.setCanopies(findCanopies(station, canopyNames));
		return proxy;
	}
	
	private static List<VegetationSurveyProxy> findCanopies(Station station, String[] canopyNames){
		List<VegetationSurveyProxy> canopies = new ArrayList<VegetationSurveyProxy>();
		for(String name: canopyNames){
			VegetationSurveyProxy proxy = findOrBuildCanopy(station, name);
			if(proxy != null) canopies.add(proxy);
		}
		return canopies;
	}
	
	private static VegetationSurveyProxy findOrBuildCanopy(Station parent, String canopyName){
		Station canopyStation = new StationService(SageApplication.getInstance().getDaoSession())
			.getSubstation(parent, VegetationGlobals.STATION_TYPE_VEGETATION_CANOPY, canopyName);
		if(canopyStation == null) return buildCanopy(canopyName);
		else return buildCanopy(canopyStation);
	}
	
	public static void updateFromViewModels(ArrayList<CategoryElementsListAdapter.ViewModel> viewModels, List<StationElementProxy> proxies){
		StationElementProxy.StationElementProxyByRowGuidComparator comparator = new StationElementProxy.StationElementProxyByRowGuidComparator();
		List<StationElementProxy> fromVM = CategorySurveyService.convertToProxies(viewModels);
		List<StationElementProxy> forDelete = CollectionOperations.except(proxies, fromVM, comparator);
		if(forDelete != null && forDelete.size() > 0) CollectionOperations.removeAll(proxies, forDelete, comparator);
		for(StationElementProxy proxy: proxies){ //TODO: investigate moving to ElementService
			StationElementProxy temp = CollectionOperations.find(fromVM, proxy, comparator);
			proxy.getModel().setCount(temp.getModel().getCount());
			MetaDataService.MetaSupportExtensionMethods.replace(proxy.getModel(), temp.getModel());
			//TODO: deal with photos and locations eventually
		}
	}

	/**
	 * Converts an element to a StationElementProxy and adds it to the proxy's station elements if an matching element
	 * does not already exist.
	 * @param proxy the VegetationSurveyProxy (canopy) to add the elements to
	 * @param elements the list of elements to add as proxies
	 * @return the number of elements added
	 */
	public static int addElements(VegetationSurveyProxy proxy, List<Element> elements){
		if(elements == null || elements.size() <1) return 0;
		List<StationElementProxy> proxies = proxy.getStationElements();
		if(proxies == null) {
			proxies = ElementService.convertFromElements(elements);
			return proxies.size();
		}
		return CollectionOperations.Merge(proxy.getStationElements(), ElementService.convertFromElements(elements), new StationElementProxy.StationElementProxyByElementRowGuidComparator());
	}
	
	public static List<VegetationSurveyProxy> generateCategories(String[] canopyNames){
		List<VegetationSurveyProxy> canopies = new ArrayList<VegetationSurveyProxy>();
		for(String name: canopyNames){
			VegetationSurveyProxy proxy = buildCanopy(name);
			if(proxy != null) canopies.add(proxy);
		}
		return canopies;
	}
	
	public static List<StationElementProxy> convertToProxies(List<CategoryElementsListAdapter.ViewModel> viewModels){
		if(viewModels == null || viewModels.size() == 0) return new ArrayList<StationElementProxy>();
		List<StationElementProxy> results = new ArrayList<StationElementProxy>(viewModels.size());
		for(CategoryElementsListAdapter.ViewModel vm : viewModels) results.add(convertToProxy(vm));
		return results;
	}
	
	public static List<StationElementProxy> convertStationElementsToProxies(List<StationElement> stationElements){
		List<StationElementProxy> proxies = new ArrayList<StationElementProxy>();
		if(stationElements != null){
			for(StationElement se : stationElements){
				StationElementProxy proxy = new StationElementProxy();
				proxy.setModel(se);
				proxies.add(proxy);
			}
		}
		return proxies;
	}
	
	public static StationElementProxy convertToProxy(CategoryElementsListAdapter.ViewModel viewModel){
		StationElementProxy proxy = new StationElementProxy();
		StationElement se = new StationElement();
		Element element = new Element();
		DescriptorServices.setByFieldDescriptor(viewModel, element);
		DescriptorServices.setByFieldDescriptor(viewModel, se);
		List<StationElementMeta> metaElements = MetaDataService.fromAnnotations(viewModel, true);
		if(metaElements != null && metaElements.size() > 0) se.setMetaData(metaElements);
		se.setElement(element);
		proxy.setModel(se);
		if(viewModel.getLocation() != null) {
			Location l = new Location();
			if(proxy.getModel().getRowGuid() == null) proxy.getModel().setRowGuid();
			l.setName(proxy.getModel().getRowGuid());
			LocationProxy lp = LocationService.createPointFromLocation(viewModel.getLocation(), l);
			proxy.setLocation(lp);
		}
		return proxy;
	}
	
	/**
	 * Builds a ViewModel from the provided proxy
	 * note: uses the first location in the proxy's locations as the location
	 * @param proxy
	 * @param elementsListAdapterViewMode
	 * @return the generated ViewModel or null if the proxy, or the proxy's model's element is null
	 */
	public static CategoryElementsListAdapter.ViewModel convertToViewModel(StationElementProxy proxy){
		if(proxy == null || proxy.getModel() == null) return null;
		CategoryElementsListAdapter.ViewModel vm = convertToViewModel(proxy.getModel().getElement());
		if(vm == null) return null;
		DescriptorServices.getByFieldDescriptor(vm, proxy.getModel());
		if(proxy.getModel().hasMetaData()) MetaDataService.updateAnnotations(vm, proxy.getModel().getMetaData());
		if(proxy.getLocation() != null && proxy.getLocation().getLocations().size() > 0) vm.setLocation(proxy.getLocation().getLocations().get(0));
		return vm;
	}
	
	public static boolean saveOrUpdate(CategorySurveyProxy proxy){
		boolean result = false;
		DaoSession session = SageApplication.getInstance().getDaoSession();
		SurveyService surveyService = new SurveyService(session);
		ElementService elementService = new ElementService(session);
		SageApplication.getInstance().getDatabase().beginTransaction();
		try{ //main survey must already be saved (this will be the case)
			for(VegetationSurveyProxy survey : proxy.getCanopies()){
				if(survey.getStationElements() != null && survey.getStationElements().size() > 0){
					//Save the survey
					survey.setRoot(proxy);
					surveyService.saveOrUpdate(survey);
					//save the elements
					elementService.saveOrUpdate(survey.getStationElements(), survey.getModel());
				}else{
					new ElementService(session).delete(survey.getModel());
				}
			}
			SageApplication.getInstance().getDatabase().setTransactionSuccessful();
			result = true;
		}catch(Exception e){
			result = false;
		}finally{
			SageApplication.getInstance().getDatabase().endTransaction();
		}
		return result;
	}
	
	/**
	 * 
	 * @param element
	 * @param elementsListAdapterViewMode used to determine which name to display (default is common name)
	 * @return
	 */
	public static CategoryElementsListAdapter.ViewModel convertToViewModel(Element element){
		if (element == null) return null;
		CategoryElementsListAdapter.ViewModel vm = new CategoryElementsListAdapter.ViewModel();
		DescriptorServices.getByFieldDescriptor(vm, element);
		return vm;
	}
	
	private static VegetationSurveyProxy buildCanopy(Station station){
		VegetationSurveyProxy result = new VegetationSurveyProxy();
		result.setModel(station);
		ElementService es = new ElementService(SageApplication.getInstance().getDaoSession());
		List<StationElement> stationElements = es.findStationElements(station);
		if(stationElements != null)	result.setStationElements(ElementService.convertFromStationElements(stationElements));
		return result;
	}
	
	private static VegetationSurveyProxy buildCanopy(String canopy){
		VegetationSurveyProxy result = new VegetationSurveyProxy();
		result.setModel(new Station());
		result.getModel().setStationType(VegetationGlobals.STATION_TYPE_VEGETATION_CANOPY);
		result.getModel().setName(canopy);
		result.getModel().setRowGuid();
		result.setStationElements(new ArrayList<StationElementProxy>());
		return result;
	}
	
}
