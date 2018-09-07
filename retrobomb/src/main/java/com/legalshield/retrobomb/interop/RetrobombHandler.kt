@file:JvmName("RetrobombHandler")
package com.legalshield.retrobomb.interop

import com.legalshield.retrobomb.RetrobombException
import com.legalshield.retrobomb.entity.ResponseData

/**
 * This will ignore the error and continue without calling the handler if the data doesn't conform to the expected type
 * or this Throwable is not a RetrobombException.
 */
fun <ErrorData> Throwable.handleErrorData(type: Class<ErrorData>, handler: Handler<ResponseData<ErrorData>>) =
    if (this is RetrobombException && data.javaClass == type) {
      handler.handle(ResponseData(url = url, code = code, data = type.cast(data)))
      true
    } else {
      false
    }
