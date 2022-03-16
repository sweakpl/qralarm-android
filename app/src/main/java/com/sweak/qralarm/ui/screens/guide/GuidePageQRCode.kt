package com.sweak.qralarm.ui.screens.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.theme.space

@Composable
fun GuidePageQRCode() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.space.large)
                .wrapContentWidth(),
            text = stringResource(R.string.get_your_qrcode),
            style = MaterialTheme.typography.h2
        )

        Row(
            modifier = Modifier
                .padding(MaterialTheme.space.large)
                .wrapContentWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(end = MaterialTheme.space.medium),
                painter = painterResource(id = R.drawable.ic_download),
                contentDescription = "Download icon",
                tint = MaterialTheme.colors.secondary
            )

            Icon(
                modifier = Modifier.padding(start = MaterialTheme.space.medium),
                painter = painterResource(id = R.drawable.ic_scan),
                contentDescription = "Scan icon",
                tint = MaterialTheme.colors.secondary
            )
        }

        Text(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.space.large)
                .fillMaxWidth(),
            text = stringResource(R.string.download_or_scan_code_description),
            style = MaterialTheme.typography.body1
        )
    }
}