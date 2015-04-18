package com.techila.travelfeedback.adapter;

import java.util.ArrayList;

import com.techila.travelfeedback.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomAdapterForSpinner extends ArrayAdapter<String>{
	
	LayoutInflater inflater;
	ArrayList<String> arr;
	
	
	public CustomAdapterForSpinner(Context context, int resourse, ArrayList<String> arr) {
		// TODO Auto-generated constructor stub
		super(context,resourse, arr);
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.arr = arr;	
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return mySpnView(position, convertView, parent);
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return mySpnView(position, convertView, parent);
	}
	
	
	private View mySpnView(int position, View convertView, ViewGroup parent){
		
		View vi = convertView;
		if(vi==null){
		 vi = inflater.inflate(R.layout.spn_item_layout, null);	
		}
		TextView tv= (TextView) vi.findViewById(R.id.tv_subvehicletype);
		
		tv.setText(arr.get(position));
		
		return vi;
	}
	
	
	
}
