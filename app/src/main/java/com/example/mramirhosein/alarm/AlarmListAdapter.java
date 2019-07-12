/* Copyright 2014 Sheldon Neilson www.neilson.co.za
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.example.mramirhosein.alarm;

import java.util.ArrayList;
import java.util.List;


import com.example.mramirhosein.alarm.database.Database;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmListAdapter extends BaseAdapter {

	private AlarmActivity alarmActivity;
	private List<Alarm> alarms = new ArrayList<Alarm>();

	public static final String ALARM_FIELDS[] = { Database.COLUMN_ALARM_ACTIVE,
			Database.COLUMN_ALARM_TIME, Database.COLUMN_ALARM_DAYS };

	public AlarmListAdapter(AlarmActivity alarmActivity) {
		this.alarmActivity = alarmActivity;
//		Database.init(alarmActivity);
//		alarms = Database.getAll();
	}

	@Override
	public int getCount() {
		return alarms.size();
	}

	@Override
	public Object getItem(int position) {
		return alarms.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		if (null == view)
			view = LayoutInflater.from(alarmActivity).inflate(
					R.layout.alarm_list_element, null);

		Alarm alarm = (Alarm) getItem(position);

		 CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox_alarm_active1);
		 checkBox.setChecked(alarm.getAlarmActive());
		 checkBox.setTag(position);
		 checkBox.setOnClickListener((OnClickListener) alarmActivity);


//		LinearLayout linearLayout=(LinearLayout)view.findViewById(R.id.linearLayout_alarm_layout);
//		linearLayout.setTag(position);
//		linearLayout.setOnClickListener((OnClickListener) alarmActivity);

		Typeface face = Typeface.createFromAsset(alarmActivity.getAssets(),
				"Fonts/digital.ttf");
		TextView alarmTimeView = (TextView) view
				.findViewById(R.id.textView_alarm_time1);
		alarmTimeView.setTypeface(face);
		alarmTimeView.setText(alarm.getAlarmTimeString());

		TextView alarmLableView = (TextView) view
				.findViewById(R.id.textView_alarm_lable);
		alarmLableView.setText(alarm.getAlarmName());

		TextView alarmDaysView = (TextView) view
					.findViewById(R.id.textView_alarm_days1);

		ImageView alarmDelete=(ImageView)view.findViewById(R.id.imageView_alarm_delete);
		alarmDelete.setTag(position);
		alarmDelete.setOnClickListener((OnClickListener) alarmActivity);

//		alarmDaysView.measure(0, 0);
//
//		if(alarm.getRepeatDaysString().length()>=(alarmDaysView.getMeasuredWidth()/17)){
//			Toast.makeText(alarmActivity.getApplicationContext(),String.valueOf(alarm.getRepeatDaysString()),Toast.LENGTH_LONG).show();
//			alarmDaysView.setText(alarm.getRepeatDaysString().substring(0,(alarmDaysView.getMeasuredWidth()/17))+"...");
//		}else {
			alarmDaysView.setText(alarm.getRepeatDaysString());
//		}

		return view;
	}

	public List<Alarm> getMathAlarms() {
		return alarms;
	}

	public void setMathAlarms(List<Alarm> alarms) {
		this.alarms = alarms;
	}

}
