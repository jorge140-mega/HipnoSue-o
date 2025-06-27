package com.example.hipnosprueba.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hipnosprueba.R
import com.example.hipnosprueba.databinding.FragmentReportBinding


class ReportFragment : Fragment() {
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

        val movimientos = 45
        val calidad = when {
            movimientos < 20 -> "Excelente"
            movimientos < 50 -> "Moderada"
            else -> "Mala"
        }

        binding.tvMovimientos.text = "Movimientos detectados: $movimientos"
        binding.tvCalidad.text = "Calidad del sueño: $calidad"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}