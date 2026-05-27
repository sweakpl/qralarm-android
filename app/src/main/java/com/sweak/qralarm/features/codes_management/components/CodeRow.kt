package com.sweak.qralarm.features.codes_management.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.model.Code

@Composable
fun CodeRow(
    code: Code,
    trailingIcon: ImageVector,
    trailingIconContentDescription: String,
    onTrailingIconClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onRowClicked: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onRowClicked != null) Modifier.clickable { onRowClicked() } else Modifier
            )
    ) {
        Icon(
            imageVector = QRAlarmIcons.QrCode,
            contentDescription = stringResource(R.string.content_description_qr_code_icon),
            modifier = Modifier.padding(end = MaterialTheme.space.smallMedium)
        )

        Column(modifier = Modifier.weight(1f)) {
            if (code.name != null) {
                Text(
                    text = code.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = code.value,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = code.value,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(onClick = onTrailingIconClicked) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = trailingIconContentDescription
            )
        }
    }
}
