package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class QRAlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val time = Calendar.getInstance().time
        Log.i("QRAlarmReceiver", "Alarm intent has been received at: $time")
    }
}