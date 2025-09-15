package me.alllexey123.itmoqueue.bot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("telegram.bot")
class BotProperties() {

    var token: String? = null
}
