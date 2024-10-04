package com.sweak.qralarm.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.sweak.qralarm.R

object QRAlarmIcons {
    val QRAlarm @Composable get() = ImageVector.vectorResource(R.drawable.ic_qralarm)

    val Add = Icons.Outlined.Add
    val Menu = Icons.Outlined.Menu
}