package com.example.hipnosprueba.ui

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hipnosprueba.R
import com.example.hipnosprueba.databinding.ActivitySettingsBinding
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val PREFS_NAME = "recordatorio_prefs"
    private val HOUR_KEY = "hora"
    private val MINUTE_KEY = "minuto"
    private var savedHour = 8
    private var savedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Cargar la hora guardada previamente
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        savedHour = prefs.getInt(HOUR_KEY, 8)     // Por defecto 8:00 AM
        savedMinute = prefs.getInt(MINUTE_KEY, 0)

        // Mostrar la hora actual
        updateReminderText(savedHour, savedMinute)

        binding.reminderButton.setOnClickListener {
            showTimePickerDialog()
        }
        // Barra de navegación inferior
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_back -> {
                    finish()  // o onBackPressedDispatcher.onBackPressed()
                    true
                }
                else -> false
            }
        }

    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
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

                Toast.makeText(
                    this,
                    "Recordatorio configurado",
                    Toast.LENGTH_SHORT
                ).show()
            },
            savedHour,
            savedMinute,
            false // Formato 12 horas (true) o 24 horas (false)
        )

        // Personalizar los botones
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
}