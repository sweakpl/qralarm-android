package com.sweak.qralarm.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme

@Composable
fun CodeNameChip(
    codeName: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                text = codeName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = QRAlarmIcons.QrCode,
                contentDescription = stringResource(R.string.content_description_qr_code_icon),
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            disabledLabelColor = MaterialTheme.colorScheme.onPrimary,
            disabledLeadingIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = false,
            disabledBorderColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier.clearAndSetSemantics { contentDescription = codeName }
    )
}

@Preview
@Composable
private fun CodeNameChipPreview() {
    QRAlarmTheme {
        CodeNameChip(codeName = "Coffee bag code")
    }
}
