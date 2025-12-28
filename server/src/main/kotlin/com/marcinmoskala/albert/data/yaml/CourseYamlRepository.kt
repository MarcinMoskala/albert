package com.marcinmoskala.albert.data.yaml

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.marcinmoskala.albert.domain.course.CourseRepository
import com.marcinmoskala.model.course.CourseApi
import com.marcinmoskala.model.course.CoursesApi
import com.marcinmoskala.model.course.LessonApi
import com.marcinmoskala.model.course.LessonStepApi
import com.marcinmoskala.model.course.TextStepApi
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors
import kotlin.use

class CourseYamlRepository(
    private val resourcePaths: List<String> = listOf("courses.yaml", "course-kotlin-essentials.yaml"),
    private val yaml: Yaml = Yaml(
        configuration = YamlConfiguration(
            polymorphismStyle = PolymorphismStyle.Property,
            encodeDefaults = false
        )
    )
) : CourseRepository {

    val courses = resourcePaths.flatMap { resourcePath ->
        val resource = javaClass.classLoader.getResource(resourcePath)
            ?: throw IllegalStateException("Course YAML file not found at path: $resourcePath")
        val content = runCatching { resource.readText() }
            .getOrElse { throw IOException("Unable to read YAML file at $resourcePath", it) }
        yaml.decodeFromString(CoursesApi.serializer(), content).courses
    }.let { CoursesApi(it) }

    override suspend fun getCourses(): CoursesApi = courses
}