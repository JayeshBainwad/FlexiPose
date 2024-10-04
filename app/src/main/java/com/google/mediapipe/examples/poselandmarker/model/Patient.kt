package com.google.mediapipe.examples.poselandmarker.model

import android.os.Parcel
import android.os.Parcelable

data class Patient(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val mobile: Long = 0,
    val fcmToken: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readLong(),
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(image)
        writeLong(mobile)
        writeString(fcmToken)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Patient> = object : Parcelable.Creator<Patient> {
            override fun createFromParcel(source: Parcel): Patient = Patient(source)
            override fun newArray(size: Int): Array<Patient?> = arrayOfNulls(size)
        }
    }
}