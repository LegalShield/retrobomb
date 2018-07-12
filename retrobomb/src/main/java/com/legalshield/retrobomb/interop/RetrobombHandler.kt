@file:JvmName("RetrobombHandler")
package com.legalshield.retrobomb.interop

import com.legalshield.retrobomb.RetrobombException

/**
 * This will ignore the error and continue without calling the handler if the data doesn't conform to the expected type
 * or this Throwable is not a RetrobombException.
 */
fun <ErrorData> Throwable.handleErrorData(type: Class<ErrorData>, handler: Handler<ErrorData>) =
    if (this is RetrobombException && data.javaClass == type) {
      handler.handle(type.cast(data))
      true
    } else {
      false
    }
