package ru.sokomishalov.skraper.bot.telegram.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import ru.sokomishalov.skraper.bot.telegram.autoconfigure.BotProperties
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux

/**
 * @author sokomishalov
 */
@Configuration
@EnableSwagger2WebFlux
@EnableConfigurationProperties(BotProperties::class)
class BotConfig