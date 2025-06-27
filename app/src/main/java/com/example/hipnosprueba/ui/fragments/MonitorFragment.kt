package com.example.hipnosprueba.ui.fragments

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.hipnosprueba.R
import com.example.hipnosprueba.databinding.FragmentMonitorBinding


class MonitorFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private var movimientos = 0
    private var isRunning = false
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                seconds++
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                binding.tvCronometro.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        binding.btnIniciar.setOnClickListener {
            if (!isRunning) {
                isRunning = true
                handler.post(runnable)
                Toast.makeText(requireContext(), "Monitoreo iniciado", Toast.LENGTH_SHORT).show()
                sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        binding.btnDetener.setOnClickListener {
            if (isRunning) {
                isRunning = false
                sensorManager.unregisterListener(this)
                handler.removeCallbacks(runnable)
                Toast.makeText(requireContext(), "Movimientos detectados: $movimientos", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isRunning) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val movimiento = Math.sqrt((x * x + y * y + z * z).toDouble())
            if (movimiento > 15) {
                movimientos++
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(runnable)
        _binding = null
    }


}