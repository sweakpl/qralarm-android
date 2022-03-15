package com.sweak.qralarm.ui.screens.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.navigation.NavHostController
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.shared.components.BackButton
import com.sweak.qralarm.ui.theme.space

@Composable
fun GuideScreen(
    navController: NavHostController
) {
    val constraints = ConstraintSet {
        val backButton = createRefFor("backButton")
        val guideText = createRefFor("guideText")
        val guidePages = createRefFor("guidePages")

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
        BackButton(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.space.medium,
                    top = MaterialTheme.space.large - MaterialTheme.space.extraSmall
                )
                .layoutId("backButton"),
            navController = navController
        )

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    MaterialTheme.space.large,
                    MaterialTheme.space.extraLarge
                )
                .layoutId("guidePages"),
            verticalArrangement = Arrangement.Top
        ) {

        }
    }
}