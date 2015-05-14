package com.amecfw.sage.vegetation.rareplant;

import java.util.ArrayList;
import java.util.List;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.proxy.PhotoProxy;
import com.amecfw.sage.proxy.StationElementProxy;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.util.CollectionOperations;
import com.amecfw.sage.vegetation.VegetationSurveyProxy;

public class CategorySurveyProxy extends StationProxy {

	 private List<VegetationSurveyProxy> canopies;
	 private int initialSize;
	 
	 private ArrayList<ArrayList<CategoryElementsListAdapter.ViewModel>> canopyElementViewModels;

	public List<VegetationSurveyProxy> getCanopies() {
		return canopies;
	}

	public void setCanopies(List<VegetationSurveyProxy> canopies) {
		this.canopies = canopies;
		initialSize = canopies.size();
		initialize();
	}

	public ArrayList<CategoryElementsListAdapter.ViewModel> getCanopyElementViewModels(String canopy) {
		int index = CollectionOperations.indexOf(canopies, canopy, new VegetationSurveyProxy.StationNameCIStringComparator());
		if(index < 0) return null;
		if (isInvalidated()) initialize();
		if(isOutOfSync(index)) syncFromProxy(index);
		return canopyElementViewModels.get(index);
	}

	/**
	 * Sets the elements for the provided canopy
	 * @param canopyElementViewModels
	 * @param canopy
	 * @exception IllegalArgumentException if the canopy provided is not in the proxy
	 */
	public void updateCanopyElementViewModels(ArrayList<CategoryElementsListAdapter.ViewModel> canopyElementViewModels, String canopy) {
		int index = getIndex(canopy);
		if(index < 0) throw new IllegalArgumentException();
		this.canopyElementViewModels.set(index, canopyElementViewModels);
		syncFromViewModel(index);
	}
	
	/**
	 * adds the elements to the category, if an element already exists, it is not added
	 * @param elements
	 * @param canopy
	 * @return the number of elements added
	 */
	public int addElements(List<Element> elements, String canopy){
		if(elements == null || elements.size() < 0) return 0;
		if(isInvalidated()) initialize();
		int index = getIndex(canopy);
		return  CategorySurveyService.addElements(canopies.get(index), elements);
	}
	
	/**
	 * Sync the view models from the proxy at the provided index
	 * @param index
	 */
	private void syncFromProxy(int index){
		int size = (canopies.get(index) == null|| canopies.get(index).getStationElements() == null) ? 0 : canopies.get(index).getStationElements().size(); 
		ArrayList<CategoryElementsListAdapter.ViewModel> viewModels = canopyElementViewModels.get(index);
		if(viewModels == null){ 
			viewModels = new ArrayList<CategoryElementsListAdapter.ViewModel>(size == 0 ? 10 : size);
			canopyElementViewModels.set(index, viewModels);
		}
		else viewModels.clear();
		if(size == 0) return;
		for(StationElementProxy proxy: canopies.get(index).getStationElements()){
			viewModels.add(CategorySurveyService.convertToViewModel(proxy));
		}		
	}
	
	/**
	 * Sync the proxy from the view models at the provided index
	 * @param index
	 */
	private void syncFromViewModel(int index){
		CategorySurveyService.updateFromViewModels(canopyElementViewModels.get(index), canopies.get(index).getStationElements());
	}
	
	private int getIndex(String canopy) { return CollectionOperations.indexOf(canopies, canopy, new VegetationSurveyProxy.StationNameCIStringComparator()); }
	
	private boolean isInvalidated() { return ! (canopies == null || initialSize == canopies.size()); }
	
	private boolean isOutOfSync(int index) {
		if(canopies.get(index).getStationElements() == null || canopyElementViewModels.get(index) == null) return true;
		return ! (canopies.get(index).getStationElements().size() == canopyElementViewModels.get(index).size());
	}
	
	private void initialize() {
		canopyElementViewModels = new ArrayList<ArrayList<CategoryElementsListAdapter.ViewModel>>(canopies.size());
		for(int i = 0 ; i < canopies.size() ; i++) canopyElementViewModels.add(null);
	}

	public void addPhotoProxy(PhotoProxy proxy, CategoryElementsListAdapter.ViewModel viewModel, String canopy){
		VegetationSurveyProxy surveyProxy = canopies.get(getIndex(canopy));
		CategorySurveyService.addPhotoProxy(surveyProxy, proxy, viewModel);
	}
	
}
