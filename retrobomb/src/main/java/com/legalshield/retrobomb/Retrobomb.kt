package com.legalshield.retrobomb

import retrofit2.http.*
import com.google.re2j.Pattern
import java.lang.StringBuilder
import java.lang.reflect.Method
import kotlin.reflect.KClass


class Retrobomb<Repository>(private val repositoryType: Class<Repository>) {
    private val mappings: MutableMap<RouteStatusKey, KClass<*>> = HashMap()

    internal fun generateMapping(): Map<RouteStatusKey, KClass<*>> {
        if (repositoryType.isAnnotationPresent(RetrobombMappings::class.java)) {
            repositoryType.getAnnotation(RetrobombMappings::class.java)?.mappings?.let { errorClassMappings ->
                repositoryType.declaredMethods.forEach { declaredMethods ->
                    generateMapping(declaredMethods, errorClassMappings)
                }
            }
        }

        repositoryType.declaredMethods.forEach { declaredMethod ->
            if (declaredMethod.isAnnotationPresent(RetrobombMappings::class.java)) {
                declaredMethod.getAnnotation(RetrobombMappings::class.java)?.mappings?.let { errorClassMappings ->
                    generateMapping(declaredMethod, errorClassMappings)
                }
            }
        }

        return mappings
    }

    private fun generateMapping(declaredMethod: Method, errorClassMappings: Array<out ErrorMapping>) {
        val queryParamNames = getQueryParamNames(declaredMethod)
        mergeIntoMappings(declaredMethod, queryParamNames, errorClassMappings)
    }

    private fun getQueryParamNames(declaredMethod: Method): List<String> {
        return declaredMethod.parameterAnnotations
                .flatMap { annotations ->
                    annotations.mapNotNull { annotation ->
                        annotation.takeIf { it is Query } as Query?
                    }
                }
                .map { it.value }

    }

    private fun mergeIntoMappings(
            declaredMethod: Method,
            queryParamNames: List<String>,
            errorClassMappings: Array<out ErrorMapping>
    ) {
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

    private fun mergeIntoMappings(
            queryParamNames: List<String>,
            rawPath: String,
            httpMethod: HttpMethod,
            errorClassMappings: Array<out ErrorMapping>
    ) {
        errorClassMappings.map {
            val pattern =
                    Pattern.compile("${createPathFromRetrofit(rawPath)}${createQueryFromRetrofit(queryParamNames)}")
            it.codes.forEach { code ->
                mappings[RouteStatusKey(pattern, httpMethod, code)] = it.errorType
            }
        }
    }

    private fun createPathFromRetrofit(path: String): String {
        val bodyPattern = pathVariablePattern.matcher(path).replaceAll("""[^/?]+""")
        return """^.*?$bodyPattern/?"""
    }

    private fun createQueryFromRetrofit(queryParamNames: List<String>): String {
        return queryParamNames
                .takeIf { it.isNotEmpty() }?.joinToString(
                        separator = "=$queryParameterPatternString&",
                        prefix = "\\?",
                        postfix = "=$queryParameterPatternString"
                ) { escape(it) } ?: ""
    }

    enum class HttpMethod {
        GET, POST, PATCH, PUT, DELETE, HEAD, OPTIONS
    }

    private fun escape(input: String): String {
        val stringBuilder = StringBuilder()
        for (char in input) {
            if (escapedChars.contains(char)) {
                stringBuilder.append("\\$char")
            } else {
                stringBuilder.append(char)
            }
        }
        return stringBuilder.toString()
    }

    companion object {
        private const val queryParameterPatternString = "[^&]*"
        private val pathVariablePattern = Pattern.compile("""\{.+?}""")
        private const val escapedChars = "\\.[]{}()<>*+-=?^${'$'}|"
    }
}