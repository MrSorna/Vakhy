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
package com.example.mramirhosein.alarm.service;

import com.example.mramirhosein.alarm.Alarm;
import com.example.mramirhosein.alarm.alert.AlarmAlert;
import com.example.mramirhosein.alarm.alert.StaticWakeLock;
import com.example.mramirhosein.alarm.database.Database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class AlarmServiceBroadcastReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                    Database.init(context);
                    List<Alarm> alarms = Database.getAll();
                    for (Alarm alarm : alarms) {
                        if (alarm.getAlarmActive()) {
                            if (!alarm.getFlag().equalsIgnoreCase("null") && alarm.getFlag().equalsIgnoreCase("false")) {
                                if (isTimeOK(alarm)) {
                                    StaticWakeLock.lockOn(context);
                                    Intent i = new Intent(context, AlarmAlert.class);
                                    i.putExtra("id", alarm.getId());
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(i);
                                    return;
                                } else {
                                    alarm.setFlag("true");
                                    Database.update(alarm);
                                }
                            }
                        }
                    }
                    Database.deactivate();
                }
            }
        } catch (Exception e) {
            Log.e("dbg","AlarmServiceBroadcastReciever : "+e.getMessage(),e);
        }


        Intent serviceIntent = new Intent(context, AlarmService.class);
        context.startService(serviceIntent);

    }


    public boolean isTimeOK(Alarm alarm) {
        long alarmTime = alarm.getAlarmTime().getTimeInMillis() % 86400000;
        long currentTime = System.currentTimeMillis() % 86400000;
        long timeDifference = (currentTime - alarmTime)/ 60000;
//        Log.d("dbg", "alarmTime          : " + String.valueOf(alarmTime));
//        Log.d("dbg", "currentTime        : " + String.valueOf(currentTime));
//        Log.d("dbg", "timeDifference     : " + String.valueOf(timeDifference));
        return (timeDifference <= 30 && timeDifference >= 0);
    }

}
