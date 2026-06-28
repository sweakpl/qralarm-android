package com.sweak.qralarm.core.ui.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.model.Code

@Composable
fun NamedCodePicker(
    availableCodes: List<Code>,
    onCodeChosen: (Code) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (MaterialTheme.isQRAlarmTheme) Color.White else Color.Unspecified,
                contentColor = if (MaterialTheme.isQRAlarmTheme) Color.Black else Color.Unspecified
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            ) {
                Text(
                    text = stringResource(R.string.click_to_choose),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(all = MaterialTheme.space.medium)
                        .weight(1f)
                )

                Icon(
                    imageVector = if (expanded) QRAlarmIcons.ArrowDropUp else QRAlarmIcons.ArrowDropDown,
                    contentDescription = stringResource(R.string.content_description_drop_down_arrow_icon),
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
            containerColor = if (MaterialTheme.isQRAlarmTheme) Color.White
            else MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            val itemContentColor =
                if (MaterialTheme.isQRAlarmTheme) Color.Black else Color.Unspecified

            availableCodes.forEach { code ->
                DropdownMenuItem(
                    text = { NamedCodeRow(code = code, contentColor = itemContentColor) },
                    onClick = {
                        onCodeChosen(code)
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

@Composable
private fun NamedCodeRow(code: Code, contentColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = QRAlarmIcons.QrCode,
            contentDescription = stringResource(R.string.content_description_qr_code_icon),
            tint = contentColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = MaterialTheme.space.small)
        )

        Column {
            if (code.name != null) {
                Text(
                    text = code.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor.takeIf { it != Color.Unspecified } ?: Color.Unspecified
                )
                Text(
                    text = code.value,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor.takeIf { it != Color.Unspecified } ?: Color.Unspecified
                )
            } else {
                Text(
                    text = code.value,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor.takeIf { it != Color.Unspecified } ?: Color.Unspecified
                )
            }
        }
    }
}

@Preview
@Composable
private fun NamedCodePickerPreview() {
    QRAlarmTheme {
        NamedCodePicker(
            availableCodes = listOf(
                Code(value = "StopAlarm", name = "Stop alarm code"),
                Code(value = "472839472890421341")
            ),
            onCodeChosen = {}
        )
    }
}
