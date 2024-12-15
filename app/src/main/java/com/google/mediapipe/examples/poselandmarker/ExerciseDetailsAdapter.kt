package com.google.mediapipe.examples.poselandmarker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.poselandmarker.model.Exercise
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExerciseDetailsAdapter(
    private var exerciseListWithNames: MutableList<Pair<String, Exercise>> // Pair of document name and exercise data
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DATE_VIEW_TYPE = 0
    private val EXERCISE_VIEW_TYPE = 1

    private var groupedItems: List<Any> = listOf()

    init {
        groupAndSortItems()
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.tvDateHeader)
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseTitle: TextView = itemView.findViewById(R.id.tvExerciseTitle)
        val minAngles: TextView = itemView.findViewById(R.id.tvMinAngles)
        val maxAngles: TextView = itemView.findViewById(R.id.tvMaxAngles)
        val successfulReps: TextView = itemView.findViewById(R.id.tvSuccessfulReps)
        val timeText: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun getItemViewType(position: Int): Int {
        return if (groupedItems[position] is String) DATE_VIEW_TYPE else EXERCISE_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == DATE_VIEW_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.exercise_details_card, parent, false)
            ExerciseViewHolder(view)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateViewHolder) {
            val date = groupedItems[position] as String
            holder.dateText.text = date
        } else if (holder is ExerciseViewHolder) {
            val (documentName, exercise) = groupedItems[position] as Pair<String, Exercise>

            holder.exerciseTitle.text = documentName
            holder.minAngles.text = "Min Angle: ${exercise.minAngle}°"
            holder.maxAngles.text = "Max Angle: ${exercise.maxAngle}°"
            holder.successfulReps.text = "Successful Reps: ${exercise.successfulReps}"
            holder.timeText.text = "Time: ${exercise.time}"
        }
    }

    override fun getItemCount(): Int = groupedItems.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<Pair<String, Exercise>>) {
        exerciseListWithNames = newList
        groupAndSortItems()
        notifyDataSetChanged()
    }

    private fun groupAndSortItems() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val groupedMap = exerciseListWithNames.groupBy { it.second.date }
            .toSortedMap { date1, date2 ->
                LocalDate.parse(date1, formatter).compareTo(LocalDate.parse(date2, formatter))
            }

        val newGroupedItems = mutableListOf<Any>()
        groupedMap.forEach { (date, exercises) ->
            newGroupedItems.add(date) // Add date as a header
            newGroupedItems.addAll(exercises) // Add exercises for this date
        }

        groupedItems = newGroupedItems
    }
}
