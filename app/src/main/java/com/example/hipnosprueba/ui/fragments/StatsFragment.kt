package com.example.hipnosprueba.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.hipnosprueba.R
import com.example.hipnosprueba.databinding.FragmentStatsBinding
import com.example.hipnosprueba.ui.databases.AppDatabase
import com.example.hipnosprueba.ui.databases.Entitys.SleepSession
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt



class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch  {
            loadSleepData()
        }
    }

    private suspend fun loadSleepData() {
        // Obtener datos de la base de datos
        val sleepSessions = AppDatabase.getInstance(requireContext())
            .sleepSessionDao()
            .getAll()
            .first()

        if (sleepSessions.isNotEmpty()) {
            // Procesar datos para los gráficos
            processBarChartData(sleepSessions)
            processPieChartData(sleepSessions)
        } else {
            // Mostrar datos de ejemplo si no hay registros
            showSampleData()
        }
    }
    private fun processBarChartData(sessions: List<SleepSession>) {
        val lasSession = sessions.sortedByDescending { it.timestamp } .take(7).reversed()
        val barEntries = lasSession.mapIndexed { index, session ->
            BarEntry(
                index.toFloat(),
                session.movements.toFloat(),
                formatDate(session.timestamp)
            )
        }

        val barDataSet = BarDataSet(barEntries, "Movimientos por noche").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = android.R.color.black
            valueTextSize = 10f
        }
        binding.barChart.apply {
            data = BarData(barDataSet)
            description.text = "Últimas 7 sesiones"
            animateY(1000)
            invalidate()
        }
    }

    private fun processPieChartData(sessions: List<SleepSession>) {
        // Como no tenemos datos de fases de sueño, haremos una estimación basada en movimientos
        val totalDuration = sessions.sumOf { it.durationSeconds }
        if (totalDuration == 0) return

        // Estimación: menos movimientos = más sueño profundo
        val avgMovements = sessions.map { it.movements }.average()

        val pieEntries = sessions.map { session ->
            val sleepQuality = when {
                session.movements < avgMovements * 0.5 -> "Profundo"
                session.movements < avgMovements * 1.5 -> "Ligero"
                else -> "Inquieto"
            }
            PieEntry(
                session.durationSeconds.toFloat(),
                sleepQuality
            )
        }.groupBy { it.label }
            .map { (label, entries) ->
                PieEntry(entries.sumOf { it.value.toDouble() }.toFloat(), label)
            }

        val pieDataSet = PieDataSet(pieEntries, "Tiempo por calidad de sueño").apply {
            colors = listOf(
                resources.getColor(R.color.deep_sleep),
                resources.getColor(R.color.light_sleep),
                resources.getColor(R.color.awake)
            )
            valueTextColor = android.R.color.white
            valueTextSize = 12f
        }

        binding.pieChart.apply {
            data = PieData(pieDataSet)
            description.text = "Distribución estimada"
            setUsePercentValues(true)
            animateY(1000)
            invalidate()
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    private fun formatShortDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }

    private fun showSampleData() {
        // Datos de ejemplo cuando no hay registros
        val sampleEntries = listOf(
            BarEntry(0f, 30f, "Ejemplo 1"),
            BarEntry(1f, 50f, "Ejemplo 2"),
            BarEntry(2f, 70f, "Ejemplo 3")
        )

        val barDataSet = BarDataSet(sampleEntries, "Movimientos (ejemplo)")
        binding.barChart.data = BarData(barDataSet)

        val pieEntries = listOf(
            PieEntry(50f, "Profundo"),
            PieEntry(30f, "Ligero"),
            PieEntry(20f, "Inquieto")
        )

        val pieDataSet = PieDataSet(pieEntries, "Calidad (ejemplo)")
        binding.pieChart.data = PieData(pieDataSet)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
