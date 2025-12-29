package com.marcinmoskala.albert.data.yaml

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.marcinmoskala.albert.domain.course.CourseRepository
import com.marcinmoskala.model.course.Courses
import java.io.IOException
import kotlin.use

class CourseYamlRepository(
    resourcePaths: List<String> = listOf("courses.yaml"),
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
        yaml.decodeFromString(Courses.serializer(), content).courses
    }.let { Courses(it) }

    override suspend fun getCourses(): Courses = courses
}