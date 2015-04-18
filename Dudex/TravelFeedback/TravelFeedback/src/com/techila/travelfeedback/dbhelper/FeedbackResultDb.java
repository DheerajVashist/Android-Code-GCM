package com.techila.travelfeedback.dbhelper;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.techila.travelfeedback.pojo.FeedbackResponsePojo;
import com.techila.travelfeedback.pojo.FeedbackTblContent;

public class FeedbackResultDb extends SQLiteOpenHelper implements FeedbackTblContent {

	public final static String TABLE_NAME = "feedbackresult";
	public final static String DB_NAME = "TravelDb";
	static int version = 1;

	SQLiteDatabase sdb;

	public FeedbackResultDb(Context context) {
		super(context, DB_NAME, null, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String statement = "CREATE TABLE " + TABLE_NAME + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY," + VEHICLE_NUMBER + " TEXT,"
				+ VEHICLE_RATING + " TEXT," + VEHICLE_TYPE + " TEXT," + USER_ID
				+ " TEXT," + CMT_DATE + " TEXT," + USER_CMT + " TEXT)";

		db.execSQL(statement);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public boolean insertFeedback(String veh_number, String veh_type,
			ArrayList<FeedbackResponsePojo> feedback) {
		try{
		
		Log.d("Vehicle Number"," "+veh_number);
		removePreviousData(veh_number,veh_type);
		
		sdb = this.getWritableDatabase();
		for (int i = 0; i < feedback.size(); i++) {
			ContentValues cv = new ContentValues();
			cv.put(USER_ID, feedback.get(i).getUserName());
			cv.put(VEHICLE_NUMBER, veh_number.toUpperCase());
			cv.put(VEHICLE_RATING, feedback.get(i).getRating());
			cv.put(VEHICLE_TYPE, veh_type);
			cv.put(USER_CMT, feedback.get(i).getComment());
			cv.put(CMT_DATE, feedback.get(i).getDate());

			long result = sdb.insert(TABLE_NAME, null, cv);
			Log.d("Row ID "," Num =  "+result);
			if (result < 0) {
				return false;
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		sdb.close();
		return true;
	}

	private void removePreviousData(String vh_number,String vh_type) {
		// TODO Auto-generated method stub
		sdb= this.getWritableDatabase();
		
		sdb.delete(TABLE_NAME, VEHICLE_NUMBER+" ='"+vh_number+"' AND "+VEHICLE_TYPE
				    +"='"+vh_type+"'", null);
		sdb.close();
	}

	public ArrayList<FeedbackResponsePojo> getFeedback(String vehicle_num,String vehicle_type){
		
		ArrayList<FeedbackResponsePojo> arr = new ArrayList<FeedbackResponsePojo>();
			sdb = this.getReadableDatabase();
		Cursor cr = sdb.rawQuery("SELECT * FROM "
					+TABLE_NAME+" WHERE "+VEHICLE_NUMBER+"='"
				    +vehicle_num.toUpperCase()+"' AND "+VEHICLE_TYPE+" = '"+vehicle_type+"'", null);
		
		Log.d("Vehicle Number "," = "+vehicle_num);
		Log.d("Vehicle Type "," = "+vehicle_type);
		
			if(!cr.isAfterLast()){
				cr.moveToFirst();
				do{
					FeedbackResponsePojo pojo = new FeedbackResponsePojo();
					pojo.setUserName(cr.getString(4));
					pojo.setDate(cr.getString(5));
					pojo.setRating(cr.getString(2));
					pojo.setComment(cr.getString(6));
					pojo.setVeh_number(cr.getString(1));
					arr.add(pojo);
				}while(cr.moveToNext());				
			}
			sdb.close();
		return arr;
	}
}
