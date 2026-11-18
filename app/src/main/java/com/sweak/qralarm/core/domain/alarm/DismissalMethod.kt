package com.sweak.qralarm.core.domain.alarm

enum class DismissalMethod {
    NONE, CODE, OBJECT, CUSTOM_OBJECT;

    val requiresCamera: Boolean get() = this != NONE
    val isObject: Boolean get() = this == OBJECT || this == CUSTOM_OBJECT
}
