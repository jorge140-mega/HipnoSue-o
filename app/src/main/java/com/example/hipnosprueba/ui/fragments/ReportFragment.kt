package com.example.hipnosprueba.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hipnosprueba.R
import com.example.hipnosprueba.databinding.FragmentReportBinding
import com.example.hipnosprueba.ui.Adapter.ReportAdapter
import com.example.hipnosprueba.ui.databases.AppDatabase


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

        binding.rvReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReport.adapter = adapter

        lifecycleScope.launchWhenStarted {
            AppDatabase.getInstance(requireContext())
                .sleepSessionDao()
                .getAll()
                .collect{ sessions ->
                    adapter.submitList(sessions)
                }
        }
    }
}