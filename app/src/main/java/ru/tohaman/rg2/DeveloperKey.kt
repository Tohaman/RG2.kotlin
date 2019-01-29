package ru.tohaman.rg2

/**
 * Static container class for holding a reference to your Developer Key.
 * need to add this key in your gradle.properties
 * YouTube_DEVELOPER_KEY = "ХХХХХХХХХХХХХХХХХХХХХХХХ"
 * RG2_Kotlin_GooglePlayKey = "XXXXXXXXXXXXXXXXXXXXXXX"
 * https://medium.com/code-better/hiding-api-keys-from-your-android-repository-b23f5598b906
 */
object DeveloperKey {

    /**
     * Please replace this with a valid API key which is enabled for the
     * YouTube Data API v3 service. Go to the
     * [Google Developers Console](https://console.developers.google.com/)
     * to register a new developer key.
     */
    val DEVELOPER_KEY = BuildConfig.YouTubeApiKey

    /**
     * открытый ключ RSA для приема оплаты внутри приложения
     */
    val base64EncodedPublicKey = BuildConfig.GooglePlayKey

}