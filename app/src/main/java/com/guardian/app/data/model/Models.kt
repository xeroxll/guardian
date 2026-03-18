package com.guardian.app.data.model

import java.util.UUID

data class BlacklistedApp(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class TrustedApp(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class SecurityEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: EventType,
    val title: String,
    val description: String,
    val packageName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class EventType {
    USB_ENABLED,
    USB_DISABLED,
    APP_BLOCKED,
    APP_INSTALLED,
    APP_REMOVED,
    PROTECTION_ENABLED,
    PROTECTION_DISABLED,
    SCAN_COMPLETED,
    SMS_BLOCKED,
    CALL_BLOCKED
}

data class AppStats(
    val threatsBlocked: Int = 0,
    val appsScanned: Int = 0,
    val lastScanTime: Long = 0
)

data class ScanHistory(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val appsScanned: Int,
    val threatsFound: Int,
    val scanType: ScanType
)

enum class ScanType {
    LOCAL,
    VIRUS_TOTAL
}
