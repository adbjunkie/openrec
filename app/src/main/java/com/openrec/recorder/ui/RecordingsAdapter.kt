package com.openrec.recorder.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.openrec.recorder.data.RecordingFile
import com.openrec.recorder.databinding.ItemRecordingBinding

class RecordingsAdapter(
    private val onShare: (RecordingFile) -> Unit,
    private val onDelete: (RecordingFile) -> Unit,
) : ListAdapter<RecordingFile, RecordingsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemRecordingBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecordingFile) {
            binding.recordingName.text = item.name
            binding.recordingMeta.text = "${item.formattedDate} · ${item.formattedSize}"
            binding.shareButton.setOnClickListener { onShare(item) }
            binding.deleteButton.setOnClickListener { onDelete(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<RecordingFile>() {
        override fun areItemsTheSame(oldItem: RecordingFile, newItem: RecordingFile): Boolean =
            oldItem.file.absolutePath == newItem.file.absolutePath

        override fun areContentsTheSame(oldItem: RecordingFile, newItem: RecordingFile): Boolean =
            oldItem == newItem
    }
}