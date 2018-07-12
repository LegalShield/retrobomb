package com.legalshield.retrobomb

import retrofit2.http.*
import com.google.re2j.Pattern
import kotlin.reflect.KClass

class Retrobomb<Repository>(private val repositoryType: Class<Repository>) {
    private val mappings: MutableMap<RouteStatusKey, KClass<*>> = HashMap()

    internal fun generateMapping(): Map<RouteStatusKey, KClass<*>> {
        repositoryType.declaredMethods.forEach { declaredMethod ->
            if (declaredMethod.isAnnotationPresent(RetrobombMappings::class.java)) {
                val errorClassMappings = declaredMethod.getAnnotation(RetrobombMappings::class.java).mappings
                declaredMethod.annotations.forEach {
                    when (it) {
                        is GET -> mergeIntoMappings(it.value, HttpMethod.GET, errorClassMappings)
                        is POST -> mergeIntoMappings(it.value, HttpMethod.POST, errorClassMappings)
                        is PATCH -> mergeIntoMappings(it.value, HttpMethod.PATCH, errorClassMappings)
                        is PUT -> mergeIntoMappings(it.value, HttpMethod.PUT, errorClassMappings)
                        is DELETE -> mergeIntoMappings(it.value, HttpMethod.DELETE, errorClassMappings)
                        is HEAD -> mergeIntoMappings(it.value, HttpMethod.HEAD, errorClassMappings)
                        is OPTIONS -> mergeIntoMappings(it.value, HttpMethod.OPTIONS, errorClassMappings)
                    }
                }
            }
        }
        return mappings
    }

    private fun createFromRetrofitPath(path: String): Pattern {
        val bodyPattern = pathVariablePattern.matcher(path).replaceAll("""[^/]+""")
        return Pattern.compile(""".*?$bodyPattern((\?.*)|/)?""")
    }

    private fun mergeIntoMappings(rawPath: String, httpMethod: HttpMethod, errorClassMappings: Array<out ErrorMapping>) {
        errorClassMappings.map {
            mappings[RouteStatusKey(createFromRetrofitPath(rawPath), httpMethod, it.code)] = it.errorType
        }
    }

    enum class HttpMethod {
        GET, POST, PATCH, PUT, DELETE, HEAD, OPTIONS
    }

    companion object {
        private val pathVariablePattern = Pattern.compile("""\{.+?}""")
    }
}
