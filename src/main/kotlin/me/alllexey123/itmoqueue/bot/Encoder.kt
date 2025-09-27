package me.alllexey123.itmoqueue.bot

interface Encoder {

    fun encode(vararg objects: Any?): String {
        return objects.joinToString("~") { o ->
            o?.toString()?.replace("~", "\\~") ?: ""
        }
    }

    fun decode(encoded: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var escaped = false

        for (c in encoded) {
            when {
                escaped -> {
                    current.append(c)
                    escaped = false
                }
                c == '\\' -> escaped = true
                c == '~' -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
        }
        result.add(current.toString())
        return result
    }

}