package com.legalshield.retrobomb

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ErrorMapping(val errorType: KClass<*>, vararg val codes: Int)