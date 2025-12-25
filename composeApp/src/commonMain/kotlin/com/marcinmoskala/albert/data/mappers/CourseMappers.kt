package com.marcinmoskala.albert.data.mappers

import com.marcinmoskala.albert.domain.model.*
import com.marcinmoskala.model.course.*

fun CourseApi.toDomain(): Course = Course(
    courseId = courseId,
    title = title,
    lessons = lessons.map { it.toDomain() }
)

fun LessonApi.toDomain(): Lesson = Lesson(
    lessonId = lessonId,
    name = name,
    steps = steps.map { it.toDomain() }
)

fun LessonStepApi.toDomain(): LessonStep = when (this) {
    is SingleAnswerStepApi -> SingleAnswerStep(
        stepId = stepId,
        question = question,
        explanation = explanation,
        repeatable = repeatable,
        answers = answers,
        correct = correct
    )

    is MultipleAnswerStepApi -> MultipleAnswerStep(
        stepId = stepId,
        question = question,
        explanation = explanation,
        repeatable = repeatable,
        answers = answers,
        correct = correct
    )

    is ExactTextStepApi -> ExactTextStep(
        stepId = stepId,
        question = question,
        explanation = explanation,
        repeatable = repeatable,
        correct = correct
    )

    is TextStepApi -> TextStep(
        stepId = stepId,
        question = question,
        explanation = explanation,
        repeatable = repeatable,
        text = text
    )
}
