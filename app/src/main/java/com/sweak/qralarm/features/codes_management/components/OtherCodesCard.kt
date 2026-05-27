package com.sweak.qralarm.features.codes_management.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.model.Code

@Composable
fun OtherCodesCard(
    codes: List<Code>,
    isPickingDefault: Boolean,
    onEditClicked: (Code) -> Unit,
    onCodePickedAsDefault: (Code) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.small),
            modifier = Modifier.padding(all = MaterialTheme.space.medium)
        ) {
            if (codes.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_codes_yet),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.no_codes_yet_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = MaterialTheme.space.xSmall)
                )
            } else {
                codes.forEachIndexed { index, code ->
                    val isNotFirstItem = index > 0

                    Column(
                        modifier = Modifier.then(
                            if (isNotFirstItem) Modifier.padding(top = MaterialTheme.space.xSmall)
                            else Modifier
                        )
                    ) {
                        if (isNotFirstItem) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = LocalContentColor.current,
                                modifier = Modifier.padding(bottom = MaterialTheme.space.small)
                            )
                        }

                        if (isPickingDefault) {
                            CodeRow(
                                code = code,
                                trailingIcon = QRAlarmIcons.Star,
                                trailingIconContentDescription = stringResource(
                                    R.string.content_description_star_icon
                                ),
                                onRowClicked = { onCodePickedAsDefault(code) },
                                onTrailingIconClicked = { onCodePickedAsDefault(code) }
                            )
                        } else {
                            CodeRow(
                                code = code,
                                trailingIcon = QRAlarmIcons.Edit,
                                trailingIconContentDescription = stringResource(
                                    R.string.content_description_edit_icon
                                ),
                                onTrailingIconClicked = { onEditClicked(code) }
                            )
                        }
                    }
                }
            }
        }
    }
}
