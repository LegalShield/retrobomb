package com.legalshield.retrobomb.extensions

import com.legalshield.retrobomb.RetrobombException

/**
 * This will ignore the error and continue without calling the handler if the data doesn't conform to the expected type
 * or this Throwable is not a RetrobombException.
 */
inline fun <reified ErrorData> Throwable.handleRetrobombErrorData(crossinline handler: (data: ErrorData) -> Unit): Boolean {
  return if (this is RetrobombException && data is ErrorData) {
    handler(data)
    true
  } else {
    false
  }
}

/**
 * This will ignore the error and continue without calling the handler if this Throwable is not a RetrobombException.
 */
inline fun Throwable.unwrapRetrobombException(crossinline handler: (data: Any) -> Unit): Boolean {
    return if (this is RetrobombException) {
      handler(this.data)
      true
    } else {
      false
    }
}
