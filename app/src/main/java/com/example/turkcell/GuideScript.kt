package com.example.turkcell

object GuideScript {
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

    var index: Int = 0

    fun current(): String = mhrsSteps.getOrElse(index) { "Bitti." }
    fun next(): String { if (index < mhrsSteps.lastIndex) index++; return current() }
    fun prev(): String { if (index > 0) index--; return current() }
    fun restart(): String { index = 0; return current() }
}
