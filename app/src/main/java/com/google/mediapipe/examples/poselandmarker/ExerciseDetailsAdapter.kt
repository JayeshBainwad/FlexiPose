package com.google.mediapipe.examples.poselandmarker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.poselandmarker.model.Exercise

class ExerciseDetailsAdapter(
    private var exerciseListWithNames: List<Pair<String, Exercise>> // Pair of document name and exercise data
) : RecyclerView.Adapter<ExerciseDetailsAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseTitle: TextView = itemView.findViewById(R.id.tvExerciseTitle)
        val minAngles: TextView = itemView.findViewById(R.id.tvMinAngles)
        val maxAngles: TextView = itemView.findViewById(R.id.tvMaxAngles)
        val successfulReps: TextView = itemView.findViewById(R.id.tvSuccessfulReps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exercise_details_card, parent, false)
        return ExerciseViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val (documentName, exercise) = exerciseListWithNames[position]

        // The document name contains both the exercise name and number (e.g., ElbowExercise1)
        holder.exerciseTitle.text = documentName
        holder.minAngles.text = "Min Angle: ${exercise.minAngle}°"
        holder.maxAngles.text = "Max Angle: ${exercise.maxAngle}°"
        holder.successfulReps.text = "Successful Reps: ${exercise.successfulReps}"
    }

    override fun getItemCount(): Int = exerciseListWithNames.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<Pair<String, Exercise>>) {
        exerciseListWithNames = newList
        notifyDataSetChanged()
    }
}
