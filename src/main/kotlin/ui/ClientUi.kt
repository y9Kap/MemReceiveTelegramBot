package org.example.ui

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.extensions.utils.withContentOrThrow
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.utils.row
import me.y9san9.ksm.telegram.json.goto
import me.y9san9.ksm.telegram.json.receive
import me.y9san9.ksm.telegram.routing.StateRouting
import me.y9san9.ksm.telegram.routing.state
import me.y9san9.ksm.telegram.state.*
import me.y9san9.ksm.telegram.state.data.StateData
import me.y9san9.ksm.telegram.state.goto
import org.example.types.*
import java.util.*

fun StateRouting.clientUI(messageService: MessageService, adminChatId: ChatId) {
    state {
        name = "welcomeClient"
        transition {
            bot.sendMessage(
                chatId = user.id,
                text = WELCOME_CLIENT,
                replyMarkup = replyKeyboard(true) {
                    row {
                        simpleButton("Отправить мем")
                    }
                }
            )
        }
        handle {
            with(message){
                if (text == "Отправить мем") {
                    goto("receiveMem")
                } else {
                    bot.sendMessage(
                        chatId = chat.id,
                        text = COMMAND_FAILED
                    )
                    stay(this@state.name)
                }
            }
        }
    }

    state {
        name = "clientMenu"
        transition {
            bot.sendMessage(
                chatId = user.id,
                text = MENU_CLIENT,
                replyMarkup = replyKeyboard(true) {
                    row {
                        simpleButton("Отправить мем")
                    }
                }
            )
        }

        handle {
            with(message){
                if (text == "Отправить мем") {
                    goto("receiveMem")
                } else {
                    bot.sendMessage(
                        chatId = chat.id,
                        text = COMMAND_FAILED
                    )
                    stay(this@state.name)
                }
            }
        }
    }

    state {
        name = "receiveMem"
        transition {
            bot.sendMessage(
                chatId = user.id,
                text = MEM,
                replyMarkup = ReplyKeyboardRemove()
            )
        }
        
        handle {
            try {
                val message = message.withContentOrThrow<PhotoContent>()
                val uuid = UUID.randomUUID().toString()
                messageService.addMessage(uuid, message)

                goto("sendOrEdit", data = StateData.String(uuid))
            } catch (_: Exception) {
                bot.reply(
                    toChatId = user.id,
                    toMessageId = message.messageId,
                    text = MEM_INPUT_ERROR
                )
                goto(this@state.name)
            }
        }
    }

    state {
        name = "sendOrEdit"
        transition {
            bot.sendMessage(
                chatId = user.id,
                text = "Выберите действие:",
                replyMarkup = replyKeyboard(true) {
                    row {
                        simpleButton("Отправить на оценку")
                    }
                    row {
                        simpleButton("Отмени, хочу отправить другой мем")
                    }
                    row {
                        simpleButton("Выйти в меню")
                    }
                }
            )
        }

        handle {
            val uuid = receive<String>()
            if (message.text == "Отправить на оценку") {
                messageService.messageStatusToWaited(uuid)
                bot.sendMessage(
                    chatId = adminChatId,
                    text = "Тебе пришел новый мем, проверь через меню!"
                )
                bot.sendMessage(
                    chatId = user.id,
                    text = "Мем отправлен, жди, пока админ посмотрит",
                    replyMarkup = ReplyKeyboardRemove()
                )
                goto("clientMenu")
            } else if (message.text == "Отмени, хочу отправить другой мем") {
                goto("receiveMem")
            } else if (message.text == "Выйти в меню") {
                messageService.removeMessage(uuid)
                goto("clientMenu")
            } else {
                bot.sendMessage(
                    chatId = user.id,
                    text = COMMAND_FAILED
                )
                stay(this@state.name)
            }
        }
    }
    
}