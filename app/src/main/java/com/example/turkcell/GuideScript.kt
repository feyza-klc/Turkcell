package com.example.turkcell

object GuideScript {

    const val MODE_MHRS = "MHRS"
    const val MODE_CALL = "CALL"
    const val MODE_MESSAGE = "MESSAGE"

    // MHRS adımları
    val mhrsSteps = listOf(
        "Ekranda Randevu Al yazan butona tıkla.",
        "E-Devlet ile Giriş Yap seçeneğine tıkla.",
        "T.C. kimlik numaranı ve e-Devlet şifreni ekrana yaz. Sonra giriş yap.",
        "Eğer mobil onay isterse, e-Devlet uygulamasından onay ver. İstemiyorsa devam et.",
        "Hastane randevusu al veya genel arama seçeneğine tıkla.",
        "İl ve klinik seçmen zorunlu. Diğerlerini istersen seç. Sonra Randevu Ara'ya tıkla.",
        "Listeden istediğin randevuyu seç.",
        "Tarih ve saat seç.",
        "Tamam'a tıkla. Bitti."
    )

    // ARAMA adımları (senin istediğin akış)
    val callSteps = listOf(
        "Kişiler sekmesine dokun.",
        "Aramak istediğin kişiyi listeden seç.",
        "Ekranda görünen telefon et veya yeşil ahize butonuna dokun."
    )

    var messageSteps = listOf(
        "Yeni mesaj yazmak için artı veya yeni mesaj butonuna dokun.",
        "Mesaj göndermek istediğin kişiyi seç.",
        "Mesaj yazma alanına mesajını yaz.",
        "Gönder butonuna dokun."
    )

    // aktif mod + index
    var mode: String = MODE_MHRS
    var index: Int = 0

    private fun steps(): List<String> {
        return when (mode) {
            MODE_CALL -> callSteps
            MODE_MHRS -> mhrsSteps
            else -> messageSteps
        }
    }

    fun size(): Int = steps().size

    fun setGuideMode(newMode: String) {
        mode = newMode
        index = 0
    }

    fun current(): String = steps().getOrElse(index) { "Bitti." }

    fun next(): String {
        if (index < steps().lastIndex) index++
        return current()
    }

    fun prev(): String {
        if (index > 0) index--
        return current()
    }

    fun restart(): String {
        index = 0
        return current()
    }
}
