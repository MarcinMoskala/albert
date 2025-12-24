package com.marcinmoskala.albert

import com.marcinmoskala.albert.data.yaml.CourseYamlRepository
import com.marcinmoskala.albert.domain.course.CourseRepository
import com.marcinmoskala.albert.domain.course.CourseService
import com.marcinmoskala.albert.di.serverModule
import com.marcinmoskala.model.course.CourseApi
import com.marcinmoskala.model.course.ExactTextStepApi
import com.marcinmoskala.model.course.MultipleAnswerStepApi
import com.marcinmoskala.model.course.SingleAnswerStepApi
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CourseEndpointTest {

    @Test
    fun `returns courses from production yaml`() = testApplication {
        application {
            module()
        }
        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.get("/course")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<CourseApi>()
        assertTrue(body.courses.isNotEmpty(), "Expected at least one course")
        val firstCourse = body.courses.first()
        assertTrue(firstCourse.lessons.isNotEmpty(), "Expected at least one lesson")
        assertTrue(firstCourse.lessons.first().steps.isNotEmpty(), "Expected at least one step")
    }

    @Test
    fun `returns expected courses from test yaml`() = testApplication {
        application {
            module(
                listOf(
                    serverModule,
                    testModule()
                )
            )
        }
        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.get("/course")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<CourseApi>()
        val course = body.courses.single()
        assertEquals("test-course", course.courseId)
        assertEquals("Test Course", course.title)

        val lesson = course.lessons.single()
        assertEquals("lesson-1", lesson.lessonId)
        assertEquals("Lesson One", lesson.name)

        val steps = lesson.steps
        assertEquals(3, steps.size)

        val single = assertIs<SingleAnswerStepApi>(steps[0])
        assertEquals("single-step", single.stepId)
        assertEquals(listOf("Option A", "Option B", "Option C"), single.answers)
        assertEquals("Option B", single.correct)
        assertTrue(single.repeatable)

        val multiple = assertIs<MultipleAnswerStepApi>(steps[1])
        assertEquals(listOf("First", "Second", "Third"), multiple.answers)
        assertEquals(listOf("First", "Third"), multiple.correct)
        assertEquals(false, multiple.repeatable)

        val exact = assertIs<ExactTextStepApi>(steps[2])
        assertEquals(listOf("keyword", "KEYWORD"), exact.correct)
        assertTrue(exact.repeatable)
    }

    private fun testModule() = module(override = true) {
        single<CourseRepository> { CourseYamlRepository(resourcePath = "test-courses.yaml") }
        single { CourseService(get()) }
    }
}