package com.example.turkcell

enum class CommandType {
    LOST, CALL, MESSAGE, MHRS, UNKNOWN
}

object CommandParser {

    fun parse(raw: String): CommandType {
        val t = raw.trim().lowercase()

        return when {
            t.isEmpty() -> CommandType.UNKNOWN

            // Kayboldum
            t.contains("kaybol") || t.contains("kurtar") || t.contains("çıkış") ->
                CommandType.LOST

            // Arama
            t.contains("ara") || t.contains("arama") || t.contains("telefon") ->
                CommandType.CALL

            // Mesaj
            t.contains("mesaj") || t.contains("whatsapp") || t.contains("sms") ->
                CommandType.MESSAGE

            // MHRS
            t.contains("mhrs") || t.contains("randevu") ->
                CommandType.MHRS

            else -> CommandType.UNKNOWN
        }
    }
}
