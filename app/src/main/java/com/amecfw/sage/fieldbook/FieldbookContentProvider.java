package com.amecfw.sage.fieldbook;

import com.amecfw.sage.model.SageApplication;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class FieldbookContentProvider extends ContentProvider {
	
	private SQLiteDatabase db;
	
	private static final int ELEMENTS = 100;
	private static final int ELEMENT_ROWGUID = 101;
	private static final int ELEMENT_ID = 102;
	
	private static final String AUTHORITY = "com.amecfw.sage.vegapp.provider";
	
	private static final String ROWGUID_COL = "rowGuid";
	private static final String ID_COL = "id";
	private static final String ELEMENTS_BASE = "elements";
	
	private static final UriMatcher providerURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		providerURIMatcher.addURI(AUTHORITY, ELEMENTS_BASE, ELEMENTS);
		providerURIMatcher.addURI(AUTHORITY, ELEMENTS_BASE + "/$", ELEMENT_ROWGUID);
		providerURIMatcher.addURI(AUTHORITY, ELEMENTS_BASE + "/#", ELEMENT_ID);
	}

	@Override
	public boolean onCreate() {
		if(SageApplication.getInstance() == null) SageApplication.initialize(getContext().getApplicationContext());
		db = SageApplication.getInstance().getDatabase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		switch (providerURIMatcher.match(uri)){
		case ELEMENTS:
			queryBuilder.setTables("Element");
			break;
		case ELEMENT_ROWGUID:
			queryBuilder.setTables("Element");
			queryBuilder.appendWhere(String.format("%s = '%s'", ROWGUID_COL, uri.getLastPathSegment()));
			break;
		case ELEMENT_ID:
			queryBuilder.setTables("Element");
			queryBuilder.appendWhere(String.format("%s = %s", ID_COL, uri.getLastPathSegment()));
			break;
		default:
				throw new IllegalArgumentException("Unkown URI: " + uri);
		}
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return new String();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
