package org.example.ui

import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.utils.row
import me.y9san9.ksm.telegram.json.goto
import me.y9san9.ksm.telegram.json.receive
import me.y9san9.ksm.telegram.routing.StateRouting
import me.y9san9.ksm.telegram.routing.state
import me.y9san9.ksm.telegram.state.*
import org.example.types.*

fun StateRouting.reviewerUI(messageService: MessageService, channelId: ChatId) {
    state {
        name = "welcomeReviewer"
        transition {
            bot.send(
                chatId = user.id,
                WELCOME_ADMIN,
                replyMarkup = replyKeyboard(true) {
                    row {
                        simpleButton("Мемы на оценку:")
                    }
                }
            )
        }

        handle {
            if (message.text == "Мемы на оценку:") {
                goto("preCheckPublications")
            } else bot.sendMessage(
                chatId = user.id,
                text = COMMAND_FAILED
            )
            stay(this@state.name)
        }
    }

    state {
        name = "preCheckPublications"
        transition {
            val amountOfMemes = messageService.getWaitingMessages().size
            val memesWord = getMemWord(amountOfMemes)

            if (amountOfMemes == 0) {
                bot.send(
                    chatId = user.id,
                    "Нет мемов для оценки.",
                    replyMarkup = replyKeyboard(true) {
                        row {
                            simpleButton("Мемы на оценку:")
                        }
                    }
                )
                goto("welcomeReviewer")
            } else {
                bot.send(
                    chatId = user.id,
                    text = "У тебя $amountOfMemes $memesWord для оценки.",
                    replyMarkup = replyKeyboard(true) {
                        row {

                            simpleButton("Начать оценку")
                        }
                        row {
                            simpleButton("Вернуться в меню")
                        }
                    }
                )
            }
        }

        handle {
            if (message.text == "Начать оценку") {
                val memMessages = messageService.getWaitingMessagesWithUUIDs()
                val sortedPublications = memMessages.sortedBy { it.second.date.unixMillisLong }
                val uuidToMem = sortedPublications.first()
                bot.send(
                    user.id,
                    uuidToMem.second.content.media,
                    text = uuidToMem.second.content.text,
                    replyMarkup = replyKeyboard(true) {
                        row {
                            simpleButton("Норм, в канал")
                        }
                        row {
                            simpleButton("В канал, но без комментария")
                        }
                        row {
                            simpleButton("Говно")
                        }
                    }
                )
                goto("checkPublication", uuidToMem.first, false)

            } else if (message.text == "Вернуться в меню") {
                goto("welcomeReviewer")
            } else bot.sendMessage(
                chatId = user.id,
                text = COMMAND_FAILED
            )
            stay(this@state.name)
        }
    }

    state {
        name = "checkPublication"
        handle {
            val memUuid = receive<String>()
            val mem = messageService.getMessage(memUuid)
            if (message.text == "Норм, в канал") {
                bot.send(
                    channelId,
                    mem.content.media,
                    text = mem.content.text
                )
                bot.send(
                    mem.chat.id,
                    "Твой мем опубликован! Ищи его в канале"
                )
                bot.send(
                    chatId = user.id,
                    "Мем опубликован в твоем канале!"
                )
                messageService.removeMessage(memUuid)
                goto("preCheckPublications")
            } else if (message.text == "В канал, но без комментария") {
                bot.send(
                    channelId,
                    mem.content.media
                )
                bot.send(
                    mem.chat.id,
                    "Твой мем опубликован! Ищи его в канале"
                )
                bot.send(
                    chatId = user.id,
                    "Мем опубликован в твоем канале!"
                )
                messageService.removeMessage(memUuid)
                goto("preCheckPublications")
            } else if (message.text == "Говно") {
                bot.send(
                    mem.chat.id,
                    "Админу твой мем не понравился. Ты можешь попробовать отправить другой мем"
                )
                messageService.removeMessage(memUuid)
                goto("preCheckPublications")
            } else bot.sendMessage(
                chatId = user.id,
                text = COMMAND_FAILED
            )
            stay(this@state.name)
        }
    }

}