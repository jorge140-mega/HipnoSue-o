package com.example.hipnosprueba.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hipnosprueba.databinding.FragmentReportBinding
import com.example.hipnosprueba.ui.Adapter.ReportAdapter
import com.example.hipnosprueba.ui.databases.AppDatabase
import com.example.hipnosprueba.ui.databases.Entitys.SleepSession


class ReportFragment : Fragment() {

    private val adapter = ReportAdapter()
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadSleepData()
    }

    private fun setupRecyclerView() {
        binding.rvReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReport.adapter = adapter
    }

    private fun loadSleepData() {
        lifecycleScope.launchWhenStarted {
            AppDatabase.getInstance(requireContext())
                .sleepSessionDao()
                .getAll()
                .collect { sessions ->
                    adapter.submitList(sessions)
                    updateSummaryStats(sessions)
                }
        }
    }

    private fun updateSummaryStats(sessions: List<SleepSession>) {
        if (sessions.isEmpty()) {
            binding.tvAvgDuration.text = "0h"
            binding.tvAvgQuality.text = "0%"
            return
        }

        // Calcular promedio de horas (convertir segundos a horas)
        val totalHours = sessions.sumOf { it.durationSeconds } / 3600f
        val avgHours = totalHours / sessions.size
        binding.tvAvgDuration.text = "%.1fh".format(avgHours)

        // Calcular calidad promedio (0-100%)
        val avgQuality = sessions.map { calculateQuality(it) }.average()
        binding.tvAvgQuality.text = "%.0f%%".format(avgQuality * 100)
    }

    private fun calculateQuality(session: SleepSession): Float {
        // Normalizar duración (8 horas = 100%)
        val durationScore = (session.durationSeconds / 28800f).coerceAtMost(1f)

        // Calcular score basado en movimientos
        val movementScore = when {
            session.movements < 15 -> 1.0f  // Excelente
            session.movements < 30 -> 0.75f // Bueno
            session.movements < 50 -> 0.5f   // Regular
            else -> 0.25f                    // Malo
        }

        // Combinar scores (70% movimientos, 30% duración)
        return (movementScore * 0.7f) + (durationScore * 0.3f)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}