package com.sweak.qralarm.core.data.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

object SystemDefaultZoneClock : Clock() {
    override fun getZone(): ZoneId = ZoneId.systemDefault()
    override fun withZone(zone: ZoneId): Clock = Clock.system(zone)
    override fun instant(): Instant = Instant.now()
    override fun millis(): Long = System.currentTimeMillis()
}
