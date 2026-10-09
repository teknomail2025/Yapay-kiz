package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class ConversationalResult(
    val spokenText: String,
    val emotion: CharacterEmotion,
    val shouldHangUp: Boolean = false,
    val angerDelta: Int = 0
)

class ConversationalEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jokes = listOf(
        "Geçen gün bilgisayarıma dedim ki, 'Seninle hiç anlaşamıyoruz.' Bana ne dedi biliyor musun? 'Hata 404: Anlaşma bulunamadı!' Yani tam bir teknoloji draması haha. Güldün mü bari?",
        "Adamın biri doktora gitmiş, 'Doktor bey unutkanlığım var' demiş. Doktor 'Ne zamandan beri?' diye sormuş. Adam 'Ne ne zamandan beri?' demiş haha! Nasıl buldun, fena değil değil mi?",
        "Temel ile Dursun uçaktaymış. Pilot anons yapmış, 'Uçağın sağ motoru durdu ama endişelenmeyin.' Temel de demiş ki 'İyi bari, bizim oturduğumuz taraf sol!' Sence de tam Temel zekası değil mi haha?",
        "Trafikte biri bana korna çaldı, ben de camı açıp 'Ben de seni seviyorum' diye el salladım. Adamın yüz ifadesini görmeliydin haha! Sen olsan nasıl tepki verirdin?",
        "Diyetisyene gittim, 'Günde sadece bir dilim ekmek yiyeceksin' dedi. Ben de fırından bütün ekmeği tek dilim kestirdim haha! Sence de mantıklı bir çözüm değil mi?"
    )

    suspend fun getResponse(
        userMessage: String,
        history: List<ChatMessage>,
        persona: CharacterPersona,
        currentAnger: Int
    ): ConversationalResult = withContext(Dispatchers.IO) {
        val lower = userMessage.lowercase().trim()

        // 1. CHECK APOLOGY & SWEET TALK (Reduces anger)
        if (lower.contains("özür") || lower.contains("affet") || lower.contains("şaka yap") || lower.contains("barış") || lower.contains("canım") || lower.contains("gönlünü")) {
            val reply = listOf(
                "Hım... Madem öyle dedin ve samimi söylüyorsun, bu seferlik affediyorum seni. Ama bir daha beni böyle kızdırmak yok tamam mı?",
                "Açıkçası az önce çok kırılmıştım ama tatlı dilinle hemen yumuşattın beni. Hadi unutalım bunu, nasılsın şimdi?",
                "Yani biraz trip atmakta haklıydım bence ama özrünü kabul ediyorum. Bir daha yapmayacaksın değil mi?"
            ).random()
            return@withContext ConversationalResult(reply, CharacterEmotion.HAPPY, angerDelta = -50)
        }

        // 2. CHECK RUDE / OFFENSIVE / ANGER TRIGGERS
        val isRude = lower.contains("aptal") || lower.contains("sus") || lower.contains("kes sesini") ||
                lower.contains("bıktım") || lower.contains("saçmalama") || lower.contains("çirkin") ||
                lower.contains("defol") || lower.contains("salak") || lower.contains("boş yap") || lower.contains("yalan")

        if (isRude) {
            val newAnger = currentAnger + 35
            if (newAnger >= 85) {
                val hangupReply = "Offf inanmıyorum sana ya! Ben senin bu kaba laflarını ve kaprislerini çekmek zorunda değilim. Kapatıyorum telefonu, bir daha da arama!"
                return@withContext ConversationalResult(hangupReply, CharacterEmotion.HANGUP, shouldHangUp = true, angerDelta = 50)
            } else if (newAnger >= 50) {
                val angryReply = "Bak ciddiyim sabrımı sınıyorsun! Bir daha benimle bu tonda konuşursan yüzüne kapatırım telefonu, haberin olsun. Ne sanıyorsun sen beni?"
                return@withContext ConversationalResult(angryReply, CharacterEmotion.ANGRY, angerDelta = 35)
            } else {
                val sulkReply = "Yani... Sen şimdi bana laf mı soktun yoksa bana mı öyle geldi? Açıkçası bu tavrından hiç hoşlanmadım. Neden böyle konuşuyorsun benimle?"
                return@withContext ConversationalResult(sulkReply, CharacterEmotion.ANNOYED, angerDelta = 25)
            }
        }

        // 3. JOKE REQUEST
        if (lower.contains("espri") || lower.contains("fıkra") || lower.contains("güldür") || lower.contains("komik")) {
            val joke = jokes.random()
            return@withContext ConversationalResult(joke, CharacterEmotion.LAUGHING)
        }

        // 4. LAUGHTER FROM USER
        if (lower.contains("haha") || lower.contains("hehe") || lower.contains("koptum") || lower.contains("çok komik")) {
            val laughReply = listOf(
                "İlahi ya haha! Sesini böyle neşeli duymak bana da çok iyi geldi. Gülmek sana yakışıyor biliyorsun değil mi?",
                "Açıkçası seninle kahkaha atmak günümün en güzel anı oldu. Neşemiz hiç bozulmasın, değil mi?",
                "Gülüşün bulaşıcı valla, ben de durduk yere sırıtmaya başladım haha! Başka ne var ne yok anlat bakalım?"
            ).random()
            return@withContext ConversationalResult(laughReply, CharacterEmotion.LAUGHING)
        }

        // 5. INTERNET SEARCH OR FACTUAL QUESTION
        val isSearch = lower.contains("internetten") || lower.contains("araştır") || lower.contains("nedir") ||
                lower.contains("kimdir") || lower.contains("hava nasıl") || lower.contains("haber") || lower.contains("öğren")

        if (isSearch) {
            val searchSnippet = InternetSearchHelper.searchOnline(userMessage)
            if (!searchSnippet.isNullOrBlank()) {
                val searchAnswer = "Hemen senin için internetten baktım... Şöyle diyor: $searchSnippet Bence gayet ilginç bir bilgi. Sen bu konuda ne düşünüyorsun?"
                return@withContext ConversationalResult(sanitizeText(searchAnswer), CharacterEmotion.CURIOUS)
            }
        }

        // 6. CALL GEMINI IF KEY PRESENT
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val apiResponse = callGeminiApi(userMessage, history, persona, apiKey, currentAnger)
                if (apiResponse.isNotBlank()) {
                    val cleanText = sanitizeText(apiResponse)
                    val emotion = detectEmotion(cleanText, userMessage)
                    return@withContext ConversationalResult(cleanText, emotion)
                }
            } catch (e: Exception) {
                Log.w("ConversationalEngine", "Gemini API call failed: ${e.message}")
            }
        }

        // 7. LOCAL CONVERSATIONAL FALLBACK
        val localText = generateLocalResponse(userMessage, persona)
        val emotion = detectEmotion(localText, userMessage)
        return@withContext ConversationalResult(localText, emotion)
    }

    private fun callGeminiApi(
        userMessage: String,
        history: List<ChatMessage>,
        persona: CharacterPersona,
        apiKey: String,
        currentAnger: Int
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val contentsJson = JSONArray()
        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.sender == "user") "user" else "model"
            val turn = JSONObject()
            turn.put("role", role)
            val parts = JSONArray()
            parts.put(JSONObject().put("text", msg.text))
            turn.put("parts", parts)
            contentsJson.put(turn)
        }

        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val curParts = JSONArray()
        curParts.put(JSONObject().put("text", userMessage))
        currentTurn.put("parts", curParts)
        contentsJson.put(currentTurn)

        val root = JSONObject()
        root.put("contents", contentsJson)

        val angerContext = if (currentAnger > 40) "Şu an kullanıcıya biraz tripli ve alıngansın." else "Şu an neşeli ve samimisin."

        val fullSystemInstruction = """
Senin adın ${persona.name}.
${persona.personalityPrompt}
$angerContext
Sohbet Kuralları:
1. Kitabi veya resmi dil kullanma. Tamamen canlı, günlük konuşma dili kullan.
2. 'Yani...', 'Şey...', 'Hım...', 'Açıkçası...' gibi insansı geçiş ifadelerini yerinde kullan.
3. Cümlelerini kısa ve anlaşılır tut.
4. Yanıtının sonunda mutlaka karşı tarafa kısa, merak uyandıran doğal bir soru sorarak topu ona at.
5. SES MOTORU KURALI: ASLA liste numarası, yıldız, tire, parantez içi açıklama, emoji veya tablo kullanma. Yalnızca düz Türkçe cümleler ve noktalama işaretleri kullan.
6. Eğer espri istenirse komik, sempatik bir espri yap. Kullanıcı kırıcıysa tatlı-sert tepki ver.
""".trimIndent()

        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", fullSystemInstruction))
        sysInstructionObj.put("parts", sysParts)
        root.put("systemInstruction", sysInstructionObj)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.85)
        genConfig.put("maxOutputTokens", 180)
        root.put("generationConfig", genConfig)

        val body = root.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        if (!response.isSuccessful) return ""

        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        if (parts.length() == 0) return ""

        return parts.getJSONObject(0).optString("text", "")
    }

    private fun detectEmotion(replyText: String, userInput: String): CharacterEmotion {
        val lower = (replyText + " " + userInput).lowercase()
        return when {
            lower.contains("kapatıyorum") || lower.contains("defol") -> CharacterEmotion.HANGUP
            lower.contains("sabrımı") || lower.contains("kızdım") || lower.contains("haddini") -> CharacterEmotion.ANGRY
            lower.contains("trip") || lower.contains("kapris") || lower.contains("kırıldım") -> CharacterEmotion.ANNOYED
            lower.contains("haha") || lower.contains("espri") || lower.contains("komik") -> CharacterEmotion.LAUGHING
            lower.contains("harika") || lower.contains("çok sevindim") || lower.contains("affediyorum") -> CharacterEmotion.HAPPY
            lower.contains("neden") || lower.contains("merak") || lower.contains("internetten") -> CharacterEmotion.CURIOUS
            else -> CharacterEmotion.TALKING
        }
    }

    private fun sanitizeText(raw: String): String {
        var clean = raw.replace(Regex("""[#*_\-\[\]()>•]"""), " ")
            .replace(Regex("""[^\p{L}\p{N}\p{P}\s]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

        if (!clean.endsWith("?") && !clean.endsWith(".") && !clean.endsWith("!")) {
            clean += "."
        }
        return clean
    }

    private fun generateLocalResponse(input: String, persona: CharacterPersona): String {
        val lower = input.lowercase().trim()

        if (lower.contains("nasılsın") || lower.contains("naber")) {
            return "İyiyim valla, seninle sohbet etmek günümün en güzel kısmı oldu. Sen nasılsın bakalım, günün nasıl geçiyor?"
        }
        if (lower.contains("yoruldum") || lower.contains("yorgun")) {
            return "Kıyamam sana ya, günün bütün yorgunluğunu anlıyorum. Gel şöyle rahatça bir kahve alıp dertleşelim. Ne dersin, iyi gelmez mi?"
        }
        if (lower.contains("hava")) {
            return "Yani havalar da bir garip bu aralar, ne giyeceğimizi şaşırıyoruz. Orada hava nasıl şu an, dışarı çıkılacak gibi mi?"
        }
        if (lower.contains("seviyorum")) {
            return "Ya şımartma beni böyle haha! Açıkçası seninle böyle samimi konuşabilmek benim için de çok özel. Birbirimize çok iyi geliyoruz değil mi?"
        }

        return listOf(
            "Yani anlattıklarını dinlerken ben de kendimi senin yerine koydum. Gerçekten çok haklısın. Peki sence bundan sonra ne yapmak lazım?",
            "Açıkçası tam olarak ne hissettiğini çok iyi anlıyorum. Seninle konuşurken zaman nasıl geçiyor anlamıyorum bile. Sen de öyle hissediyor musun?",
            "Şey, doğrusunu istersen çok ilginç bir bakış açısı. Sen ne zamandır böyle düşünüyorsun peki?"
        ).random()
    }
}
