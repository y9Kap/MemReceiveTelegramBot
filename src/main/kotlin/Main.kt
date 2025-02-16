package org.example

import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.message.abstracts.PrivateContentMessage
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.y9san9.ksm.telegram.TelegramStorage
import me.y9san9.ksm.telegram.buildTelegramFSM
import me.y9san9.ksm.telegram.json.json
import me.y9san9.ksm.telegram.routing
import me.y9san9.ksm.telegram.routing.state
import me.y9san9.ksm.telegram.state.goto
import me.y9san9.ksm.telegram.state.handle
import me.y9san9.ksm.telegram.state.name
import me.y9san9.ksm.telegram.state.user
import me.y9san9.ksm.telegram.storage
import org.example.types.JsonConfig
import org.example.types.MessageService
import org.example.ui.clientUI
import org.example.ui.reviewerUI

suspend fun main() {
    val token: String = System.getenv("BOT_TOKEN") 
    val adminChatIdLong = System.getenv("ADMIN_CHAT_ID").toLong()
    val channelIdLong = System.getenv("CHANNEL_ID").toLong()
    val adminChatId = ChatId(RawChatId(adminChatIdLong))
    val channelId = ChatId(RawChatId(channelIdLong))
    val bot = telegramBot(token)
    val messageService = MessageService()
    
    val fsm = buildTelegramFSM {
        json = JsonConfig.json
        storage = TelegramStorage.InMemory()
        routing {
            initial = "initial"
            state {
                name = "initial"
                handle {
                    when (user.id) {
                        adminChatId -> goto("welcomeReviewer")
                        else -> goto("welcomeClient")
                    }
                }
            }
            clientUI(messageService, adminChatId)
            reviewerUI(messageService, channelId)
        }
    }
    
    bot.buildBehaviourWithLongPolling {
        launch {
            val flow = messagesFlow.filter { it.data is PrivateContentMessage<*> }
            fsm.run(bot, flow)
        }
        launch {
            allUpdatesFlow.collect { update ->
                println(update)
            }
        }
    }.join()
}