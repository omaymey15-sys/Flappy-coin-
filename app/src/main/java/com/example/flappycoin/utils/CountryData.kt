package com.example.flappycoin.utils

data class Country(
    val code: String,
    val name: String,
    val currency: String,
    val exchangeRate: Float = 1.0f
)

object CountryData {
    val countries = listOf(
        Country("FR", "France", "EUR", 0.92f),
        Country("US", "États-Unis", "USD", 1.0f),
        Country("GB", "Royaume-Uni", "GBP", 0.79f),
        Country("DE", "Allemagne", "EUR", 0.92f),
        Country("IT", "Italie", "EUR", 0.92f),
        Country("ES", "Espagne", "EUR", 0.92f),
        Country("PT", "Portugal", "EUR", 0.92f),
        Country("NL", "Pays-Bas", "EUR", 0.92f),
        Country("BE", "Belgique", "EUR", 0.92f),
        Country("CH", "Suisse", "CHF", 0.88f),
        Country("AT", "Autriche", "EUR", 0.92f),
        Country("SE", "Suède", "SEK", 9.5f),
        Country("NO", "Norvège", "NOK", 10.5f),
        Country("DK", "Danemark", "DKK", 6.85f),
        Country("FI", "Finlande", "EUR", 0.92f),
        Country("PL", "Pologne", "PLN", 4.0f),
        Country("CZ", "République Tchèque", "CZK", 23.5f),
        Country("HU", "Hongrie", "HUF", 370f),
        Country("RO", "Roumanie", "RON", 4.9f),
        Country("RU", "Russie", "RUB", 96f),
        Country("UA", "Ukraine", "UAH", 41f),
        Country("CN", "Chine", "CNY", 7.24f),
        Country("JP", "Japon", "JPY", 149f),
        Country("KR", "Corée du Sud", "KRW", 1310f),
        Country("IN", "Inde", "INR", 83f),
        Country("BR", "Brésil", "BRL", 4.97f),
        Country("MX", "Mexique", "MXN", 17.1f),
        Country("CA", "Canada", "CAD", 1.36f),
        Country("AU", "Australie", "AUD", 1.52f),
        Country("NZ", "Nouvelle-Zélande", "NZD", 1.66f),
        Country("ZA", "Afrique du Sud", "ZAR", 18.9f),
        Country("EG", "Égypte", "EGP", 30.9f),
        Country("NG", "Nigéria", "NGN", 770f),
        Country("KE", "Kenya", "KES", 131f),
        Country("AE", "Émirats Arabes Unis", "AED", 3.67f),
        Country("SA", "Arabie Saoudite", "SAR", 3.75f),
        Country("IL", "Israël", "ILS", 3.65f),
        Country("TH", "Thaïlande", "THB", 35.3f),
        Country("VN", "Vietnam", "VND", 24500f),
        Country("ID", "Indonésie", "IDR", 15600f),
        Country("PH", "Philippines", "PHP", 55.8f),
        Country("MY", "Malaisie", "MYR", 4.71f),
        Country("SG", "Singapour", "SGD", 1.34f),
        Country("TW", "Taïwan", "TWD", 31.5f),
        Country("HK", "Hong Kong", "HKD", 7.81f),
        Country("TR", "Turquie", "TRY", 31.8f),
        Country("GR", "Grèce", "EUR", 0.92f),
        Country("CY", "Chypre", "EUR", 0.92f),
        Country("MT", "Malte", "EUR", 0.92f),
    )

    fun getCountryByCodes(code: String): Country? {
        return countries.find { it.code == code }
    }

    fun getCountries(): List<String> {
        return countries.map { it.name }
    }

    fun getCurrencies(): List<String> {
        return countries.map { it.currency }.distinct()
    }
}