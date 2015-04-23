package com.amecfw.sage.util;

/**
 * An interface to identify listeners for fragments/dialogs that use ViewModels.
 * When the fragment is closed, the ViewModel will be passed back to the host with the associated
 * ViewState
 *
 * @param <T> the type of ViewModel to return
 */
public interface OnExitListener<T> {
	/**
	 * The fragment or dialog has exited under the provided ViewState and the ViewModel 
	 * @param viewModel the current ViewModel for the fragment or dialog
	 * @param viewState the current ViewState
	 */
	public void onExit(T viewModel, ViewState viewState);
}
