package com.serverless

import java.util.*

class RandomWord {
    fun getter(message: String): String {
        val words = arrayOf(
                "すごーい！",
                "わーい！",
                "そうなんだー！",
                "へー！",
                "うっそー！"
        )
        val rand = Random()

        return message + "？\n" + words[rand.nextInt(words.size)]
    }
}
