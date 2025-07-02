package com.example.hipnosprueba.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.hipnosprueba.R
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Verificar permiso antes de mostrar notificación
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showNotification(context)
        }

        // Reprogramar para el próximo día
        val prefs = context.getSharedPreferences("recordatorio_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("hora", 8)
        val minute = prefs.getInt("minuto", 0)

        scheduleNextAlarm(context, hour, minute)
    }

    private fun showNotification(context: Context) {
        val channelId = "hipnos_reminder_channel"
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios Hipnos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para recordatorios diarios"
            }
            notificationManager?.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.hipnos_logo_2)
            .setContentTitle("¡Hora de Hipnos!")
            .setContentText("Es momento de tu sesión diaria")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager?.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun scheduleNextAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextIntent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1) // Programar para mañana
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Manejar error de permiso
        }
    }
}