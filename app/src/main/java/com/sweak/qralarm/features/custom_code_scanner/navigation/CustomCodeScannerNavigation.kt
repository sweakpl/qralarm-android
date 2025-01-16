package com.sweak.qralarm.features.custom_code_scanner.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.custom_code_scanner.CustomCodeScannerScreen

const val CUSTOM_CODE_SCANNER_SCREEN_ROUTE = "customCodeScannerScreen"

fun NavController.navigateToCustomCodeScanner() = navigate(route = CUSTOM_CODE_SCANNER_SCREEN_ROUTE)

fun NavGraphBuilder.customCodeScannerScreen(
    onCustomCodeSaved: () -> Unit,
    onCloseClicked: () -> Unit
) {
    composable(route = CUSTOM_CODE_SCANNER_SCREEN_ROUTE) {
        CustomCodeScannerScreen(
            onCustomCodeSaved = onCustomCodeSaved,
            onCloseClicked = onCloseClicked
        )
    }
}