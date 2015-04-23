package com.amecfw.sage.proxy;

/**
 * A class to represent proxied objects in the fieldbook. Primarily ViewModels are proxied
 * to sage domain model classes. A proxy could be bound to one or more model classes (up to the
 * implementer to determine the models required to address the viewmodel represented.
 * @param <Tviewmodel> the view model for the proxy
 * @param <Tmodel> the principle model the proxy represents
 */
public abstract class Model<Tviewmodel, Tmodel> extends Proxy<Tmodel> {
	
	protected Tviewmodel viewModel;
	
	public Tviewmodel getViewModel() {
		return viewModel;
	}
	public void setViewModel(Tviewmodel viewModel) {
		this.viewModel = viewModel;
	}
	
	/**
	 * builds the viewmodel based on the Proxy's model(s)
	 */
	public abstract void buildViewModel();
	
	/**
	 * builds the model(s) from the proxy's viewmodel
	 */
	public abstract void buildModel();
}
