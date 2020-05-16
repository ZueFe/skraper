package ru.sokomishalov.skraper.bot.telegram.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sokomishalov.commons.core.log.Loggable
import ru.sokomishalov.skraper.bot.telegram.autoconfigure.BotProperties

/**
 * @author sokomishalov
 */
@Service
class BotService(private val botProperties: BotProperties) : TelegramLongPollingBot() {

    companion object : Loggable

    override fun getBotUsername(): String = botProperties.username
    override fun getBotToken(): String = botProperties.token

    override fun onUpdateReceived(update: Update?) {
        GlobalScope.launch {
            logInfo { "Received message ${update?.message?.text}" }
        }
    }

}