package me.alllexey123.itmoqueue.services

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.File
import java.io.InputStream
import java.util.concurrent.CompletableFuture

@Service
class Telegram(val telegramClient: TelegramClient) : TelegramClient by telegramClient {

    @PostConstruct
    fun init() {
    }

    override fun downloadFile(filePath: String?): File? {
        return telegramClient.downloadFile(filePath)
    }

    override fun downloadFileAsStream(filePath: String?): InputStream? {
        return telegramClient.downloadFileAsStream(filePath)
    }

    override fun downloadFileAsync(filePath: String?): CompletableFuture<File?>? {
        return telegramClient.downloadFileAsync(filePath)
    }

    override fun downloadFileAsStreamAsync(filePath: String?): CompletableFuture<InputStream?>? {
        return telegramClient.downloadFileAsStreamAsync(filePath)
    }

}