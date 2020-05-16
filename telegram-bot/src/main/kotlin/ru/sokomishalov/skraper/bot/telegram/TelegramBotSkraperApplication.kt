package ru.sokomishalov.skraper.bot.telegram

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.ApiContextInitializer

/**
 * @author sokomishalov
 */
@SpringBootApplication
class TelegramBotApplication

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    runApplication<TelegramBotApplication>(*args)
}