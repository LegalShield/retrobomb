package com.legalshield.retrobomb


/**
 * @HttpErrorCodes
 * List of HTTP error codes.
 */
object HttpErrorCodes {
    const val BAD_REQUEST = 400
    const val UNAUTHORIZED = 401
    const val PAYMENT_REQUIRED = 402
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val METHOD_NOT_ALLOWED = 405
    const val NOT_ACCEPTABLE = 406
    const val PROXY_AUTH_REQUIRED = 407
    const val REQUEST_TIMEOUT = 408
    const val CONFLICT = 409
    const val GONE = 410
    const val LENGTH_REQUIRED = 411
    const val PRECONDITION_FAILED = 412
    const val PAYLOAD_TOO_LARGE = 413
    const val URI_TOO_LONG = 414
    const val UNSUPPORTED_MEDIA_TYPE = 415
    const val RANGE_NOT_SATISFIABLE = 416
    const val EXPECTATION_FAILED = 417
    const val MISDIRECTED_URL = 421
    const val UNPROCESSABLE_ENTITY = 422
    const val LOCKED = 423
    const val FAILED_DEPENDECY = 424
    const val UPGRADE_REQUIRED = 426
    const val PRECONDITION_REQUIRED = 428
    const val TOO_MANY_REQUESTS = 429
    const val HEADER_FIELD_TOO_LARGE = 431
    const val UNAVAILABLE_FOR_LEGAL_REASONS = 451
    const val INTERNAL_ERROR = 500
    const val NOT_IMPLEMENTED = 501
    const val BAD_GATEWAY = 502
    const val UNAVAILABLE = 503
    const val GATEWAY_TIMEOUT = 504
    const val VERSION = 505
    const val VARIANT_ALSO_NEGOTIOATES = 506
    const val INSUFFICIENT_STORAGE = 507
    const val LOOP_DETECTED = 508
    const val NOT_EXTENDED = 510
    const val NETWORK_AUTH_REQUIRED = 511
}