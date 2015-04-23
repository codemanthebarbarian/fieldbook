package com.amecfw.sage.proxy;

import java.io.File;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.model.EntityBase;
import com.amecfw.sage.model.Photo;

public class PhotoProxy extends Proxy<Photo> {

	private Coordinate coordinate;
	private EntityBase parent;
	private boolean isTemporary;
	private File file;
	
	public Coordinate getCoordinate() {
		return coordinate;
	}
	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
	public EntityBase getParent() {
		return parent;
	}
	public void setParent(EntityBase parent) {
		this.parent = parent;
	}
	public boolean isTemporary() {
		return isTemporary;
	}
	public void setTemporary(boolean isTemporary) {
		this.isTemporary = isTemporary;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	
}
