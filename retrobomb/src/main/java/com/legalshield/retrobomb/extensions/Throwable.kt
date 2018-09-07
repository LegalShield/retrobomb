package com.legalshield.retrobomb.extensions

import com.legalshield.retrobomb.RetrobombException
import com.legalshield.retrobomb.entity.ResponseData

/**
 * This will ignore the error and continue without calling the handler if the data doesn't conform to the expected type
 * or this Throwable is not a RetrobombException.
 */
inline fun <reified ErrorData> Throwable.handleRetrobombErrorData(
    crossinline handler: (data: ResponseData<ErrorData>) -> Unit
): Boolean {
  return if (this is RetrobombException && data is ErrorData) {
    handler(ResponseData(url = url, code = code, data = data))
    true
  } else {
    false
  }
}

/**
 * This will ignore the error and continue without calling the handler if this Throwable is not a RetrobombException.
 */
inline fun Throwable.unwrapRetrobombException(crossinline handler: (data: ResponseData<Any>) -> Unit): Boolean {
    return if (this is RetrobombException) {
      handler(ResponseData(url = url, code = code, data = data))
      true
    } else {
      false
    }
}
