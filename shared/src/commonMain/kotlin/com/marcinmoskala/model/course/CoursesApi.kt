package com.marcinmoskala.model.course

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Courses(
    val courses: List<Course>
)

@Serializable
data class Course(
    val courseId: String,
    val title: String,
    val lessons: List<Lesson>
)

@Serializable
data class Lesson(
    val lessonId: String,
    val name: String,
    val steps: List<LessonStep>
)

@Serializable
sealed class LessonStep {
    abstract val stepId: String
    abstract val repeatable: Boolean

    sealed class ChallengeLessonStep : LessonStep() {
        abstract val question: String
        abstract val explanation: String
    }

    @Serializable
    @SerialName("text")
    data class TextLessonStep(
        override val stepId: String,
        override val repeatable: Boolean,
        val text: String
    ) : LessonStep()

    @Serializable
    @SerialName("question-single-answer")
    data class SingleAnswerQuestionLessonStep(
        override val stepId: String,
        override val question: String,
        override val explanation: String,
        override val repeatable: Boolean,
        val answers: List<String>,
        val correct: String
    ) : ChallengeLessonStep()

    @Serializable
    @SerialName("question-multiple-answer")
    data class MultipleAnswerQuestionLessonStep(
        override val stepId: String,
        override val question: String,
        override val explanation: String,
        override val repeatable: Boolean,
        val answers: List<String>,
        val correct: List<String>
    ) : ChallengeLessonStep()

    @Serializable
    @SerialName("question-exact-text")
    data class ExactTextQuestionLessonStep(
        override val stepId: String,
        override val question: String,
        override val explanation: String,
        override val repeatable: Boolean,
        val correct: List<String>
    ) : ChallengeLessonStep()
}