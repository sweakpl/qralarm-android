package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun ComboBox(
    menuItems: List<Any>,
    selectedIndex: Int,
    onMenuItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }

        ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            ) {
                Text(
                    text = menuItems[selectedIndex].toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(all = MaterialTheme.space.medium)
                        .weight(1f),
                )

                Icon(
                    imageVector =
                    if (expanded) QRAlarmIcons.ArrowDropUp else QRAlarmIcons.ArrowDropDown,
                    contentDescription = stringResource(
                        R.string.content_description_drop_down_arrow_icon
                    ),
                    tint = Color.Black,
                    modifier = Modifier.padding(all = MaterialTheme.space.medium)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(
                MaterialTheme.space.xSmall,
                -MaterialTheme.space.xSmall
            ),
            modifier = Modifier
                .wrapContentWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            menuItems.forEachIndexed { index, content ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = content.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onMenuItemClick(index)
                        expanded = false
                    }
                )
            }
        }
    }
}