package com.marcinmoskala.model.course

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoursesApi(
    val courses: List<CourseApi>
)

@Serializable
data class CourseApi(
    val courseId: String,
    val title: String,
    val lessons: List<LessonApi>
)

@Serializable
data class LessonApi(
    val lessonId: String,
    val name: String,
    val steps: List<LessonStepApi>
)

@Serializable
sealed class LessonStepApi {
    abstract val stepId: String
    abstract val question: String
    abstract val explanation: String

    @SerialName("repetable")
    abstract val repeatable: Boolean
}

@Serializable
@SerialName("single-answer")
data class SingleAnswerStepApi(
    override val stepId: String,
    override val question: String,
    override val explanation: String,
    @SerialName("repetable") override val repeatable: Boolean,
    val answers: List<String>,
    val correct: String
) : LessonStepApi()

@Serializable
@SerialName("multiple-answer")
data class MultipleAnswerStepApi(
    override val stepId: String,
    override val question: String,
    override val explanation: String,
    @SerialName("repetable") override val repeatable: Boolean,
    val answers: List<String>,
    val correct: List<String>
) : LessonStepApi()

@Serializable
@SerialName("exact-text")
data class ExactTextStepApi(
    override val stepId: String,
    override val question: String,
    override val explanation: String,
    @SerialName("repetable") override val repeatable: Boolean,
    val correct: List<String>
) : LessonStepApi()

@Serializable
@SerialName("text")
data class TextStepApi(
    override val stepId: String,
    override val question: String,
    override val explanation: String,
    @SerialName("repetable") override val repeatable: Boolean,
    val text: String
) : LessonStepApi()