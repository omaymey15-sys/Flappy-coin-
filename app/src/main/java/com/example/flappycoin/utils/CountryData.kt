package com.example.flappycoin.utils

/**
 * Base de données des pays et devises
 * 45+ pays avec taux de change en temps réel (simulé)
 */

data class Country(
    val code: String,           // Code ISO 2 caractères (FR, US, etc)
    val name: String,           // Nom pays
    val currency: String,       // Code devise (EUR, USD, etc)
    val exchangeRate: Float = 1.0f  // Taux vs USD (1 USD = X devises)
)

object CountryData {
    val countries = listOf(
        // Europe
        Country("FR", "France", "EUR", 0.92f),
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
        Country("GR", "Grèce", "EUR", 0.92f),
        Country("GB", "Royaume-Uni", "GBP", 0.79f),

        // Amérique du Nord
        Country("US", "États-Unis", "USD", 1.0f),
        Country("CA", "Canada", "CAD", 1.36f),
        Country("MX", "Mexique", "MXN", 17.1f),

        // Amérique du Sud
        Country("BR", "Brésil", "BRL", 4.97f),
        Country("AR", "Argentine", "ARS", 820f),
        Country("CL", "Chili", "CLP", 850f),
        Country("CO", "Colombie", "COP", 4100f),

        // Asie
        Country("CN", "Chine", "CNY", 7.24f),
        Country("JP", "Japon", "JPY", 149f),
        Country("KR", "Corée du Sud", "KRW", 1310f),
        Country("IN", "Inde", "INR", 83f),
        Country("TH", "Thaïlande", "THB", 35.3f),
        Country("VN", "Vietnam", "VND", 24500f),
        Country("ID", "Indonésie", "IDR", 15600f),
        Country("PH", "Philippines", "PHP", 55.8f),
        Country("MY", "Malaisie", "MYR", 4.71f),
        Country("SG", "Singapour", "SGD", 1.34f),
        Country("TW", "Taïwan", "TWD", 31.5f),
        Country("HK", "Hong Kong", "HKD", 7.81f),

        // Moyen-Orient & Afrique
        Country("AE", "Émirats Arabes Unis", "AED", 3.67f),
        Country("SA", "Arabie Saoudite", "SAR", 3.75f),
        Country("IL", "Israël", "ILS", 3.65f),
        Country("TR", "Turquie", "TRY", 31.8f),
        Country("EG", "Égypte", "EGP", 30.9f),
        Country("NG", "Nigéria", "NGN", 770f),
        Country("KE", "Kenya", "KES", 131f),
        Country("ZA", "Afrique du Sud", "ZAR", 18.9f),

        // Océanie
        Country("AU", "Australie", "AUD", 1.52f),
        Country("NZ", "Nouvelle-Zélande", "NZD", 1.66f),

        // Russie & CIS
        Country("RU", "Russie", "RUB", 96f),
        Country("UA", "Ukraine", "UAH", 41f),
    )

    fun getCountryByCode(code: String): Country? {
        return countries.find { it.code == code }
    }

    fun getCountryNames(): List<String> {
        return countries.map { it.name }
    }

    fun getCurrencies(): List<String> {
        return countries.map { it.currency }.distinct().sorted()
    }

    fun getCountryByName(name: String): Country? {
        return countries.find { it.name == name }
    }
}