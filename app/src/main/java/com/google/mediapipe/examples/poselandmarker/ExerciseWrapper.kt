package com.google.mediapipe.examples.poselandmarker

import android.os.Parcel
import android.os.Parcelable
import com.google.mediapipe.examples.poselandmarker.model.Exercise

data class ExerciseWrapper(
    val exerciseName: String,
    val exercise: Exercise
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(Exercise::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(exerciseName)
        parcel.writeParcelable(exercise, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ExerciseWrapper> {
        override fun createFromParcel(parcel: Parcel): ExerciseWrapper {
            return ExerciseWrapper(parcel)
        }

        override fun newArray(size: Int): Array<ExerciseWrapper?> {
            return arrayOfNulls(size)
        }
    }
}
