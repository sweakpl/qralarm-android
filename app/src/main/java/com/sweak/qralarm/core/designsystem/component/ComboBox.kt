package com.sweak.qralarm.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun QRAlarmComboBox(
    menuItems: List<Any>,
    selectedIndex: Int,
    onMenuItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = if (MaterialTheme.isQRAlarmTheme) Color.White else Color.Unspecified,
    contentColor: Color = if (MaterialTheme.isQRAlarmTheme) Color.Black else Color.Unspecified
) {
    Column(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            ) {
                Text(
                    text = menuItems[selectedIndex].toString(),
                    style = MaterialTheme.typography.titleMedium,
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
            containerColor = with (MaterialTheme) {
                if (isQRAlarmTheme) Color.White else colorScheme.surfaceContainer
            }
        ) {
            val itemContentColor =
                if (MaterialTheme.isQRAlarmTheme) Color.Black
                else Color.Unspecified

            menuItems.forEachIndexed { index, content ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = content.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    onClick = {
                        onMenuItemClick(index)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = itemContentColor,
                        leadingIconColor = itemContentColor
                    )
                )
            }
        }
    }
}