package ru.sokomishalov.skraper.bot.telegram.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sokomishalov.skraper.Skraper
import ru.sokomishalov.skraper.SkraperClient
import ru.sokomishalov.skraper.bot.telegram.autoconfigure.BotProperties
import ru.sokomishalov.skraper.client.ktor.KtorSkraperClient
import ru.sokomishalov.skraper.knownList

/**
 * @author sokomishalov
 */
@Configuration
@EnableConfigurationProperties(BotProperties::class)
class SkraperBotConfig {
    @Bean
    fun ktorClient(): SkraperClient = KtorSkraperClient()

    @Bean
    fun knownSkrapers(ktor: SkraperClient): List<Skraper> = Skraper.knownList(ktor)
}