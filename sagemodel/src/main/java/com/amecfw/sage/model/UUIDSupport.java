package com.amecfw.sage.model;

import java.util.UUID;

public interface UUIDSupport {
	UUID getUUID();
	void setUUID(UUID rowGuid);
	void generateUUID();
	void setRowGuid();
	String getRowGuid();
}
