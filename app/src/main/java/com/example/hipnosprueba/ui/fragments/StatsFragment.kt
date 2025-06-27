package com.example.hipnosprueba.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hipnosprueba.R
import com.example.hipnosprueba.databinding.FragmentStatsBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


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

        val barEntries = listOf(
            BarEntry(1f, 30f),
            BarEntry(2f, 50f),
            BarEntry(3f, 70f)
        )
        val barDataSet = BarDataSet(barEntries, "Movimientos por noche")
        binding.barChart.data = BarData(barDataSet)

        val pieEntries = listOf(
            PieEntry(50f, "Sueño profundo"),
            PieEntry(30f, "Sueño ligero"),
            PieEntry(20f, "Despierto")
        )
        val pieDataSet = PieDataSet(pieEntries, "Fases del sueño")
        binding.pieChart.data = PieData(pieDataSet)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}