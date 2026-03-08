package com.sweak.qralarm.core.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(
    private val state: NavigationState
) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun navigateToTopLevelAndClear(route: NavKey) {
        require(route in state.backStacks) {
            "Route must be a top-level route: $route"
        }
        val stack = state.backStacks[route]!!
        while (stack.size > 1) {
            stack.removeLastOrNull()
        }
        state.topLevelRoute = route
        state.startRoute = route
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}

