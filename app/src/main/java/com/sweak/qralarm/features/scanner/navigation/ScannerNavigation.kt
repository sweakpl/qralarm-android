package com.sweak.qralarm.features.scanner.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.scanner.ScannerScreen

const val SCANNER_SCREEN_ROUTE = "scannerScreen"

fun NavController.navigateToScanner() = navigate(route = SCANNER_SCREEN_ROUTE)

fun NavGraphBuilder.scannerScreen(onCustomCodeSaved: () -> Unit) {
    composable(route = SCANNER_SCREEN_ROUTE) {
        ScannerScreen(onCustomCodeSaved = onCustomCodeSaved)
    }
}