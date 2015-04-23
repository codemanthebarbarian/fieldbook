package com.amecfw.sage.util;

import android.content.ServiceConnection;
import android.os.Messenger;

public interface ApplicationGps {

	public Messenger getGpsMessenger();
	
	public ServiceConnection getGpsServiceConnection();
	
}
