package com.sweak.qralarm.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun HomeScreen(onAddNewAlarm: () -> Unit) {
    HomeScreenContent(
        onEvent = { event ->
            when (event) {
                HomeScreenUserEvent.AddNewAlarm -> onAddNewAlarm()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(onEvent: (HomeScreenUserEvent) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { /* TODO */ }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.Menu,
                            contentDescription =
                            stringResource(R.string.content_description_menu_icon)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(HomeScreenUserEvent.AddNewAlarm) },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    imageVector = QRAlarmIcons.Add,
                    contentDescription = stringResource(R.string.content_description_add_icon),
                    modifier = Modifier.size(size = MaterialTheme.space.large)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.medium,
                        top = MaterialTheme.space.medium,
                        end = MaterialTheme.space.small
                    )
            ) {
                Text(
                    text = stringResource(R.string.alarms),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                IconButton(
                    onClick = { onEvent(HomeScreenUserEvent.AddNewAlarm) }
                ) {
                    Icon(
                        imageVector = QRAlarmIcons.Add,
                        contentDescription = stringResource(R.string.content_description_add_icon),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(size = MaterialTheme.space.large)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenContentPreview() {
    QRAlarmTheme {
        HomeScreenContent(
            onEvent = {}
        )
    }
}