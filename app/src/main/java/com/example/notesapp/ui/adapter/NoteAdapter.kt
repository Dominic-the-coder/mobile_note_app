package com.example.notesapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.data.model.Note
import com.example.notesapp.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var notes = emptyList<Note>()

    fun submitList(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.noteTitle.text = note.title
            binding.noteContent.text = note.content
            binding.noteCard.setCardBackgroundColor(note.color.toInt())

            binding.noteDate.text = formatNoteDate(note.updatedAt)
        }

        private fun formatNoteDate(timestamp: Long): String {
            val noteCalendar = Calendar.getInstance().apply {
                timeInMillis = timestamp
            }

            val todayCalendar = Calendar.getInstance()

            if (isSameDay(noteCalendar, todayCalendar)) {
                return "Today"
            }

            val yesterdayCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }

            if (isSameDay(noteCalendar, yesterdayCalendar)) {
                return "Yesterday"
            }

            return SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(Date(timestamp))
        }

        private fun isSameDay(first: Calendar, second: Calendar): Boolean {
            return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
                    first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)

        holder.itemView.setOnClickListener {
            onNoteClick(note)
        }
    }

    override fun getItemCount(): Int = notes.size
}