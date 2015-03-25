package com.clozerr.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by S.ARAVIND on 3/25/2015.
 */
public class AlarmTester {
    private AlarmManager mAlarmManager;
    private Calendar mCalendar;

    public AlarmTester(Context context) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mCalendar.set(Calendar.HOUR_OF_DAY, 20);
        mCalendar.set(Calendar.MINUTE, 59);
    }

    public void execute(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent actionIntent = PendingIntent.getBroadcast(context, 1234, intent, 0);
        mAlarmManager.setInexactRepeating(AlarmManager.RTC, mCalendar.getTimeInMillis(),
                TimeUnit.MILLISECONDS.convert(20L, TimeUnit.SECONDS), actionIntent);
    }
}
