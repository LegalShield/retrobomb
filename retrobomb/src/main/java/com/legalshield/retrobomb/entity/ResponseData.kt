package com.legalshield.retrobomb.entity

data class ResponseData<T>(val url: String, val code: Int, val data: T)