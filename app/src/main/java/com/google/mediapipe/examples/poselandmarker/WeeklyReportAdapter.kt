package com.google.mediapipe.examples.poselandmarker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeeklyReportAdapter(
    private val data: List<Any>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER_ROW = 0
        private const val VIEW_TYPE_DATE_HEADER = 1
        private const val VIEW_TYPE_EXERCISE_ROW = 2
    }

    // ViewHolder for the Header Row
    class HeaderRowViewHolder(view: View) : RecyclerView.ViewHolder(view)

    // ViewHolder for the Date Header
    class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDateHeader: TextView = view.findViewById(R.id.tvDateHeader)

        fun bind(date: String) {
            tvDateHeader.text = date
        }
    }

    // ViewHolder for Exercise Row
    class ExerciseRowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvExerciseName: TextView = view.findViewById(R.id.tvExerciseNameRow)
        private val tvMinAngle: TextView = view.findViewById(R.id.tvMinAngleRow)
        private val tvMaxAngle: TextView = view.findViewById(R.id.tvMaxAngleRow)
        private val tvReps: TextView = view.findViewById(R.id.tvRepsRow)
        private val tvTime: TextView = view.findViewById(R.id.tvTimeRow)

        fun bind(exercise: ExerciseData) {
            tvExerciseName.text = exercise.exerciseName
            tvMinAngle.text = exercise.minAngle.toString()
            tvMaxAngle.text = exercise.maxAngle.toString()
            tvReps.text = exercise.reps.toString()
            tvTime.text = exercise.time
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> VIEW_TYPE_HEADER_ROW // Header Row at the top
            data[position] is String -> VIEW_TYPE_DATE_HEADER // Date Header
            data[position] is ExerciseData -> VIEW_TYPE_EXERCISE_ROW // Exercise Row
            else -> throw IllegalArgumentException("Invalid data type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER_ROW -> {
                val view = inflater.inflate(R.layout.item_header_row, parent, false)
                HeaderRowViewHolder(view)
            }
            VIEW_TYPE_DATE_HEADER -> {
                val view = inflater.inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            VIEW_TYPE_EXERCISE_ROW -> {
                val view = inflater.inflate(R.layout.item_exercise_row, parent, false)
                ExerciseRowViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderRowViewHolder -> {
                // No additional binding for the static header
            }
            is DateHeaderViewHolder -> {
                val date = data[position] as String
                holder.bind(date)
            }
            is ExerciseRowViewHolder -> {
                val exercise = data[position] as ExerciseData
                holder.bind(exercise)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}