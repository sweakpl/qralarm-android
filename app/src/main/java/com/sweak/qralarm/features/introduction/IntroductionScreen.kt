package com.sweak.qralarm.features.introduction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.features.introduction.pages.IntroductionPage1
import com.sweak.qralarm.features.introduction.pages.IntroductionPage2
import com.sweak.qralarm.features.introduction.pages.IntroductionPage3
import kotlinx.coroutines.launch

@Composable
fun IntroductionScreen(onContinueClicked: () -> Unit) {
    val introductionViewModel = hiltViewModel<IntroductionViewModel>()

    ObserveAsEvents(
        flow = introductionViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                IntroductionScreenBackendEvent.IntroductionFinishConfirmed -> onContinueClicked()
            }
        }
    )

    IntroductionScreenContent(
        onEvent = { event ->
            when (event) {
                IntroductionScreenUserEvent.ContinueClicked -> introductionViewModel.onEvent(event)
            }
        }
    )
}

@Composable
private fun IntroductionScreenContent(
    onEvent: (IntroductionScreenUserEvent) -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
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
            val pagerState = rememberPagerState(pageCount = { 3 })
            val composableScope = rememberCoroutineScope()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.space.mediumLarge))

                Icon(
                    imageVector = QRAlarmIcons.QRAlarm,
                    contentDescription = stringResource(R.string.content_description_qralarm_icon),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(size = MaterialTheme.space.xxLarge)
                )

                Text(
                    text = stringResource(R.string.welcome_to_qralarm),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        start = MaterialTheme.space.mediumLarge,
                        top = MaterialTheme.space.small,
                        end = MaterialTheme.space.mediumLarge,
                        bottom = MaterialTheme.space.mediumLarge
                    )
                )

                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.space.medium)
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> IntroductionPage1(
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )
                        1 -> IntroductionPage2(
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )
                        2 -> IntroductionPage3(
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )
                    }
                }
            }

            Row(modifier = Modifier.padding(bottom = MaterialTheme.space.medium)) {
                repeat(pagerState.pageCount) {
                    val color =
                        if (pagerState.currentPage == it) MaterialTheme.colorScheme.tertiary
                        else Color.White.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.space.small)
                            .clip(CircleShape)
                            .background(color)
                            .size(MaterialTheme.space.small)
                    )
                }
            }
            
            Button(
                onClick = { 
                    if (pagerState.currentPage == 2) {
                        onEvent(IntroductionScreenUserEvent.ContinueClicked)
                    } else {
                        composableScope.launch {
                            pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.mediumLarge,
                        end = MaterialTheme.space.mediumLarge,
                        bottom = MaterialTheme.space.mediumLarge
                    )
            ) {
                Text(
                    text = stringResource(
                        if (pagerState.currentPage == 2) R.string.lets_go
                        else R.string.next_step
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun IntroductionScreenPreview() {
    QRAlarmTheme {
        IntroductionScreenContent(
            onEvent = {}
        )
    }
}