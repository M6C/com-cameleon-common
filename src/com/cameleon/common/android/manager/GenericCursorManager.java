package com.cameleon.common.android.manager;

import java.util.List;

import org.gdocument.gtracergps.launcher.log.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import com.cameleon.common.android.mapper.GenericMapper;
import com.cameleon.common.android.model.GenericDBPojo;

public abstract class GenericCursorManager<T extends GenericDBPojo<Long>, M extends GenericMapper<T>> {

	private final static String TAG = GenericCursorManager.class.getSimpleName();

	protected List<T> getList(Context context, String where, String[] whereParameters) {
		// Run query
		CursorLoader cursorLoader = buildCursorLoader(context, where, whereParameters);
		
		List<T> ret = null;
		Cursor cursor = null;
		try {
			cursor = cursorLoader.loadInBackground();
			
			if (cursor!=null) {
				logCursor(cursor);
				ret = getMapper().mappe(cursor);
			}
		} finally {
			if (cursor!=null) {
				cursor.close();
			}
		}
		logManager("getList", ret);
		return ret;
	}

	protected abstract CursorLoader buildCursorLoader(Context context, String where, String[] whereParameters);
	protected abstract M getMapper();

	protected boolean showLogTrace() {
		return false;
	}

	protected boolean showLogCursor() {
		return true;
	}

	protected String tagLogTrace() {
		return TAG;
	}
	
	protected void logManager(String title, List<T> ret) {
		if (ret!=null && showLogTrace()) {
			for (int i = 0; i < ret.size(); i++) {
				T pojo = ret.get(i);
				logMe(title + "[" + i + "]:" + pojo.getId());
			}
		}
	}
	

	protected void logCursor(Cursor cursor) {
		if (showLogCursor()) {
			int ROW_LIMIT = 20;
			int STR_LIMIT = 20;
			int columnsQty = cursor.getColumnCount();
			StringBuilder sb = new StringBuilder();
			for(int i=0 ; i<columnsQty ; i++) {
				sb.append(cursor.getColumnName(i));
				if (i < columnsQty - 1)
					sb.append("; ");
			}
			logMe(String.format("Column Names: %s", sb.toString()));
			int rowCnt = ROW_LIMIT;
			if (cursor.moveToFirst()) {
				do {
					sb = new StringBuilder();
					for (int idx = 0; idx < columnsQty; ++idx) {
						String str = cursor.getString(idx);
						if (str != null && str.length() > STR_LIMIT) {
							str = str.substring(0, STR_LIMIT);
						}
						sb.append(cursor.getColumnName(idx)).append(":").append(str);
						if (idx < columnsQty - 1)
							sb.append("; ");
					}
					logMe(String.format("Row: %d, Values: %s", cursor.getPosition(), sb.toString()));
				} while ((rowCnt-- > 0) && cursor.moveToNext());
			}
		}
	}

	protected void logMe(String message) {
		Logger.logMe(tagLogTrace(), message);
	}

	protected void logMe(Exception ex) {
		Logger.logMe(tagLogTrace(), ex);
    }
}