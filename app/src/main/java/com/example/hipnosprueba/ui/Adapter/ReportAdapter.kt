package com.example.hipnosprueba.ui.Adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hipnosprueba.databinding.ItemSessionBinding
import com.example.hipnosprueba.ui.databases.Entitys.SleepSession
import java.text.DateFormat
import java.util.Date

class ReportAdapter: ListAdapter<SleepSession, ReportAdapter.ViewHolder>(DIFF) {
    companion object {
        val DIFF = object: DiffUtil.ItemCallback<SleepSession>() {
            override fun areItemsTheSame(a: SleepSession, b: SleepSession) = a.id == b.id
            override fun areContentsTheSame(a: SleepSession, b: SleepSession) = a == b
        }
    }
    class ViewHolder(val binding: ItemSessionBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val s = getItem(position)
            tvTimestamp.text = DateFormat.getDateTimeInstance().format(Date(s.timestamp))
            tvDuration.text = String.format("%02d:%02d:%02d",
                s.durationSeconds/3600, (s.durationSeconds%3600)/60, s.durationSeconds%60)
            tvMovements.text = s.movements.toString()
        }
    }
}