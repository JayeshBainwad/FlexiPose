package com.google.mediapipe.examples.poselandmarker.model

import android.os.Parcel
import android.os.Parcelable
import com.google.mediapipe.examples.poselandmarker.utils.Constants

data class Doctor(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val mobile: Long = 0,
    val age: Int = 0,
    val userType: String = Constants.DOCTORUSERS,
    val fcmToken: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readLong(),
        source.readInt(),
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(image)
        writeLong(mobile)
        writeInt(age)
        writeString(userType)
        writeString(fcmToken)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Doctor> = object : Parcelable.Creator<Doctor> {
            override fun createFromParcel(source: Parcel): Doctor = Doctor(source)
            override fun newArray(size: Int): Array<Doctor?> = arrayOfNulls(size)
        }
    }
}