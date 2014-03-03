package com.cameleon.common.android.db.sqlite.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import org.gdocument.gtracergps.launcher.log.Logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.cameleon.common.android.inotifier.INotifierMessage;
import com.cameleon.common.tool.ToolDatetime;

public abstract class DBAbstractHelper extends SQLiteOpenHelper {

	private static final String PACKAGE_NAME = "com.runningstars";
	protected String databaseName;
	protected int databaseVersion;
	protected INotifierMessage notificationMessage;

	public DBAbstractHelper(Context context, String databaseName, CursorFactory cursorFactory, int databaseVersion, INotifierMessage notificationMessage) {
		super(context, databaseName, cursorFactory, databaseVersion);

		this.databaseName = databaseName;
		this.databaseVersion = databaseVersion;
		this.notificationMessage = notificationMessage;
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			logMe("Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			try {
				backupDbToSdcard(oldVersion);

				logMe("Recreate database databaseName:" + databaseName);
				recreateTable(database);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			logMe("No Upgrading database, identical version");
		}
	}

	/**
	 * Return the table name
	 * @return
	 */
	protected abstract String getTableName();

	/**
	 * Return the class tag name for logs 
	 * @return
	 */
	protected abstract String getTag();

	public void backupDbToSdcard() throws IOException {
		backupDbToSdcard(databaseName);
	}

	public void backupDbToSdcard(int oldVersion) throws IOException {
		String dbNameBackup = databaseName;
		int dot = databaseName.lastIndexOf('.');
		if (dot>0) {
			dbNameBackup = databaseName.substring(0, dot) + "_" + 
					oldVersion + "_" + 
					ToolDatetime.toCompactedDatetime(new Date()) + "_" + 
					databaseName.substring(dot);
		}
		else {
			dbNameBackup = databaseName + "_" + 
					oldVersion + "_" + 
					ToolDatetime.toCompactedDatetime(new Date()) + "_" + 
					".db";
		}

		logMe("Backup database databaseName:" + databaseName + " to " + dbNameBackup);
		
		backupDbToSdcard(dbNameBackup);
	}
	public void backupDbToSdcard(String database) throws IOException {
		File sd = Environment.getExternalStorageDirectory();

        if (sd.canWrite()) {
    		String packageNames = getPackagename();
            String backupDBPath = "/" + packageNames + "/" + database;

            File dir = new File(sd, packageNames);
        	if (!dir.exists() && !dir.mkdirs()) {
        		throw new IOException("Dir '"+packageNames+"' creation error on sd");
        	}

	        File currentDB = new File(this.getWritableDatabase().getPath());
	        File backupDB = new File(sd, backupDBPath);
	
	        copyDb(currentDB, backupDB);
        }
        else {
        	notificationMessage.notifyMessage("sdcard directory is not writable");
        }
	}

	public void restoreDbFromSdcard() throws IOException {
		String packageNames = getPackagename();
		String database = databaseName;
        String backupDBPath = "/" + packageNames + "/" + database;

        File sd = Environment.getExternalStorageDirectory();
        File backupDB = new File(sd, backupDBPath);
        if (sd.exists()) {

        	if (sd.canRead()) {
		        File currentDB = new File(this.getWritableDatabase().getPath());
		        if (currentDB.canWrite()) {
			        copyDb(backupDB, currentDB);
		        }
		        else {
		        	notificationMessage.notifyMessage("current database is not writable");
		        }
            }
            else {
            	notificationMessage.notifyMessage("sdcard database can not be read");
            }
        }
        else {
        	notificationMessage.notifyMessage("sdcard database do not exist");
        }
	}

	public void recreateTable(SQLiteDatabase database) {
		dropTable(database);
		onCreate(database);
	}

	protected void dropTable(SQLiteDatabase database) {
        notificationMessage.notifyMessage("drop database:" + database.getPath());

        database.execSQL("DROP TABLE IF EXISTS " + getTableName());
	}
	
	protected String getPackagename() {
		return PACKAGE_NAME;
	}

	private void copyDb(File currentDB, File backupDB) {
		try {
            FileChannel src = null, dst = null;
            try {
                src = new FileInputStream(currentDB).getChannel();
                dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
            }
            finally {
            	if (src!=null)
            		src.close();
            	if (dst!=null)
            		dst.close();
            }

            notificationMessage.notifyMessage("copyDb From:" + currentDB.toString() + " To:" + backupDB.toString());
	    } catch (Exception ex) {
	    	notificationMessage.notifyError(ex);
	    }
	}

	protected void logMe(String msg) {
		Logger.logMe(getTag(), msg);
    }
}