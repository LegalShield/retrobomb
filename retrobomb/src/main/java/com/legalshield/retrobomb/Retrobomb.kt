package com.legalshield.retrobomb

import retrofit2.http.*
import com.google.re2j.Pattern
import java.lang.reflect.Method
import kotlin.reflect.KClass

class Retrobomb<Repository>(private val repositoryType: Class<Repository>) {
    private val mappings: MutableMap<RouteStatusKey, KClass<*>> = HashMap()

    internal fun generateMapping(): Map<RouteStatusKey, KClass<*>> {
        repositoryType.declaredMethods.forEach { declaredMethod ->
            if (declaredMethod.isAnnotationPresent(RetrobombMappings::class.java)) {
                val queryParamNames = declaredMethod.parameterAnnotations
                    .flatMap { annotations ->
                        annotations.map { annotation ->
                            annotation.takeIf { it is Query } as Query?
                        }.filterNotNull()
                    }
                    .map { it.value }
                val errorClassMappings = declaredMethod.getAnnotation(RetrobombMappings::class.java).mappings
                declaredMethod.annotations.forEach {
                    when (it) {
                        is GET -> mergeIntoMappings(queryParamNames, it.value, HttpMethod.GET, errorClassMappings)
                        is POST -> mergeIntoMappings(queryParamNames, it.value, HttpMethod.POST, errorClassMappings)
                        is PATCH -> mergeIntoMappings(queryParamNames, it.value, HttpMethod.PATCH, errorClassMappings)
                        is PUT -> mergeIntoMappings(queryParamNames, it.value, HttpMethod.PUT, errorClassMappings)
                        is DELETE -> mergeIntoMappings(queryParamNames, it.value, HttpMethod.DELETE, errorClassMappings)
                        is HEAD -> mergeIntoMappings(queryParamNames, it.value, HttpMethod.HEAD, errorClassMappings)
                        is OPTIONS -> mergeIntoMappings(queryParamNames, it.value, HttpMethod.OPTIONS, errorClassMappings)
                    }
                }
            }
        }
        return mappings
    }

    private fun createPrimaryFromRetrofit(path: String): String {
        val bodyPattern = pathVariablePattern.matcher(path).replaceAll("""[^/?]+""")
        return """^.*?$bodyPattern/?"""
    }

    private fun createQueryFromRetrofit(queryParamNames: List<String>): String {
        return queryParamNames
            .takeIf { it.isNotEmpty() }?.joinToString(
                separator = "=$queryParameterPatternString&",
                prefix = "?",
                postfix = "=$queryParameterPatternString") ?: ""
    }

    private fun mergeIntoMappings(queryParamNames: List<String>, rawPath: String, httpMethod: HttpMethod, errorClassMappings: Array<out ErrorMapping>) {
        errorClassMappings.map {
            mappings[RouteStatusKey(Pattern.compile("${createPrimaryFromRetrofit(rawPath)}${createQueryFromRetrofit(queryParamNames)}"), httpMethod, it.code)] = it.errorType
        }
    }

    enum class HttpMethod {
        GET, POST, PATCH, PUT, DELETE, HEAD, OPTIONS
    }

    companion object {
        private val pathVariablePattern = Pattern.compile("""\{.+?}""")
        private val queryParameterPatternString = """[^&]*"""
    }
}
