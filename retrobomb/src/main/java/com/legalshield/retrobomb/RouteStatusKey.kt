package com.legalshield.retrobomb

import com.google.re2j.Pattern

internal data class RouteStatusKey(val route: Pattern, val method: Retrobomb.HttpMethod, val code: Int) {
    override fun equals(other: Any?): Boolean {
        val otherStatusKey = other as? RouteStatusKey ?: return false
        return route.pattern() == otherStatusKey.route.pattern() &&
            method == otherStatusKey.method &&
            code == otherStatusKey.code
    }

    override fun hashCode(): Int {
        var result = route.pattern().hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + code
        return result
    }
}