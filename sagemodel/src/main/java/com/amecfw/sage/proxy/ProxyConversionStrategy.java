package com.amecfw.sage.proxy;

/**
 * An interface to help with converting proxies to viewModels and filling proxies from view models
 *
 * @param <TViewModel>
 * @param <TProxy>
 */
public interface ProxyConversionStrategy<TViewModel extends ViewModel, TProxy extends Model<?,?>> {

	/**
	 * Build a view model from the provided proxy
	 * @param proxy
	 * @return
	 */
	 public TViewModel buildViewModel(TProxy proxy);
	 
	 /**
	  * Fill the proxy from the provided view model
	  * @param proxy
	  * @param viewModel
	  */
	 public void fillProxy(TProxy proxy, TViewModel viewModel);
}
