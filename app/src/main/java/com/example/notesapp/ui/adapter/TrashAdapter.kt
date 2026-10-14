package com.example.notesapp.ui.trash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.data.model.Note
import com.example.notesapp.databinding.ItemTrashNoteBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TrashAdapter(
    private val onRestoreClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<TrashAdapter.TrashViewHolder>() {

    private var notes = emptyList<Note>()

    fun submitList(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    inner class TrashViewHolder(
        private val binding: ItemTrashNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.trashNoteTitle.text =
                note.title.ifBlank { "Untitled note" }

            binding.trashNoteContent.text = note.content

            // Use the note's saved color.
            binding.trashNoteCard.setBackgroundColor(
                note.color.toInt()
            )

            val deletedTimestamp = note.deletedAt ?: note.updatedAt
            val deletedDate = formatDeletedDate(deletedTimestamp)

            binding.trashDeletedDate.text = "Deleted on $deletedDate"

            binding.restoreNoteButton.setOnClickListener {
                onRestoreClick(note)
            }

            binding.deleteForeverButton.setOnClickListener {
                onDeleteClick(note)
            }
        }

        private fun formatDeletedDate(timestamp: Long): String {
            val deletedCalendar = Calendar.getInstance().apply {
                timeInMillis = timestamp
            }

            val todayCalendar = Calendar.getInstance()

            if (isSameDay(deletedCalendar, todayCalendar)) {
                return "Today"
            }

            val yesterdayCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }

            if (isSameDay(deletedCalendar, yesterdayCalendar)) {
                return "Yesterday"
            }

            return SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(Date(timestamp))
        }

        private fun isSameDay(
            first: Calendar,
            second: Calendar
        ): Boolean {
            return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
                    first.get(Calendar.DAY_OF_YEAR) ==
                    second.get(Calendar.DAY_OF_YEAR)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrashViewHolder {
        val binding = ItemTrashNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TrashViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TrashViewHolder,
        position: Int
    ) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size
}