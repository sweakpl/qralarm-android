package com.sweak.qralarm.features.custom_code_scanner.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.custom_code_scanner.CustomCodeScannerScreen
import com.sweak.qralarm.features.custom_code_scanner.CustomCodeScannerScreen2

const val CUSTOM_CODE_SCANNER_SCREEN_ROUTE = "customCodeScannerScreen"
const val SHOULD_SCAN_FOR_DEFAULT_CODE = "shouldScanForDefaultCode"

fun NavController.navigateToCustomCodeScanner(
    shouldScanForDefaultCode: Boolean
) = navigate(
    route = "$CUSTOM_CODE_SCANNER_SCREEN_ROUTE/$shouldScanForDefaultCode"
)

fun NavGraphBuilder.customCodeScannerScreen(
    onCustomCodeSaved: () -> Unit,
    onCloseClicked: () -> Unit
) {
    composable(
        route = "$CUSTOM_CODE_SCANNER_SCREEN_ROUTE/{$SHOULD_SCAN_FOR_DEFAULT_CODE}",
        arguments = listOf(
            navArgument(SHOULD_SCAN_FOR_DEFAULT_CODE) {
                type = NavType.BoolType
            }
        )
    ) {
        CustomCodeScannerScreen2(
            onCustomCodeSaved = onCustomCodeSaved,
            onCloseClicked = onCloseClicked
        )
    }
}