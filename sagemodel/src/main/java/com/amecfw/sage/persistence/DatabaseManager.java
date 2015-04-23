package com.amecfw.sage.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.amecfw.sage.persistence.DaoMaster;
import com.amecfw.sage.persistence.DaoMaster.OpenHelper;

public class DatabaseManager extends OpenHelper {

	public DatabaseManager(Context context, String name, CursorFactory factory) {		
		super(context, name, factory);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion < 10){//this is the dev version always recreate
			DaoMaster.dropAllTables(db, true);
            onCreate(db);
		}		
	}

}
