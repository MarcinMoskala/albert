package com.marcinmoskala.albert.data.yaml

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.marcinmoskala.albert.domain.course.CourseRepository
import com.marcinmoskala.model.course.CoursesApi
import java.io.IOException

class CourseYamlRepository(
    private val resourcePath: String = "courses.yaml",
    private val yaml: Yaml = Yaml(
        configuration = YamlConfiguration(
            polymorphismStyle = PolymorphismStyle.Property,
            encodeDefaults = false
        )
    )
) : CourseRepository {

    override suspend fun getCourses(): CoursesApi {
        val resource = javaClass.classLoader.getResource(resourcePath)
            ?: throw IllegalStateException("Course YAML file not found at path: $resourcePath")
        val content = runCatching { resource.readText() }
            .getOrElse { throw IOException("Unable to read YAML file at $resourcePath", it) }
        return yaml.decodeFromString(CoursesApi.serializer(), content)
    }
}