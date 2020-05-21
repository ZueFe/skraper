/**
 * Copyright (c) 2019-present Mikhael Sokolov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress(
        "BlockingMethodInNonBlockingContext",
        "RemoveExplicitTypeArguments"
)

package ru.sokomishalov.skraper.bot.telegram.service

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo
import ru.sokomishalov.commons.core.common.unit
import ru.sokomishalov.commons.core.log.Loggable
import ru.sokomishalov.skraper.Skraper
import ru.sokomishalov.skraper.bot.telegram.autoconfigure.BotProperties
import ru.sokomishalov.skraper.download
import ru.sokomishalov.skraper.internal.net.path
import ru.sokomishalov.skraper.model.*
import ru.sokomishalov.skraper.resolve
import java.io.File
import java.nio.file.Files.createTempDirectory
import kotlin.text.RegexOption.*

/**
 * @author sokomishalov
 */
@Service
class BotService(
        private val botProperties: BotProperties,
        private val knownSkrapers: List<Skraper>
) : TelegramLongPollingBot() {

    override fun getBotUsername(): String = botProperties.username
    override fun getBotToken(): String = botProperties.token
    override fun onUpdateReceived(update: Update) = GlobalScope.launch { scrape(requireNotNull(update.message)) }.unit()

    private suspend fun scrape(message: Message) {
        // 0. say hello
        if (message.text.orEmpty() == "/start") {
            sendText(message, "Hello!")
            return
        }

        // 1. extract url
        val url = extractUrlFromMessage(message.text.orEmpty())
        if (url == null) {
            sendText(message, "URL not found in the message")
            return
        }

        // 2. find suitable skraper
        val supportedSkraper: Skraper? = knownSkrapers.find { it.supports(url.orEmpty()) }
        if (supportedSkraper == null) {
            sendText(message, "Unsupported URL")
            return
        }

        // 3. try to either scrape posts and download attachments or just download attachment
        val posts = runCatching { supportedSkraper.getPosts(url.path) }.getOrElse { emptyList() }
        val latestPost = posts.firstOrNull()
        val tmpDir = createTempDirectory("skraper-bot").toFile()

        runCatching {
            when {
                latestPost != null -> {
                    latestPost
                            .media
                            .map { media ->
                                media to Skraper.download(
                                        media = media,
                                        destDir = tmpDir,
                                        skrapers = knownSkrapers
                                )
                            }
                            .let { items ->
                                when {
                                    items.isNotEmpty() -> sendMedia(message, items)
                                    else -> saySorry(message)
                                }
                            }
                }

                else -> {
                    listOf(Video(url), Audio(url), Image(url))
                            .map { media ->
                                media to Skraper.resolve(
                                        media = media,
                                        skrapers = knownSkrapers
                                )
                            }
                            .firstOrNull { (original, resolved) -> original.url != resolved.url }
                            ?.let { (_, resolved) ->
                                val file = Skraper.download(
                                        media = resolved,
                                        destDir = tmpDir,
                                        skrapers = knownSkrapers
                                )
                                sendMedia(message, listOf(resolved to file))
                            }
                            ?: saySorry(message)

                }
            }
        }.onFailure {
            logError(it)
            saySorry(message)
        }

        // 4. release
        tmpDir.deleteRecursively()
    }

    private fun extractUrlFromMessage(text: String): URLString? {
        return URL_REGEX
                .find(text)
                ?.groupValues
                ?.firstOrNull()
                ?.trim()
    }

    private suspend fun sendText(message: Message, msg: String): Unit = withContext<Unit>(IO) {
        execute(SendMessage().apply {
            chatId = message.chatId?.toString()
            text = msg
            replyToMessageId = message.messageId
        })
    }

    private suspend fun sendMedia(message: Message, attachments: List<Pair<Media, File>>): Unit = withContext<Unit>(IO) {
        execute(SendMediaGroup().apply {
            replyToMessageId = message.messageId
            chatId = message.chatId?.toString()
            media = attachments.map { (media, file) ->
                when (media) {
                    is Image -> InputMediaPhoto().apply {
                        setMedia(file, file.nameWithoutExtension)
                    }
                    is Video -> InputMediaVideo().apply {
                        setMedia(file, file.nameWithoutExtension)
                    }

                    is Audio -> InputMediaPhoto().apply {
                        setMedia(file, file.nameWithoutExtension)
                    }
                }
            }
        })
    }

    private suspend fun saySorry(message: Message) {
        sendText(message, "Unable to download this media, sorry :(")
    }

    companion object : Loggable {
        private val URL_REGEX: Regex = "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)".toRegex(setOf(IGNORE_CASE, MULTILINE, DOT_MATCHES_ALL))
    }
}
