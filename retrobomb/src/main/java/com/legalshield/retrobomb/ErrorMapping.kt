package com.legalshield.retrobomb

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ErrorMapping(val code: Int, val errorType: KClass<*>)