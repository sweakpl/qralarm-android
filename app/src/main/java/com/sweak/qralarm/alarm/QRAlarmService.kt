package com.sweak.qralarm.alarm

import android.app.Service
import android.content.Intent
import android.util.Log

class QRAlarmService : Service() {

    override fun onCreate() {
        super.onCreate()

        Log.i("QRAlarmService", "Service has been started...")
    }

    override fun onBind(intent: Intent?) = null
}