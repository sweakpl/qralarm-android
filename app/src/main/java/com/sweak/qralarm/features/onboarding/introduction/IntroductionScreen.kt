package com.sweak.qralarm.features.onboarding.introduction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.introduction.pages.IntroductionPage1
import com.sweak.qralarm.features.onboarding.introduction.pages.IntroductionPage2
import com.sweak.qralarm.features.onboarding.introduction.pages.IntroductionPage3
import kotlinx.coroutines.launch

@Composable
fun IntroductionScreen(onContinueClicked: () -> Unit) {
    Scaffold { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac)))
                .padding(paddingValues = paddingValues)
        ) {
            val pagerState = rememberPagerState(pageCount = { 3 })
            val composableScope = rememberCoroutineScope()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(
                            top = MaterialTheme.space.mediumLarge,
                            bottom = MaterialTheme.space.medium
                        )
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
                        if (pagerState.currentPage == it) LocalContentColor.current
                        else LocalContentColor.current.copy(alpha = 0.5f)

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
                    if (pagerState.currentPage == pagerState.pageCount - 1) {
                        onContinueClicked()
                    } else {
                        composableScope.launch {
                            pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.mediumLarge,
                        end = MaterialTheme.space.mediumLarge,
                        bottom = MaterialTheme.space.mediumLarge
                    )
            ) {
                Text(text = stringResource(R.string.next_step))
            }
        }
    }
}

@Preview
@Composable
private fun IntroductionScreenPreview() {
    QRAlarmTheme {
        IntroductionScreen(onContinueClicked = {})
    }
}
