package com.sweak.qralarm.core.data.backup.dto

import kotlinx.serialization.Serializable

/** codes.json - the saved code library. */
@Serializable
data class CodesDto(
    val codes: List<CodeDto> = emptyList()
)

@Serializable
data class CodeDto(
    val codeId: Long = 0,
    val value: String = "",
    val name: String? = null
)
