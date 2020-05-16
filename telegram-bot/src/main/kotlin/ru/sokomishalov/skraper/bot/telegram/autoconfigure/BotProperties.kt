package ru.sokomishalov.skraper.bot.telegram.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * @author sokomishalov
 */
@ConfigurationProperties(prefix = "skraper.bot")
@ConstructorBinding
data class BotProperties(
        val username: String,
        val token: String
)