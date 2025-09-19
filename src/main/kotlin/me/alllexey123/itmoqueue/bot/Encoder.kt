package me.alllexey123.itmoqueue.bot

import java.util.Base64

interface Encoder {

    fun encode(vararg objects: Any?): String {
        return objects.joinToString(":") { o -> encodeParam(o) }
    }

    fun decode(encoded: String): List<String> {
        return encoded.split(":").map { s -> decodeParam(s) }
    }

    fun encodeParam(obj: Any?): String {
        return Base64.getUrlEncoder().encodeToString(obj.toString().toByteArray())
    }

    fun decodeParam(string: String): String {
        return String(Base64.getUrlDecoder().decode(string))
    }
}