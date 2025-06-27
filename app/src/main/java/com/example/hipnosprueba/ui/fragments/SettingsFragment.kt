package com.example.hipnosprueba.ui.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hipnosprueba.databinding.FragmentSettingsBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar la hora guardada previamente
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        savedHour = prefs.getInt(HOUR_KEY, 8)     // Por defecto 8:00 AM
        savedMinute = prefs.getInt(MINUTE_KEY, 0)

        // Mostrar la hora actual
        updateReminderText(savedHour, savedMinute)

        binding.reminderButton.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                // Guardar la nueva hora seleccionada
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

                Toast.makeText(
                    requireContext(),
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}