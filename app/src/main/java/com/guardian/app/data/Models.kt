package com.guardian.app.data

import java.util.UUID

data class BlacklistedApp(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String
)

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val type: LogType,
    val title: String,
    val desc: String,
    val time: String
)

enum class LogType {
    BLOCK,
    THREAT,
    CHECK
}

data class Stats(
    val threats: Int = 0,
    val blocks: Int = 0,
    val checks: Int = 0
)
