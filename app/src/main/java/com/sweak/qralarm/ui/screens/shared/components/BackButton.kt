package com.sweak.qralarm.ui.screens.shared.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.sweak.qralarm.R

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    IconButton(
        onClick = { navController.popBackStack() },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back_arrow),
            contentDescription = "Back button",
            tint = MaterialTheme.colors.secondary
        )
    }
}