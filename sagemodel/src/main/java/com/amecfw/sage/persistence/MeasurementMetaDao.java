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

import com.amecfw.sage.model.Measurement;

import com.amecfw.sage.model.MeasurementMeta;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table MEASUREMENT_META.
*/
public class MeasurementMetaDao extends AbstractDao<MeasurementMeta, Long> {

    public static final String TABLENAME = "MEASUREMENT_META";

    /**
     * Properties of entity MeasurementMeta.<br/>
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

    private Query<MeasurementMeta> measurement_MetaDataQuery;

    public MeasurementMetaDao(DaoConfig config) {
        super(config);
    }
    
    public MeasurementMetaDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'MEASUREMENT_META' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'ROW_GUID' TEXT NOT NULL UNIQUE ," + // 1: rowGuid
                "'NAME' TEXT," + // 2: name
                "'VALUE' TEXT," + // 3: value
                "'PARENT_ID' INTEGER NOT NULL );"); // 4: parentID
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'MEASUREMENT_META'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, MeasurementMeta entity) {
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
    protected void attachEntity(MeasurementMeta entity) {
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
    public MeasurementMeta readEntity(Cursor cursor, int offset) {
        MeasurementMeta entity = new MeasurementMeta( //
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
    public void readEntity(Cursor cursor, MeasurementMeta entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setRowGuid(cursor.getString(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setValue(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setParentID(cursor.getLong(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(MeasurementMeta entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(MeasurementMeta entity) {
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
    
    /** Internal query to resolve the "metaData" to-many relationship of Measurement. */
    public List<MeasurementMeta> _queryMeasurement_MetaData(long parentID) {
        synchronized (this) {
            if (measurement_MetaDataQuery == null) {
                QueryBuilder<MeasurementMeta> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ParentID.eq(null));
                measurement_MetaDataQuery = queryBuilder.build();
            }
        }
        Query<MeasurementMeta> query = measurement_MetaDataQuery.forCurrentThread();
        query.setParameter(0, parentID);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getMeasurementDao().getAllColumns());
            builder.append(" FROM MEASUREMENT_META T");
            builder.append(" LEFT JOIN MEASUREMENT T0 ON T.'PARENT_ID'=T0.'_id'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected MeasurementMeta loadCurrentDeep(Cursor cursor, boolean lock) {
        MeasurementMeta entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Measurement measurement = loadCurrentOther(daoSession.getMeasurementDao(), cursor, offset);
         if(measurement != null) {
            entity.setMeasurement(measurement);
        }

        return entity;    
    }

    public MeasurementMeta loadDeep(Long key) {
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
    public List<MeasurementMeta> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<MeasurementMeta> list = new ArrayList<MeasurementMeta>(count);
        
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
    
    protected List<MeasurementMeta> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<MeasurementMeta> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
