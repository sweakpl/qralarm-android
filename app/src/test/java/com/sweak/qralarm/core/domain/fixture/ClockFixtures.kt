package com.sweak.qralarm.core.domain.fixture

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

val TEST_ZONE: ZoneId = ZoneId.of("Europe/Warsaw")

fun fixedClock(dateTime: LocalDateTime): Clock =
    Clock.fixed(Instant.ofEpochMilli(epochMillisAt(dateTime)), TEST_ZONE)

fun epochMillisAt(dateTime: LocalDateTime): Long =
    dateTime.atZone(TEST_ZONE).toInstant().toEpochMilli()
