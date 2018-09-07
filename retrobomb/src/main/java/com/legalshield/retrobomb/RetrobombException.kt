package com.legalshield.retrobomb

class RetrobombException(val url: String, val code: Int, val data: Any): RuntimeException()
