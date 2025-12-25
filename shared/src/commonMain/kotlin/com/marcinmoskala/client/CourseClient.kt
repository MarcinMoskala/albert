package com.marcinmoskala.client

import com.marcinmoskala.model.course.CoursesApi
import com.marcinmoskala.model.course.CourseApi
import com.marcinmoskala.model.course.LessonApi
import com.marcinmoskala.model.course.SingleAnswerStepApi
import com.marcinmoskala.model.course.MultipleAnswerStepApi
import com.marcinmoskala.model.course.ExactTextStepApi
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class CourseClient(
    private val httpClient: HttpClient
) {
    //    suspend fun fetchCourses(): CourseApi = httpClient.get("/course").body()
    suspend fun fetchCourses(): CoursesApi = CoursesApi(
        courses = listOf(
            CourseApi(
                courseId = "kotlin-basics",
                title = "Kotlin Fundamentals",
                lessons = listOf(
                    LessonApi(
                        lessonId = "lesson-1",
                        name = "Variables and Data Types",
                        steps = listOf(
                            SingleAnswerStepApi(
                                stepId = "step-1-1",
                                question = "Which keyword is used to declare an immutable variable in Kotlin?",
                                explanation = "In Kotlin, 'val' is used for immutable (read-only) variables, while 'var' is used for mutable variables.",
                                repeatable = true,
                                answers = listOf("var", "val", "const", "let"),
                                correct = "val"
                            ),
                            MultipleAnswerStepApi(
                                stepId = "step-1-2",
                                question = "Which of the following are primitive types in Kotlin?",
                                explanation = "Kotlin has several basic types: Int, Long, Double, Float, Boolean, Char, Byte, and Short.",
                                repeatable = false,
                                answers = listOf("Int", "String", "Double", "Boolean", "Array"),
                                correct = listOf("Int", "Double", "Boolean")
                            ),
                            ExactTextStepApi(
                                stepId = "step-1-3",
                                question = "Complete the code: val name: String = ___ (assign the value \"Kotlin\")",
                                explanation = "String literals in Kotlin are enclosed in double quotes.",
                                repeatable = true,
                                correct = listOf("\"Kotlin\"", "\"kotlin\"")
                            )
                        )
                    ),
                    LessonApi(
                        lessonId = "lesson-2",
                        name = "Functions and Control Flow",
                        steps = listOf(
                            SingleAnswerStepApi(
                                stepId = "step-2-1",
                                question = "What is the correct syntax to declare a function in Kotlin?",
                                explanation = "Functions in Kotlin are declared using the 'fun' keyword followed by the function name.",
                                repeatable = false,
                                answers = listOf(
                                    "function myFunc() {}",
                                    "fun myFunc() {}",
                                    "def myFunc() {}",
                                    "func myFunc() {}"
                                ),
                                correct = "fun myFunc() {}"
                            ),
                            MultipleAnswerStepApi(
                                stepId = "step-2-2",
                                question = "Which of these are valid control flow statements in Kotlin?",
                                explanation = "Kotlin supports if, when, for, and while for control flow. 'switch' is called 'when' in Kotlin.",
                                repeatable = true,
                                answers = listOf("if", "when", "switch", "for", "while", "foreach"),
                                correct = listOf("if", "when", "for", "while")
                            ),
                            ExactTextStepApi(
                                stepId = "step-2-3",
                                question = "What keyword is used for single-expression functions? (fun add(a: Int, b: Int) ___ a + b)",
                                explanation = "The '=' operator is used to declare single-expression functions in Kotlin.",
                                repeatable = false,
                                correct = listOf("=")
                            )
                        )
                    )
                )
            ),
            CourseApi(
                courseId = "kotlin-advanced",
                title = "Advanced Kotlin",
                lessons = listOf(
                    LessonApi(
                        lessonId = "lesson-3",
                        name = "Coroutines and Concurrency",
                        steps = listOf(
                            SingleAnswerStepApi(
                                stepId = "step-3-1",
                                question = "Which keyword is used to call a suspending function?",
                                explanation = "The 'suspend' modifier marks a function as suspending, and it must be called with 'await' or from another suspend function.",
                                repeatable = true,
                                answers = listOf("async", "await", "suspend", "coroutine"),
                                correct = "suspend"
                            ),
                            MultipleAnswerStepApi(
                                stepId = "step-3-2",
                                question = "Which are valid coroutine builders in Kotlin?",
                                explanation = "Common coroutine builders include launch (fire-and-forget), async (returns Deferred), and runBlocking (blocks the thread).",
                                repeatable = false,
                                answers = listOf(
                                    "launch",
                                    "async",
                                    "runBlocking",
                                    "start",
                                    "execute"
                                ),
                                correct = listOf("launch", "async", "runBlocking")
                            ),
                            ExactTextStepApi(
                                stepId = "step-3-3",
                                question = "What dispatcher should you use for CPU-intensive operations? (Dispatchers.___)",
                                explanation = "Dispatchers.Default is optimized for CPU-intensive work, while Dispatchers.IO is for I/O operations.",
                                repeatable = true,
                                correct = listOf("Default", "default")
                            )
                        )
                    ),
                    LessonApi(
                        lessonId = "lesson-4",
                        name = "Null Safety and Smart Casts",
                        steps = listOf(
                            SingleAnswerStepApi(
                                stepId = "step-4-1",
                                question = "Which operator provides a default value when a nullable variable is null?",
                                explanation = "The Elvis operator (?:) returns the value on the left if not null, otherwise returns the value on the right.",
                                repeatable = false,
                                answers = listOf("?.", "!!", "?:", "?.?"),
                                correct = "?:"
                            ),
                            MultipleAnswerStepApi(
                                stepId = "step-4-2",
                                question = "Which operators are related to null safety in Kotlin?",
                                explanation = "Kotlin provides several null-safety operators: safe call (?.), Elvis (?:), and not-null assertion (!!).",
                                repeatable = true,
                                answers = listOf("?.", "!!", "?:", "??", "?"),
                                correct = listOf("?.", "!!", "?:")
                            ),
                            ExactTextStepApi(
                                stepId = "step-4-3",
                                question = "What symbol makes a type nullable in Kotlin? (e.g., String___)",
                                explanation = "Adding a question mark (?) after a type makes it nullable in Kotlin's type system.",
                                repeatable = false,
                                correct = listOf("?")
                            )
                        )
                    )
                )
            ),
            CourseApi(
                courseId = "kotlin-collections",
                title = "Kotlin Collections & Functional Programming",
                lessons = listOf(
                    LessonApi(
                        lessonId = "lesson-5",
                        name = "Collections and Higher-Order Functions",
                        steps = listOf(
                            SingleAnswerStepApi(
                                stepId = "step-5-1",
                                question = "Which function transforms each element of a collection?",
                                explanation = "The 'map' function applies a transformation to each element and returns a new collection.",
                                repeatable = true,
                                answers = listOf("map", "filter", "forEach", "transform"),
                                correct = "map"
                            ),
                            MultipleAnswerStepApi(
                                stepId = "step-5-2",
                                question = "Which are terminal operations on Kotlin collections?",
                                explanation = "Terminal operations trigger the computation and return a result: toList(), count(), first(), etc.",
                                repeatable = false,
                                answers = listOf(
                                    "toList()",
                                    "map()",
                                    "count()",
                                    "filter()",
                                    "first()"
                                ),
                                correct = listOf("toList()", "count()", "first()")
                            ),
                            ExactTextStepApi(
                                stepId = "step-5-3",
                                question = "What collection type should you use for read-only lists? (___<T>)",
                                explanation = "List<T> is the read-only interface, while MutableList<T> allows modifications.",
                                repeatable = true,
                                correct = listOf("List", "list")
                            )
                        )
                    )
                )
            )
        )
    )
}