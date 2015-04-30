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

import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.model.ObservationType;

import com.amecfw.sage.model.GroupObservation;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table GROUP_OBSERVATION.
*/
public class GroupObservationDao extends AbstractDao<GroupObservation, Long> {

    public static final String TABLENAME = "GROUP_OBSERVATION";

    /**
     * Properties of entity GroupObservation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property RowGuid = new Property(1, String.class, "rowGuid", false, "ROW_GUID");
        public final static Property AllowableValues = new Property(2, String.class, "allowableValues", false, "ALLOWABLE_VALUES");
        public final static Property ObservationTypeID = new Property(3, long.class, "observationTypeID", false, "OBSERVATION_TYPE_ID");
        public final static Property ObservationGroupID = new Property(4, long.class, "observationGroupID", false, "OBSERVATION_GROUP_ID");
    };

    private DaoSession daoSession;

    private Query<GroupObservation> observationGroup_GroupObservationsQuery;

    public GroupObservationDao(DaoConfig config) {
        super(config);
    }
    
    public GroupObservationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'GROUP_OBSERVATION' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'ROW_GUID' TEXT NOT NULL UNIQUE ," + // 1: rowGuid
                "'ALLOWABLE_VALUES' TEXT," + // 2: allowableValues
                "'OBSERVATION_TYPE_ID' INTEGER NOT NULL ," + // 3: observationTypeID
                "'OBSERVATION_GROUP_ID' INTEGER NOT NULL );"); // 4: observationGroupID
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'GROUP_OBSERVATION'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, GroupObservation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getRowGuid());
 
        String allowableValues = entity.getAllowableValues();
        if (allowableValues != null) {
            stmt.bindString(3, allowableValues);
        }
        stmt.bindLong(4, entity.getObservationTypeID());
        stmt.bindLong(5, entity.getObservationGroupID());
    }

    @Override
    protected void attachEntity(GroupObservation entity) {
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
    public GroupObservation readEntity(Cursor cursor, int offset) {
        GroupObservation entity = new GroupObservation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // rowGuid
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // allowableValues
            cursor.getLong(offset + 3), // observationTypeID
            cursor.getLong(offset + 4) // observationGroupID
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, GroupObservation entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setRowGuid(cursor.getString(offset + 1));
        entity.setAllowableValues(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setObservationTypeID(cursor.getLong(offset + 3));
        entity.setObservationGroupID(cursor.getLong(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(GroupObservation entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(GroupObservation entity) {
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
    
    /** Internal query to resolve the "groupObservations" to-many relationship of ObservationGroup. */
    public List<GroupObservation> _queryObservationGroup_GroupObservations(long observationGroupID) {
        synchronized (this) {
            if (observationGroup_GroupObservationsQuery == null) {
                QueryBuilder<GroupObservation> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ObservationGroupID.eq(null));
                observationGroup_GroupObservationsQuery = queryBuilder.build();
            }
        }
        Query<GroupObservation> query = observationGroup_GroupObservationsQuery.forCurrentThread();
        query.setParameter(0, observationGroupID);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getObservationTypeDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T1", daoSession.getObservationGroupDao().getAllColumns());
            builder.append(" FROM GROUP_OBSERVATION T");
            builder.append(" LEFT JOIN OBSERVATION_TYPE T0 ON T.'OBSERVATION_TYPE_ID'=T0.'_id'");
            builder.append(" LEFT JOIN OBSERVATION_GROUP T1 ON T.'OBSERVATION_GROUP_ID'=T1.'_id'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected GroupObservation loadCurrentDeep(Cursor cursor, boolean lock) {
        GroupObservation entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        ObservationType observationType = loadCurrentOther(daoSession.getObservationTypeDao(), cursor, offset);
         if(observationType != null) {
            entity.setObservationType(observationType);
        }
        offset += daoSession.getObservationTypeDao().getAllColumns().length;

        ObservationGroup observationGroup = loadCurrentOther(daoSession.getObservationGroupDao(), cursor, offset);
         if(observationGroup != null) {
            entity.setObservationGroup(observationGroup);
        }

        return entity;    
    }

    public GroupObservation loadDeep(Long key) {
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
    public List<GroupObservation> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<GroupObservation> list = new ArrayList<GroupObservation>(count);
        
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
    
    protected List<GroupObservation> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<GroupObservation> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}