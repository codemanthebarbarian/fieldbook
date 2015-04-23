package com.amecfw.sage.persistence;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import com.amecfw.sage.model.StationElement;

import com.amecfw.sage.model.StationElementMeta;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table STATION_ELEMENT_META.
*/
public class StationElementMetaDao extends AbstractDao<StationElementMeta, Long> {

    public static final String TABLENAME = "STATION_ELEMENT_META";

    /**
     * Properties of entity StationElementMeta.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property RowGuid = new Property(1, String.class, "rowGuid", false, "ROW_GUID");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property Value = new Property(3, String.class, "value", false, "VALUE");
        public final static Property ParentID = new Property(4, long.class, "parentID", false, "PARENT_ID");
    };

    private DaoSession daoSession;

    private Query<StationElementMeta> stationElement_MetaDataQuery;

    public StationElementMetaDao(DaoConfig config) {
        super(config);
    }
    
    public StationElementMetaDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'STATION_ELEMENT_META' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'ROW_GUID' TEXT NOT NULL UNIQUE ," + // 1: rowGuid
                "'NAME' TEXT," + // 2: name
                "'VALUE' TEXT," + // 3: value
                "'PARENT_ID' INTEGER NOT NULL );"); // 4: parentID
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'STATION_ELEMENT_META'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, StationElementMeta entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getRowGuid());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String value = entity.getValue();
        if (value != null) {
            stmt.bindString(4, value);
        }
        stmt.bindLong(5, entity.getParentID());
    }

    @Override
    protected void attachEntity(StationElementMeta entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public StationElementMeta readEntity(Cursor cursor, int offset) {
        StationElementMeta entity = new StationElementMeta( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // rowGuid
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // value
            cursor.getLong(offset + 4) // parentID
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, StationElementMeta entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setRowGuid(cursor.getString(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setValue(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setParentID(cursor.getLong(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(StationElementMeta entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(StationElementMeta entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "metaData" to-many relationship of StationElement. */
    public List<StationElementMeta> _queryStationElement_MetaData(long parentID) {
        synchronized (this) {
            if (stationElement_MetaDataQuery == null) {
                QueryBuilder<StationElementMeta> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ParentID.eq(null));
                stationElement_MetaDataQuery = queryBuilder.build();
            }
        }
        Query<StationElementMeta> query = stationElement_MetaDataQuery.forCurrentThread();
        query.setParameter(0, parentID);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getStationElementDao().getAllColumns());
            builder.append(" FROM STATION_ELEMENT_META T");
            builder.append(" LEFT JOIN STATION_ELEMENT T0 ON T.'PARENT_ID'=T0.'_id'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected StationElementMeta loadCurrentDeep(Cursor cursor, boolean lock) {
        StationElementMeta entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        StationElement stationElement = loadCurrentOther(daoSession.getStationElementDao(), cursor, offset);
         if(stationElement != null) {
            entity.setStationElement(stationElement);
        }

        return entity;    
    }

    public StationElementMeta loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<StationElementMeta> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<StationElementMeta> list = new ArrayList<StationElementMeta>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<StationElementMeta> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<StationElementMeta> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
