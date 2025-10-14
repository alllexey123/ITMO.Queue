package me.alllexey123.itmoqueue.bot.command

enum class Command(
    val raw: String,
    val escaped: String,
) {
    NEW("/new", "/new"),
    LABS("/labs", "/labs"),
    SUBJECTS("/subjects", "/subjects"),
    NAME("/name", "/name"),
    START("/start", "/start"),
    CANCEL("/cancel", "/cancel"),
    GROUP("/group", "/group"),
}