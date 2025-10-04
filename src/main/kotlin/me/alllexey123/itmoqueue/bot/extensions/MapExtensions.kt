package me.alllexey123.itmoqueue.bot.extensions

fun Map<String, Any>.getInt(key: String, defaultValue: Int = 0): Int {
    return getIntOrNull(key) ?: defaultValue
}

fun Map<String, Any>.getIntOrNull(key: String): Int? {
    return when (val value = this[key]) {
        is Int -> value
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }
}

fun Map<String, Any>.getLong(key: String, defaultValue: Long = 0): Long {
    return getLongOrNull(key) ?: defaultValue
}

fun Map<String, Any>.getLongOrNull(key: String): Long? {
    return when (val value = this[key]) {
        is Long -> value
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    }
}

fun Map<String, Any>.getString(key: String, defaultValue: String = ""): String {
    return getStringOrNull(key) ?: defaultValue
}

fun Map<String, Any>.getStringOrNull(key: String): String? {
    return when (val value = this[key]) {
        is String -> value
        else -> value?.toString()
    }
}

fun Map<String, Any>.getBoolean(key: String, defaultValue: Boolean = false): Boolean {
    return getBooleanOrNull(key) ?: defaultValue
}


fun Map<String, Any>.getBooleanOrNull(key: String): Boolean? {
    return when (val value = this[key]) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull()
        else -> null
    }
}