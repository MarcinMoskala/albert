package com.marcinmoskala.albert.domain.model

data class Course(
    val courseId: String,
    val title: String,
    val lessons: List<Lesson>
)

data class Lesson(
    val lessonId: String,
    val name: String,
    val steps: List<LessonStep>
)

sealed class LessonStep {
    abstract val stepId: String
    abstract val question: String
    abstract val explanation: String
    abstract val repeatable: Boolean
}

data class SingleAnswerStep(
    override val stepId: String,
    override val question: String,
    override val explanation: String,
    override val repeatable: Boolean,
    val answers: List<String>,
    val correct: String
) : LessonStep()

data class MultipleAnswerStep(
    override val stepId: String,
    override val question: String,
    override val explanation: String,
    override val repeatable: Boolean,
    val answers: List<String>,
    val correct: List<String>
) : LessonStep()

data class ExactTextStep(
    override val stepId: String,
    override val question: String,
    override val explanation: String,
    override val repeatable: Boolean,
    val correct: List<String>
) : LessonStep()

data class TextStep(
    override val stepId: String,
    override val question: String,
    override val explanation: String,
    override val repeatable: Boolean,
    val text: String
) : LessonStep()
