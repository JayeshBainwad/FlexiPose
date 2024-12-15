package com.google.mediapipe.examples.poselandmarker

import com.google.mediapipe.examples.poselandmarker.model.Exercise

sealed class WeeklyReportItem {
    object StaticHeader : WeeklyReportItem()
    data class DateHeader(val date: String) : WeeklyReportItem()
    data class ExerciseRow(val exerciseWrapper: ExerciseWrapper) : WeeklyReportItem()  // Hold ExerciseWrapper
}

