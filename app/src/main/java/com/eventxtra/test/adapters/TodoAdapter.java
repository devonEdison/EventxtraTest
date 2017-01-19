package com.eventxtra.test.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eventxtra.test.R;

/**
 * Created by Devon on 2016/1/18
 */
public class TodoAdapter extends ArrayAdapter<TodoAdapter.SampleItem> {
	public TodoAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		View v = convertView;
		final Holder holder;
		if(v == null){
			v = LayoutInflater.from(getContext()).inflate(R.layout.todo_list_row, null);
			holder = new Holder();
			holder.text_datetimevalue = (TextView) v.findViewById(R.id.edit_text_datetimevalue);
			holder.text_taskvalue = (TextView)v.findViewById(R.id.edit_text_taskvalue);
			holder.text_isfinishvalue = (TextView)v.findViewById(R.id.edit_text_isfinishvalue);
			holder.relativelayout_listrow = (RelativeLayout)v.findViewById(R.id.relativelayout_listrow);
			v.setTag(holder);
		}else{
			holder = (Holder) v.getTag();
		}

		holder.text_datetimevalue.setText(getItem(position).string_datetime);
		holder.text_taskvalue.setText(getItem(position).string_task);
		holder.text_isfinishvalue.setText(getItem(position).string_isFinish);

		if(position % 6 == 0) {
			// set color
			holder.relativelayout_listrow.setBackgroundColor(Color.parseColor("#B0E0E6"));
		} else if(position % 6 == 1){
			//set different color
			holder.relativelayout_listrow.setBackgroundColor(Color.parseColor("#00FFFF"));
		} else if(position % 6 == 2){
			//set different color
			holder.relativelayout_listrow.setBackgroundColor(Color.parseColor("#7FFFD4"));
		} else if(position % 6 == 3){
			//set different color
			holder.relativelayout_listrow.setBackgroundColor(Color.parseColor("#AFEEEE"));
		} else if(position % 6 == 4){
			//set different color
			holder.relativelayout_listrow.setBackgroundColor(Color.parseColor("#40E0D0"));
		} else if(position % 6 == 5){
			//set different color
			holder.relativelayout_listrow.setBackgroundColor(Color.parseColor("#00CED1"));
		}

		return v;
	}
	/**
	 * View holder for the views we need access to
	 */
	private class Holder {
		public TextView text_datetimevalue;
		public TextView text_taskvalue;
		public TextView text_isfinishvalue;
		public RelativeLayout relativelayout_listrow;
	}

	public static class SampleItem {
		public String string_datetime;
		public String string_task ;
		public String string_isFinish;

		public SampleItem(String string_datetime, String string_task, String string_isFinish) {
			this.string_datetime = string_datetime;
			this.string_task  = string_task ;
			this.string_isFinish = string_isFinish;
		}
	}
}

