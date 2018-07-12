package com.legalshield.retrobomb

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection.*
/**
 * @RetrobombInterceptor
 * Intercepts all non-200 response codes and attempts to convert them to structured error data.
 *
 * To use, add an instance of this interceptor as the _first_ interceptor in your OkHttpClient.
 *
 * By default, raw responses are wrapped in a RetrobombException as a String unless otherwise
 * specified by @RetrobombMappings in the supplied repository.
 *
 * @param repositoryClass The retrofit repository interface
 * @param gson Optionally inject a custom gson configuration
 */
class RetrobombInterceptor(repositoryClass: Class<*>, private val gson: Gson = Gson()) : Interceptor {
    private val mapping = Retrobomb(repositoryClass).generateMapping()

    override fun intercept(chain: Interceptor.Chain?): Response {
        val request = chain!!.request()
        return chain.proceed(request).apply {
            val code = code()
            when (code) {
                HTTP_ACCEPTED, HTTP_OK, HTTP_CREATED, HTTP_NOT_AUTHORITATIVE, HTTP_RESET, HTTP_NO_CONTENT, HTTP_PARTIAL -> {}
                else -> {
                    val responseBody = body()?.string() ?: return@apply
                    val url = request.url().encodedPath()

                    val convertClass = mapping.keys
                        .firstOrNull {
                            it.code == code && it.method.name == request.method() && it.route.matcher(url).matches()
                        }
                        .let { mapping[it]?.java }
                        .takeIf { it != String::class.java } ?: throw RetrobombException(responseBody)

                    lateinit var mappedException: RetrobombException
                    try {
                        mappedException = RetrobombException(gson.fromJson(responseBody, convertClass))
                    } catch (e: JsonSyntaxException) {
                        throw RetrobombMappingException("Unable to convert JSON to expected type", e)
                    } catch (e: IllegalStateException) {
                        throw RetrobombMappingException("Unable to convert JSON to expected type", e)
                    }
                    throw mappedException
                }
            }
        }
    }
}