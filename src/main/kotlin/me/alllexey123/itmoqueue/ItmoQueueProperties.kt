package me.alllexey123.itmoqueue

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("itmoqueue")
class ItmoQueueProperties() {

    var botToken: String? = null

    var origin: String? = null
}