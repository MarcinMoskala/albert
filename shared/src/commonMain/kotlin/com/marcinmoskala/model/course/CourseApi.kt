package com.marcinmoskala.model.course

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseApi(
    val courses: List<CourseDefinitionApi>
)

@Serializable
data class CourseDefinitionApi(
    val courseId: String,
    val title: String,
    val lessons: List<LessonDefinitionApi>
)

@Serializable
data class LessonDefinitionApi(
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