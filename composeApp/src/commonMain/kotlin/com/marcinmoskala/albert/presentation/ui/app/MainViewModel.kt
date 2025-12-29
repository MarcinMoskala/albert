package com.marcinmoskala.albert.presentation.ui.app

import com.marcinmoskala.albert.domain.model.Course
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.domain.repository.UserRepository
import com.marcinmoskala.albert.domain.usecase.SynchronizeProgressUseCase
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import com.marcinmoskala.albert.presentation.navigation.AppDestination
import com.marcinmoskala.albert.presentation.navigation.Navigator
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.UserProgressStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

class MainViewModel(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val userProgressRepository: UserProgressRepository,
    private val synchronizeProgressUseCase: SynchronizeProgressUseCase,
    private val navigator: Navigator,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    val uiState: StateFlow<MainUiState> = combine(
        courseRepository.courses,
        userProgressRepository.progress,
        userRepository.isLoggedIn
    ) { courses, progress, isLoggedIn ->
        MainUiState(
            loading = false,
            courses = createCoursesUi(courses, progress),
            isLoggedIn = isLoggedIn
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState()
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            courseRepository.refresh()
        }
    }

    fun onSyncClick() {
        viewModelScope.launch {
            val user = userRepository.currentUser.value
            if (user== null) {
                navigator.navigateTo(AppDestination.Login)
            } else {
                synchronizeProgressUseCase(user.userId)
            }
        }
    }

    fun onResetProgressClick() {
        navigator.navigateTo(AppDestination.ResetProgressDialog)
    }

    fun onSignOutClick() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun onCourseClick(courseId: String) {
        navigator.navigateTo(
            AppDestination.Learning(
                courseId = courseId,
                lessonId = null
            )
        )
    }

    fun onLessonClick(courseId: String, lessonId: String) {
        navigator.navigateTo(
            AppDestination.Learning(
                courseId = courseId,
                lessonId = lessonId
            )
        )
    }

    fun onReviewAllClick() {
        navigator.navigateTo(
            AppDestination.Learning(
                courseId = null,
                lessonId = null
            )
        )
    }

    private fun createCoursesUi(
        courses: List<Course>,
        progress: Map<String, UserProgressRecord>
    ): List<CourseMainUi> {
        val now: Instant = Clock.System.now()
        return courses.map { course ->
            val progressByStepId = progress.values.associateBy { it.stepId }
            CourseMainUi(
                courseId = course.courseId,
                title = course.title,
                lessons = course.lessons.map { lesson ->
                    val stepsDueToday = lesson.steps.count { step ->
                        val record = progressByStepId[step.stepId]
                        isStepDueToday(record, now)
                    }
                    LessonMainUi(
                        lessonId = lesson.lessonId,
                        name = lesson.name,
                        steps = lesson.steps.size,
                        remainingSteps = stepsDueToday
                    )
                }
            )
        }
    }

    private fun isStepDueToday(record: UserProgressRecord?, now: Instant): Boolean {
        if (record == null) return true
        return when (record.status) {
            UserProgressStatus.COMPLETED -> false
            UserProgressStatus.PENDING -> true
            UserProgressStatus.REPEATING -> {
                val reviewAt = record.reviewAt ?: return true
                reviewAt <= now
            }
        }
    }
}