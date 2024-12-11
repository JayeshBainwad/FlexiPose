package com.google.mediapipe.examples.poselandmarker.model

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDate
import java.time.LocalTime

data class Exercise(
    val minAngle: Int = 0,
    val maxAngle: Int = 0,
    val successfulReps: String = "",
    val date: String = LocalDate.now().toString(), // e.g., "2024-10-25"
    val time: String = LocalTime.now().toString()  // e.g., "14:30:00"
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readInt(),
        source.readString()!!,
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(minAngle)
        writeInt(maxAngle)
        writeString(successfulReps)
        writeString(date)
        writeString(time)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Exercise> = object : Parcelable.Creator<Exercise> {
            override fun createFromParcel(source: Parcel): Exercise = Exercise(source)
            override fun newArray(size: Int): Array<Exercise?> = arrayOfNulls(size)
        }
    }
}
