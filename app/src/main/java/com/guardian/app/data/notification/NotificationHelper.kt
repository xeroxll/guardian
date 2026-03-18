package com.guardian.app.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.guardian.app.GuardianApp
import com.guardian.app.R
import com.guardian.app.ui.MainActivity

object NotificationHelper {
    
    private const val NOTIFICATION_ID_SCAN_COMPLETE = 1001
    private const val NOTIFICATION_ID_THREAT_FOUND = 1002
    
    fun showScanCompleteNotification(context: Context, threatsFound: Int, appsScanned: Int) {
        val title = if (threatsFound > 0) "⚠️ Сканирование завершено" else "✅ Сканирование завершено"
        val message = if (threatsFound > 0) {
            "Найдено угроз: $threatsFound из $appsScanned приложений"
        } else {
            "Проверено приложений: $appsScanned. Угроз не найдено."
        }
        
        showNotification(context, NOTIFICATION_ID_SCAN_COMPLETE, title, message, GuardianApp.CHANNEL_ALERTS)
    }
    
    fun showThreatFoundNotification(context: Context, appName: String, threatType: String) {
        showNotification(
            context,
            NOTIFICATION_ID_THREAT_FOUND,
            "🚨 Обнаружена угроза!",
            "$appName - $threatType",
            GuardianApp.CHANNEL_BLOCKED
        )
    }
    
    private fun showNotification(context: Context, id: Int, title: String, message: String, channel: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }
}
