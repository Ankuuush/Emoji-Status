package com.ankush.emojistatus.models

import androidx.emoji.text.EmojiCompat

data class User (val uid: String ="",
                 val displayName: String? ="",
                 val emoji:String?=String(Character.toChars(0x1F60A)))
