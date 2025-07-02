package com.example.hipnosprueba.ui.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hipnosprueba.R
import com.example.hipnosprueba.databinding.FragmentSettingsBinding
import com.example.hipnosprueba.ui.NotificationReceiver
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val PREFS_NAME = "recordatorio_prefs"
    private val HOUR_KEY = "hora"
    private val MINUTE_KEY = "minuto"
    private var savedHour = 8
    private var savedMinute = 0

    // Registro para solicitar permiso de notificaciones
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showTimePickerDialog()
        } else {
            Toast.makeText(
                requireContext(),
                "Permiso denegado. No se mostrarán notificaciones.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Registro para solicitar permiso de alarmas exactas
    private val exactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Volver a intentar después de regresar de la configuración
        scheduleReminder(savedHour, savedMinute)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar la hora guardada previamente
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        savedHour = prefs.getInt(HOUR_KEY, 8)
        savedMinute = prefs.getInt(MINUTE_KEY, 0)

        // Mostrar la hora actual
        updateReminderText(savedHour, savedMinute)

        binding.reminderButton.setOnClickListener {
            checkNotificationPermission()
        }

        // Programar alarma al iniciar
        scheduleReminder(savedHour, savedMinute)
    }

    private fun checkNotificationPermission() {
        // Solo necesitamos verificar permiso para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showTimePickerDialog()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        requireContext(),
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
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                savedHour = hourOfDay
                savedMinute = minute

                // Guardar en SharedPreferences
                val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putInt(HOUR_KEY, hourOfDay)
                    .putInt(MINUTE_KEY, minute)
                    .apply()

                // Actualizar texto
                updateReminderText(hourOfDay, minute)

                // Programar alarma
                scheduleReminder(hourOfDay, minute)

                Toast.makeText(
                    requireContext(),
                    "Recordatorio configurado",
                    Toast.LENGTH_SHORT
                ).show()
            },
            savedHour,
            savedMinute,
            false
        )

        timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE, "Aceptar") { dialog, _ ->
            dialog.dismiss()
        }

        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        timePickerDialog.show()
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
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
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
                    requireContext(),
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}