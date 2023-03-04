package com.sweak.qralarm.ui.screens.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.shared.components.BackButton
import com.sweak.qralarm.ui.theme.space
import com.sweak.qralarm.util.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GuideScreen(
    navController: NavHostController,
    isFirstLaunch: () -> Boolean,
    closeGuideCallback: () -> Unit
) {
    val previousButtonVisible = remember { mutableStateOf(false) }
    val pagerState = rememberPagerState()
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

    ConstraintLayout(
        constraints,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colors.primary,
                        MaterialTheme.colors.primaryVariant
                    )
                )
            )
    ) {
        if (!isFirstLaunch()) {
            BackButton(
                modifier = Modifier
                    .padding(
                        start = MaterialTheme.space.medium,
                        top = MaterialTheme.space.large - MaterialTheme.space.extraSmall
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
                    MaterialTheme.space.large,
                    MaterialTheme.space.small,
                    MaterialTheme.space.large
                )
                .layoutId("guideText"),
            style = MaterialTheme.typography.h1
        )

        HorizontalPager(
            modifier = Modifier.layoutId("guidePages"),
            state = pagerState,
            count = 2
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
                .layoutId("navigationButtons"),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (pagerState.currentPage == 1) {
                        composableScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent
                ),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
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
                            navController.navigate(Screen.HomeScreen.route) {
                                popUpTo(Screen.GuideScreen.route) {
                                    inclusive = true
                                }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                ),
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