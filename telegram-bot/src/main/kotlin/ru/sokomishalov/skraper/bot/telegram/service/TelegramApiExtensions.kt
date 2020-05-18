package ru.sokomishalov.skraper.bot.telegram.service

import kotlinx.coroutines.suspendCancellableCoroutine
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.meta.updateshandlers.SentCallback
import java.io.Serializable
import kotlin.coroutines.resumeWithException

/**
 * @author sokomishalov
 */
internal fun initTelegramApi() = ApiContextInitializer.init()

@Suppress("EXPERIMENTAL_API_USAGE")
internal suspend fun <T : Serializable> AbsSender.send(method: BotApiMethod<T>): T {
    return suspendCancellableCoroutine { cont ->
        executeAsync(method, object : SentCallback<T> {
            override fun onResult(method: BotApiMethod<T>, response: T) = cont.resume(response) {}
            override fun onException(method: BotApiMethod<T>, exception: Exception) = cont.resumeWithException(exception)
            override fun onError(method: BotApiMethod<T>, apiException: TelegramApiRequestException) = cont.resumeWithException(apiException)
        })
    }
}