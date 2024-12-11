package com.google.mediapipe.examples.poselandmarker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mediapipe.examples.poselandmarker.model.Patient

class SavedPatientListAdapter(
    private var patientList: MutableList<Patient> = ArrayList(),
    private val onPatientClick: (Patient) -> Unit, // Lambda function for handling click events
    private val onAddPatientClick: (Patient) -> Unit // Lambda function for handling add button click
) : RecyclerView.Adapter<SavedPatientListAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val patientName: TextView = itemView.findViewById(R.id.tv_saved_patient_name)
        val patientImage: ImageView = itemView.findViewById(R.id.iv_saved_patient_profile_image)
        val removeButton: ImageButton = itemView.findViewById(R.id.btn_add_saved_patient) // Reference to the add button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_patient_card, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patientList[position]
        holder.patientName.text = patient.name
        Glide.with(holder.itemView.context)
            .load(patient.image)
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.patientImage)

        // Set the click listener to navigate to the exercise details activity
        holder.itemView.setOnClickListener {
            onPatientClick(patient)
        }

        // Set the click listener for the add button
        holder.removeButton.setOnClickListener {
            onAddPatientClick(patient) // Call the function when the button is clicked
        }
    }

    override fun getItemCount(): Int = patientList.size

    // Add this method to update the patient list based on the search results
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<Patient>) {
        patientList = newList
        notifyDataSetChanged()
    }

    // Inside PatientListAdapter
    fun removePatient(patient: Patient) {
        patientList.remove(patient)
        notifyDataSetChanged() // This will refresh the RecyclerView to reflect the change
    }

}
