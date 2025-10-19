package dev.alllexey.itmoqueue.bot.extensions

// 0-indexed
fun <T> Iterable<T>.atPage(page: Int, perPage: Int): List<T> {
    return this.drop(page * perPage).take(perPage)
}