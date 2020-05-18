package ru.sokomishalov.skraper.bot.telegram.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import ru.sokomishalov.commons.spring.webclient.REACTIVE_WEB_CLIENT
import ru.sokomishalov.skraper.Skraper
import ru.sokomishalov.skraper.SkraperClient
import ru.sokomishalov.skraper.client.spring.SpringReactiveSkraperClient
import ru.sokomishalov.skraper.knownList

/**
 * @author sokomishalov
 */
@Configuration
class SkraperConfig {

    @Bean
    fun webClient(): WebClient = REACTIVE_WEB_CLIENT

    @Bean
    fun client(webClient: WebClient): SkraperClient = SpringReactiveSkraperClient(webClient = webClient)

    @Bean
    fun knownSkrapers(client: SkraperClient): List<Skraper> = Skraper.knownList(client)
}