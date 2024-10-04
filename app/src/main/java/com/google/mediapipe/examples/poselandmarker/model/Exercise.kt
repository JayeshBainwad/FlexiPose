package com.google.mediapipe.examples.poselandmarker.model

import android.os.Parcel
import android.os.Parcelable

data class Exercise(
    val minAngle: Double = 0.0,
    val maxAngle: Double = 0.0,
    val successfulReps: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readDouble(),
        source.readDouble(),
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeDouble(minAngle)
        writeDouble(maxAngle)
        writeString(successfulReps)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Exercise> = object : Parcelable.Creator<Exercise> {
            override fun createFromParcel(source: Parcel): Exercise = Exercise(source)
            override fun newArray(size: Int): Array<Exercise?> = arrayOfNulls(size)
        }
    }
}