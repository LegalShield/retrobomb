package com.legalshield.retrobomb

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RetrobombMappings(vararg val mappings: ErrorMapping)
