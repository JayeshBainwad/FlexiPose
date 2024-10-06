package com.google.mediapipe.examples.poselandmarker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.poselandmarker.model.Patient

class PatientListAdapter(private val patientList: List<Patient>) : RecyclerView.Adapter<PatientListAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val patientName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val patientImage: ImageView = itemView.findViewById(R.id.iv_patient_profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_patient_card, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patientList[position]
        holder.patientName.text = patient.name
        // You can load the image using Glide or Picasso if needed
        // Glide.with(holder.itemView.context).load(patient.imageUrl).into(holder.patientImage)
    }

    override fun getItemCount(): Int = patientList.size
}
