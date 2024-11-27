package com.sweak.qralarm.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.AppSettingsAlt
import androidx.compose.material.icons.outlined.AppShortcut
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.sweak.qralarm.R

object QRAlarmIcons {
    val QRAlarm @Composable get() = ImageVector.vectorResource(R.drawable.ic_qralarm)
    val NoQRCode @Composable get() = ImageVector.vectorResource(R.drawable.ic_no_qr_code)
    val SkipNextAlarm @Composable get() = ImageVector.vectorResource(R.drawable.ic_skip_next_alarm)

    val Add = Icons.Outlined.Add
    val Menu = Icons.Outlined.Menu
    val Close = Icons.Outlined.Close
    val Done = Icons.Outlined.Done
    val ForwardArrow = Icons.AutoMirrored.Outlined.ArrowForwardIos
    val BackArrow = Icons.AutoMirrored.Outlined.ArrowBackIos
    val ArrowDropUp = Icons.Outlined.ArrowDropUp
    val ArrowDropDown = Icons.Outlined.ArrowDropDown
    val Play = Icons.Outlined.PlayArrow
    val Stop = Icons.Outlined.Stop
    val Edit = Icons.Outlined.Edit
    val More = Icons.Outlined.MoreVert
    val QrCodeScanner = Icons.Outlined.QrCodeScanner
    val Delete = Icons.Outlined.Delete
    val Camera = Icons.Outlined.CameraAlt
    val Alarm = Icons.Outlined.Alarm
    val Notification = Icons.Outlined.Notifications
    val FullScreen = Icons.Outlined.Fullscreen
    val AutomaticSettings = Icons.Outlined.SettingsSuggest
    val AppSettings = Icons.Outlined.AppSettingsAlt
    val SpecialAppSettings = Icons.Outlined.AppShortcut
    val Star = Icons.Outlined.Star
    val SpecialAlarmSettings = Icons.Outlined.Tune
    val EarlyAccess = Icons.Default.Bolt
    val AccessForever = Icons.Outlined.AllInclusive
    val Undo = Icons.AutoMirrored.Outlined.Undo
    val Label = Icons.AutoMirrored.Outlined.Label
}