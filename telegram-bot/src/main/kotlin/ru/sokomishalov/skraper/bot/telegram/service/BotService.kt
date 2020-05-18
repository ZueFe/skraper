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
package ru.sokomishalov.skraper.bot.telegram.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sokomishalov.commons.core.common.unit
import ru.sokomishalov.commons.core.log.Loggable
import ru.sokomishalov.skraper.Skraper
import ru.sokomishalov.skraper.bot.telegram.autoconfigure.BotProperties
import ru.sokomishalov.skraper.internal.net.path
import ru.sokomishalov.skraper.model.URLString
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
    override fun onUpdateReceived(update: Update) = GlobalScope.launch { reply(update.message) }.unit()

    private suspend fun reply(message: Message?) {
        logInfo { "Received message ${message?.text}" }

        // 0. extract url
        val url = extractUrlFromMessage(message?.text.orEmpty())
        if (url == null) {
            send(SendMessage(message?.chatId, "URL not found in the message"))
            return
        }

        // 1. find suitable skraper
        val supportedSkraper: Skraper? = knownSkrapers.find { it.supports(url.orEmpty()) }
        if (supportedSkraper == null) {
            send(SendMessage(message?.chatId, "Unsupported URL"))
            return
        }

        // 2. try to either scrape posts and download attachments or just download attachment
        val posts = runCatching { supportedSkraper.getPosts(url.path) }.getOrElse { emptyList() }
        when {
            posts.isNotEmpty() -> {
                send(SendMessage(message?.chatId, "Found ${posts.size} posts for url $url"))
                // TODO
            }
            else -> {
                send(SendMessage(message?.chatId, "Posts not found, attempting to download media by link"))
                // TODO
            }
        }
    }

    private fun extractUrlFromMessage(text: String): URLString? {
        return URL_REGEX
                .find(text)
                ?.groupValues
                ?.firstOrNull()
                ?.trim()
    }

    companion object : Loggable {
        private val URL_REGEX: Regex = "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)".toRegex(setOf(IGNORE_CASE, MULTILINE, DOT_MATCHES_ALL))
    }
}
