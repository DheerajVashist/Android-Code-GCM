package com.techila.travelfeedback;

import java.util.ArrayList;
import java.util.List;

import com.techila.travelfeedback.pojo.FeedbackResponsePojo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

public class CustomAdapterForFindFeedback extends ArrayAdapter<FeedbackResponsePojo> {

	LayoutInflater inflater;
	ArrayList<FeedbackResponsePojo> arr;
	
	public CustomAdapterForFindFeedback(Context context, int resource,
			ArrayList<FeedbackResponsePojo> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.arr = objects;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	private class ViewHolder{
		
		TextView tv_userName, tv_date, tv_comments, tv_veh_num;
		RatingBar rt_bar;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi =convertView;
		ViewHolder holder=null;
		if(vi==null){
			vi = inflater.inflate(R.layout.lst_item_for_feedback, null);
			holder = new ViewHolder();
			holder.tv_comments = (TextView)vi.findViewById(R.id.tv_comment);
			holder.tv_userName = (TextView)vi.findViewById(R.id.tv_user_name);
			holder.tv_date = (TextView)vi.findViewById(R.id.tv_date);
			holder.rt_bar = (RatingBar)vi.findViewById(R.id.rating_bar);
			holder.tv_veh_num = (TextView)vi.findViewById(R.id.tv_veh_num);
			vi.setTag(holder);
		}else{
			holder = (ViewHolder)vi.getTag();
		}
			holder.tv_comments.setText(arr.get(position).getComment());
			holder.tv_userName.setText("    "+arr.get(position).getUserName());
			holder.tv_date.setText(arr.get(position).getDate());
			holder.tv_veh_num.setText("    "+arr.get(position).getVeh_number());
			try{
				holder.rt_bar.setRating(Float.parseFloat(arr.get(position).getRating()));
				holder.rt_bar.setEnabled(false);
			}catch(Exception e){
				holder.rt_bar.setRating(0.0f);
			}
		return vi;
	}

}
