package com.garlicg.screenrecordct.data;


import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

/**
 * ContentResolverにアクセスするクラス
 */
public class ContentAccessor {

    //////////////
    // Params


    private ContentResolver mContentResolver;

    public ContentAccessor(Fragment f) {
        this(f.getActivity());
    }

    public ContentAccessor(Context context) {
        mContentResolver = context.getContentResolver();
    }

    private ContentValues mContentValues;
    private Uri mContentUri;
    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mOrder;
    ArrayList<ContentProviderOperation> mOperations;


    //////////////
    // executes

    /**
     * Query
     */
    public void startQuery(Uri contentUri, AsyncExecutor.Listener<Cursor> listener) {
        mContentUri = contentUri;
        new AsyncExecutor<Object, Integer, Cursor>(listener) {
            @Override
            protected Cursor doInBackground(Object[] values) {
                return mContentResolver.query(mContentUri, mProjection, mSelection, mSelectionArgs, mOrder);
            }
        }.execute();
    }

    /**
     * Query
     */
    public void setQuery(String[] projection, String selection, String[] selectionArgs, String order) {
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mOrder = order;
    }


    /**
     * Query Item
     */
    public void startQueryItem(AsyncExecutor.Listener<Cursor> listener) {
        new AsyncExecutor<Object, Integer, Cursor>(listener) {
            @Override
            protected Cursor doInBackground(Object[] values) {
                return mContentResolver.query(mContentUri, mProjection, null, null, null);
            }
        }.execute();
    }

    public void setQueryItem(Uri contentUri, String[] projection) {
        mContentUri = contentUri;
        mProjection = projection;
    }


    /**
     * Insert
     */
    public void startInsert(AsyncExecutor.Listener<Uri> listener) {
        new AsyncExecutor<Object, Integer, Uri>(listener) {
            @Override
            protected Uri doInBackground(Object[] values) {
                return mContentResolver.insert(mContentUri, mContentValues);
            }
        }.execute();
    }

    public void setInsertItem(Uri contentUri, ContentValues contentValues) {
        mContentUri = contentUri;
        mContentValues = contentValues;
    }

    /**
     * Update
     */
    public void startUpdateItem(AsyncExecutor.Listener<Integer> listener) {
        new AsyncExecutor<Object, Integer, Integer>(listener) {
            @Override
            protected Integer doInBackground(Object... params) {
                return mContentResolver.update(mContentUri, mContentValues, null, null);
            }
        }.execute();
    }

    public void setUpdateItem(Uri itemUri, ContentValues value) {
        mContentUri = itemUri;
        mContentValues = value;
    }

    /**
     * Updates
     */
    public void startOperation(final String authority, AsyncExecutor.Listener<ContentProviderResult[]> listener) {
        new AsyncExecutor<Object, Integer, ContentProviderResult[]>(listener) {
            @Override
            protected ContentProviderResult[] doInBackground(Object... params) {
                try {
                    return mContentResolver.applyBatch(authority, mOperations);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public void setOperation(ArrayList<ContentProviderOperation> ops) {
        mOperations = ops;
    }


    /**
     * Delete
     */
    public void startDeleteItem(AsyncExecutor.Listener<Integer> listener) {
        new AsyncExecutor<Object, Integer, Integer>(listener) {
            @Override
            protected Integer doInBackground(Object[] values) {
                return mContentResolver.delete(mContentUri, null, null);
            }
        }.execute();
    }

    public void setDeleteItem(Uri contentUri) {
        mContentUri = contentUri;
    }


}