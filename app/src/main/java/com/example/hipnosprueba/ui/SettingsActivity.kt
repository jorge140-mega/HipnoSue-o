package com.example.hipnosprueba.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hipnosprueba.R
import com.example.hipnosprueba.databinding.ActivitySettingsBinding
import com.example.hipnosprueba.ui.NotificationReceiver
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val PREFS_NAME = "recordatorio_prefs"
    private val HOUR_KEY = "hora"
    private val MINUTE_KEY = "minuto"
    private val SWITCH_STATE_KEY = "switch_state"
    private var savedHour = 8
    private var savedMinute = 0
    private var timePickerDialog: TimePickerDialog? = null

    // Registro para permisos
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showTimePickerDialogInternal()
        } else {
            Toast.makeText(
                this,
                "Permiso denegado. No se mostrarán notificaciones.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val exactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Volver a intentar después de regresar de la configuración
        scheduleReminder(savedHour, savedMinute)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar la hora guardada previamente
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        savedHour = prefs.getInt(HOUR_KEY, 8)
        savedMinute = prefs.getInt(MINUTE_KEY, 0)

        // Cargar el estado del Switch
        binding.notificationsSwitch.isChecked = prefs.getBoolean(SWITCH_STATE_KEY, false)

        // Listener para guardar el estado del Switch cuando cambia
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(SWITCH_STATE_KEY, isChecked)
                .apply()

            // Si se desactiva el switch, cancelar las alarmas
            if (!isChecked) {
                cancelReminder()
            }
        }
        // Mostrar la hora actual
        updateReminderText(savedHour, savedMinute)

        binding.reminderButton.setOnClickListener {
            checkNotificationPermission()
        }

        // Programar alarma al iniciar
        scheduleReminder(savedHour, savedMinute)

        // Barra de navegación inferior
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_back -> {
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun cancelReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Toast.makeText(this, "Recordatorios desactivados", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Restaurar cualquier estado necesario
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        savedHour = prefs.getInt(HOUR_KEY, 8)
        savedMinute = prefs.getInt(MINUTE_KEY, 0)
        updateReminderText(savedHour, savedMinute)
    }

    override fun onPause() {
        super.onPause()
        // Cerrar el diálogo si está abierto
        timePickerDialog?.dismiss()
        timePickerDialog = null
    }

    private fun checkNotificationPermission() {
        // Solo necesitamos verificar permiso para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showTimePickerDialogInternal()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        this,
                        "Las notificaciones son necesarias para los recordatorios",
                        Toast.LENGTH_LONG
                    ).show()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            showTimePickerDialogInternal()
        }
    }

    private fun showTimePickerDialog() {
        // Verificar si ya hay un diálogo mostrándose
        if (timePickerDialog != null && timePickerDialog!!.isShowing) {
            return
        }
        showTimePickerDialogInternal()
    }

    private fun showTimePickerDialogInternal() {
        timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                // Guardar la nueva hora seleccionada
                savedHour = hourOfDay
                savedMinute = minute

                // Guardar en SharedPreferences
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putInt(HOUR_KEY, hourOfDay)
                    .putInt(MINUTE_KEY, minute)
                    .apply()

                // Actualizar texto
                updateReminderText(hourOfDay, minute)

                // Programar alarma
                scheduleReminder(hourOfDay, minute)

                Toast.makeText(
                    this,
                    "Recordatorio configurado",
                    Toast.LENGTH_SHORT
                ).show()
            },
            savedHour,
            savedMinute,
            false // Formato 12 horas (true) o 24 horas (false)
        ).apply {
            setButton(TimePickerDialog.BUTTON_POSITIVE, "Aceptar") { dialog, _ ->
                dialog.dismiss()
            }

            setButton(TimePickerDialog.BUTTON_NEGATIVE, "Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
        }

        timePickerDialog?.show()
    }

    private fun updateReminderText(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeString = timeFormat.format(calendar.time)

        binding.reminderTimeText.text = "Hora actual: $timeString"
    }

    private fun scheduleReminder(hour: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancelar alarma existente
        alarmManager.cancel(pendingIntent)


        // Configurar calendario
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Si la hora ya pasó, programar para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Verificar permiso para alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Pedir permiso si no está concedido
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    exactAlarmPermissionLauncher.launch(intent)
                }
            } catch (e: SecurityException) {
                // Manejar excepción en caso de permiso revocado
                Toast.makeText(
                    this,
                    "Error: Permiso para alarmas exactas revocado",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                exactAlarmPermissionLauncher.launch(intent)
            }
        } else {
            // Para versiones anteriores
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}