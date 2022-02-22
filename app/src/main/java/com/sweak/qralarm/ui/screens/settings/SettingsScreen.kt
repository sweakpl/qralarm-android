package com.sweak.qralarm.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.theme.space

@Composable
fun SettingsScreen(
    navController: NavHostController
) {
    val scrollState = rememberScrollState()

    val snoozeDurations = listOf("10", "5", "3", "2")
    val snoozeDurationsDropdownMenuExpanded = remember { mutableStateOf(false) }
    val selectedSnoozeDurationIndex = remember { mutableStateOf(0) }

    val snoozeCounts = listOf("3", "2", "1", "0")
    val snoozeCountsDropdownMenuExpanded = remember { mutableStateOf(false) }
    val selectedSnoozeCountIndex = remember { mutableStateOf(0) }

    val constraints = ConstraintSet {
        val backButton = createRefFor("backButton")
        val settingsText = createRefFor("settingsText")
        val settingsColumn = createRefFor("settingsColumn")

        constrain(backButton) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }

        constrain(settingsText) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(settingsColumn) {
            top.linkTo(settingsText.bottom)
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
                .layoutId("menuButton"),
            navController = navController
        )

        Text(
            text = stringResource(R.string.settings),
            modifier = Modifier
                .padding(
                    MaterialTheme.space.small,
                    MaterialTheme.space.large,
                    MaterialTheme.space.small,
                    MaterialTheme.space.large
                )
                .layoutId("settingsText"),
            style = MaterialTheme.typography.h1
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    MaterialTheme.space.large,
                    MaterialTheme.space.extraLarge
                )
                .verticalScroll(scrollState)
                .layoutId("settingsColumn"),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.large)
                        .weight(1f),
                    text = stringResource(R.string.snooze_duration),
                    style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.Normal)
                )

                ComboBox(
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),
                    menuItems = snoozeDurations,
                    menuExpandedState = snoozeDurationsDropdownMenuExpanded.value,
                    selectedIndex = selectedSnoozeDurationIndex.value,
                    updateMenuExpandedStatus = { snoozeDurationsDropdownMenuExpanded.value = true },
                    onDismissMenuView = { snoozeDurationsDropdownMenuExpanded.value = false },
                    onMenuItemClick = { index ->
                        selectedSnoozeDurationIndex.value = index
                        snoozeDurationsDropdownMenuExpanded.value = false
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(MaterialTheme.space.large))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.large)
                        .weight(1f),
                    text = stringResource(R.string.number_of_snoozes),
                    style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.Normal)
                )

                ComboBox(
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),
                    menuItems = snoozeCounts,
                    menuExpandedState = snoozeCountsDropdownMenuExpanded.value,
                    selectedIndex = selectedSnoozeCountIndex.value,
                    updateMenuExpandedStatus = { snoozeCountsDropdownMenuExpanded.value = true },
                    onDismissMenuView = { snoozeCountsDropdownMenuExpanded.value = false },
                    onMenuItemClick = { index ->
                        selectedSnoozeCountIndex.value = index
                        snoozeCountsDropdownMenuExpanded.value = false
                    }
                )
            }
        }
    }
}

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

@Composable
fun ComboBox(
    modifier: Modifier = Modifier,
    menuItems: List<Any>,
    menuExpandedState: Boolean,
    selectedIndex: Int,
    updateMenuExpandedStatus: () -> Unit,
    onDismissMenuView: () -> Unit,
    onMenuItemClick: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                onClick = updateMenuExpandedStatus
            )
    ) {
        val constraints = ConstraintSet {
            val selectionLabel = createRefFor("selectionLabel")
            val arrowIcon = createRefFor("arrowIcon")

            constrain(selectionLabel) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(arrowIcon.start)
                width = Dimension.fillToConstraints
            }

            constrain(arrowIcon) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        }

        ConstraintLayout(
            constraints,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .layoutId("selectionLabel")
                    .padding(
                        start = MaterialTheme.space.medium,
                        top = MaterialTheme.space.extraSmall,
                        bottom = MaterialTheme.space.extraSmall
                    )
                    .wrapContentHeight(),
                text = menuItems[selectedIndex].toString(),
                style = MaterialTheme.typography.h2
            )

            Icon(
                modifier = Modifier
                    .layoutId("arrowIcon")
                    .size(24.dp, 20.dp)
                    .padding(end = MaterialTheme.space.small),
                painter = painterResource(R.drawable.ic_dropdown_arrow),
                contentDescription = "Dropdown menu icon",
                tint = Color.White
            )

            DropdownMenu(
                expanded = menuExpandedState,
                onDismissRequest = onDismissMenuView,
                offset = DpOffset(
                    MaterialTheme.space.extraSmall,
                    -MaterialTheme.space.extraSmall
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = MaterialTheme.colors.secondary,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                menuItems.forEachIndexed { index, content ->
                    DropdownMenuItem(
                        onClick = { onMenuItemClick(index) }
                    ) {
                        Text(
                            text = content.toString(),
                            style = MaterialTheme.typography.h2
                        )
                    }
                }
            }
        }
    }
}