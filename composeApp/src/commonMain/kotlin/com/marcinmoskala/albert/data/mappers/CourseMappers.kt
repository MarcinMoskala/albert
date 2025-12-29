package com.marcinmoskala.albert.data.mappers

import com.marcinmoskala.albert.domain.model.Course as DomainCourse
import com.marcinmoskala.albert.domain.model.ExactTextStep as DomainExactTextStep
import com.marcinmoskala.albert.domain.model.Lesson as DomainLesson
import com.marcinmoskala.albert.domain.model.LessonStep as DomainLessonStep
import com.marcinmoskala.albert.domain.model.MultipleAnswerStep as DomainMultipleAnswerStep
import com.marcinmoskala.albert.domain.model.SingleAnswerStep as DomainSingleAnswerStep
import com.marcinmoskala.albert.domain.model.TextStep as DomainTextStep
import com.marcinmoskala.model.course.Course as SharedCourse
import com.marcinmoskala.model.course.Lesson as SharedLesson
import com.marcinmoskala.model.course.LessonStep as SharedLessonStep

fun SharedCourse.toDomain(): DomainCourse = DomainCourse(
    courseId = courseId,
    title = title,
    lessons = lessons.map { it.toDomain() }
)

fun SharedLesson.toDomain(): DomainLesson = DomainLesson(
    lessonId = lessonId,
    name = name,
    steps = steps.map { it.toDomain() }
)

fun SharedLessonStep.toDomain(): DomainLessonStep = when (this) {
    is SharedLessonStep.SingleAnswerQuestionLessonStep -> DomainSingleAnswerStep(
        stepId = stepId,
        question = question,
        explanation = explanation,
        repeatable = repeatable,
        answers = answers,
        correct = correct
    )

    is SharedLessonStep.MultipleAnswerQuestionLessonStep -> DomainMultipleAnswerStep(
        stepId = stepId,
        question = question,
        explanation = explanation,
        repeatable = repeatable,
        answers = answers,
        correct = correct
    )

    is SharedLessonStep.ExactTextQuestionLessonStep -> DomainExactTextStep(
        stepId = stepId,
        question = question,
        explanation = explanation,
        repeatable = repeatable,
        correct = correct
    )

    is SharedLessonStep.TextLessonStep -> DomainTextStep(
        stepId = stepId,
        question = "",
        explanation = "",
        repeatable = repeatable,
        text = text
    )
}
