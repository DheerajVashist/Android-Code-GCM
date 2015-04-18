package com.techila.travelfeedback.dbhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.techila.travelfeedback.vehicle_interface.VehicleNumberData;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VehicleNumberDb extends SQLiteOpenHelper implements
		VehicleNumberData {

	private String DB_PATH;
	private String TABLE_NAME = "vehicle_tbl";
	static String DBName = "vehicle_db.sqlite";
	public SQLiteDatabase myDatabase = null;
	private Context myContext = null;
	public final static int version = 1;

	public static VehicleNumberDb myDbHelper;

	private VehicleNumberDb(Context context) {
		super(context, DBName, null, version);
		// TODO Auto-generated constructor stub
		DB_PATH = "data" + File.separator + "data" + File.separator
				+ context.getPackageName() + File.separator + "databases"
				+ File.separator;

		this.myContext = context;
		Log.d("Debug", "Point 2");
		try {
			if (!isDatabaseAvailable()) {
				this.getReadableDatabase();
				try {
					Log.d("Debug", " Point 5");
					copyTheDatabase();
				} catch (IOException e) {
					Log.d("Debug", " Point 6");
					throw new Error("Error Coping database");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static VehicleNumberDb getInstance(Context context) {
		if (myDbHelper == null) {
			myDbHelper = new VehicleNumberDb(context);
		}
		return myDbHelper;
	}

	private void copyTheDatabase() throws IOException {
		// TODO Auto-generated method stub
		InputStream iStream = myContext.getAssets().open(DBName);

		String file_location = DB_PATH + DBName;

		OutputStream oStream = new FileOutputStream(file_location);

		byte[] buffer = new byte[1024];

		while (iStream.read(buffer) > 0) {
			oStream.write(buffer, 0, buffer.length);
		}

		oStream.flush();
		oStream.close();
		iStream.close();

	}

	private boolean isDatabaseAvailable() {
		// TODO Auto-generated method stub
		try {
			String myPath = DB_PATH + DBName;
			myDatabase = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
			if (myDatabase != null) {
				myDatabase.close();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public ArrayList<String> getTrainNumber() {
		ArrayList<String> arr = new ArrayList<String>();

		if (isDatabaseAvailable()) {
			myDatabase.close();
			myDatabase = this.getWritableDatabase();
			Log.d("===============","===================");
			String stmt = "SELECT train_number FROM " + TABLE_NAME + " where "
					+ TRAIN_NUMBER + " !=" + "\"\"";
			
			try{
			Cursor cr = myDatabase.rawQuery(stmt, null);

			if (!cr.isAfterLast()) {
				cr.moveToFirst();
				do {
					arr.add(cr.getString(0));

				} while (cr.moveToNext());

			}
			
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				Log.d("===============","=========Exception==========");
			}
		}
		return arr;
	}

}
