package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object InternetSearchHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun searchOnline(query: String): String? = withContext(Dispatchers.IO) {
        val cleanQuery = query.replace(Regex("""(?i)(internetten ara|araştır|bana bilgi ver|nedir|kimdir|öğren|\?)"""), "").trim()
        val searchQuery = if (cleanQuery.isBlank()) query else cleanQuery

        // Try DuckDuckGo first
        val ddgResult = searchDuckDuckGo(searchQuery)
        if (!ddgResult.isNullOrBlank()) {
            return@withContext ddgResult
        }

        // Try Wikipedia
        val wikiResult = searchWikipedia(searchQuery)
        if (!wikiResult.isNullOrBlank()) {
            return@withContext wikiResult
        }

        return@withContext null
    }

    private fun searchDuckDuckGo(query: String): String? {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CanliSohbetAndroidApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)

            val abstract = json.optString("AbstractText", "")
            if (abstract.isNotBlank()) return abstract

            val heading = json.optString("Heading", "")
            val answer = json.optString("Answer", "")
            if (answer.isNotBlank()) return "$heading: $answer"

            val relatedTopics = json.optJSONArray("RelatedTopics")
            if (relatedTopics != null && relatedTopics.length() > 0) {
                val firstTopic = relatedTopics.optJSONObject(0)
                val text = firstTopic?.optString("Text", "")
                if (!text.isNullOrBlank()) return text
            }
            null
        } catch (e: Exception) {
            Log.w("InternetSearch", "DuckDuckGo search error: ${e.message}")
            null
        }
    }

    private fun searchWikipedia(query: String): String? {
        return try {
            val encoded = URLEncoder.encode(query.replace(" ", "_"), "UTF-8")
            val url = "https://tr.wikipedia.org/api/rest_v1/page/summary/$encoded"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CanliSohbetAndroidApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            val extract = json.optString("extract", "")
            if (extract.isNotBlank()) {
                // Shorten to first 2 sentences for TTS
                val sentences = extract.split(". ")
                return sentences.take(2).joinToString(". ") + "."
            }
            null
        } catch (e: Exception) {
            Log.w("InternetSearch", "Wikipedia search error: ${e.message}")
            null
        }
    }
}
