package com.sweak.qralarm.ui.screens.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.components.BackButton
import com.sweak.qralarm.ui.screens.navigateThrottled
import com.sweak.qralarm.ui.screens.popBackStackThrottled
import com.sweak.qralarm.ui.theme.space
import com.sweak.qralarm.util.Screen
import kotlinx.coroutines.launch

@Composable
fun GuideScreen(
    navController: NavHostController,
    isFirstLaunch: () -> Boolean,
    closeGuideCallback: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val previousButtonVisible = remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val composableScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page == 0) {
                previousButtonVisible.value = false
            } else if (page == 1) {
                previousButtonVisible.value = true
            }
        }
    }

    val constraints = ConstraintSet {
        val backButton = createRefFor("backButton")
        val guideText = createRefFor("guideText")
        val guidePages = createRefFor("guidePages")
        val navigationButtons = createRefFor("navigationButtons")

        constrain(backButton) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }

        constrain(guideText) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(guidePages) {
            top.linkTo(guideText.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(navigationButtons.top)
            height = Dimension.fillToConstraints
        }

        constrain(navigationButtons) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
        }
    }

    Scaffold { paddingValues ->
        ConstraintLayout(
            constraints,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            if (!isFirstLaunch()) {
                BackButton(
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.medium,
                            top = MaterialTheme.space.run {
                                large - extraSmall
                            } + paddingValues.calculateTopPadding()
                        )
                        .layoutId("backButton"),
                    navController = navController
                )
            }

            Text(
                text = stringResource(R.string.guide),
                modifier = Modifier
                    .padding(
                        MaterialTheme.space.small,
                        MaterialTheme.space.large + paddingValues.calculateTopPadding(),
                        MaterialTheme.space.small,
                        MaterialTheme.space.large
                    )
                    .layoutId("guideText"),
                style = MaterialTheme.typography.displayLarge
            )

            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.layoutId("guidePages")
            ) { page ->
                if (page == 0) {
                    GuidePageQRCode()
                } else if (page == 1) {
                    GuidePageBackgroundWork()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .layoutId("navigationButtons"),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        if (pagerState.currentPage == 1) {
                            composableScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.large,
                            bottom = MaterialTheme.space.large,
                            top = MaterialTheme.space.large
                        )
                        .alpha(if (previousButtonVisible.value) 1f else 0f)
                ) {
                    Text(
                        text = stringResource(R.string.previous),
                        modifier = Modifier.padding(MaterialTheme.space.extraSmall)
                    )
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == 0) {
                            composableScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        } else if (pagerState.currentPage == 1) {
                            if (isFirstLaunch()) {
                                closeGuideCallback()
                                navController.navigateThrottled(
                                    Screen.HomeScreen.route,
                                    lifecycleOwner
                                ) {
                                    popUpTo(Screen.GuideScreen.route) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                navController.popBackStackThrottled(lifecycleOwner)
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(
                            end = MaterialTheme.space.large,
                            bottom = MaterialTheme.space.large,
                            top = MaterialTheme.space.large
                        )
                ) {
                    Text(
                        text = stringResource(R.string.next),
                        modifier = Modifier.padding(MaterialTheme.space.extraSmall)
                    )
                }
            }
        }
    }
}