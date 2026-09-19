package com.example.data

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

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun queryGemini(userPrompt: String, systemContext: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent local syllabus assistant fallback
            return@withContext generateLocalAssistantResponse(userPrompt)
        }

        try {
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            val fullPrompt = if (systemContext.isNotBlank()) {
                                "$systemContext\n\nUser request: $userPrompt"
                            } else {
                                "You are the Zahira College Mawanella ZSP-28 Science & AI Intelligence Assistant for Sri Lankan A/L English Medium students. Provide concise, accurate, inspiring academic help.\n\nUser request: $userPrompt"
                            }
                            put(JSONObject().put("text", fullPrompt))
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 600)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val jsonRes = JSONObject(responseBody)
                val candidates = jsonRes.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No response text received.")
                    }
                }
            }
            generateLocalAssistantResponse(userPrompt)
        } catch (e: Exception) {
            generateLocalAssistantResponse(userPrompt)
        }
    }

    private fun generateLocalAssistantResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("points") || lower.contains("sp") -> {
                "🌟 You can earn SP (Science Points) by ranking in the Top 10 in the Wednesday 7:30 PM - 10:00 PM quizzes! Head to the Redemption Center tab to redeem your SP for A/L past papers, lab manuals, and scientific calculators."
            }
            lower.contains("level") || lower.contains("milestone") -> {
                "🎖️ You level up as you complete quizzes correctly! Milestone rewards unlock every 5 levels (Level 5, 10, 15, etc.). Check your Profile tab to see your unlocked badges and claim milestone rewards."
            }
            lower.contains("quiz") || lower.contains("wednesday") -> {
                "📝 The synchronized A/L Quiz is scheduled every Wednesday night from 7:30 PM to 10:00 PM. Tap the 'Quiz' tab in the bottom bar to view all syllabus units and launch practice or live sessions."
            }
            lower.contains("math") || lower.contains("calculus") || lower.contains("differentiation") -> {
                "📐 For Sri Lankan A/L Combined Maths: Differentiation requires mastery of product rule, quotient rule, and chain rule. In the Statics and Dynamics units, always resolve coplanar forces along two perpendicular axes!"
            }
            lower.contains("physics") || lower.contains("mechanics") || lower.contains("waves") -> {
                "⚡ Sri Lankan A/L Physics tip: Pay close attention to SI units and sign conventions. For Doppler effect, f' = f(v ± v_o)/(v ∓ v_s). Always specify directions for vector quantities!"
            }
            lower.contains("chemistry") || lower.contains("equilibrium") || lower.contains("organic") -> {
                "🧪 Chemistry tip: According to Le Chatelier's principle, an equilibrium system shifts to counteract any external constraint. In Organic chemistry, practice reaction mechanisms and stability of carbocations!"
            }
            lower.contains("bio") || lower.contains("photosynthesis") || lower.contains("genetics") -> {
                "🌿 Biology tip: In Plant Form & Function, remember that light reactions in the thylakoid membrane generate ATP and NADPH, while the Calvin cycle in the stroma synthesizes G3P carbohydrates."
            }
            lower.contains("ict") || lower.contains("python") || lower.contains("network") -> {
                "💻 ICT tip: The OSI model has 7 layers: Physical, Data Link, Network, Transport, Session, Presentation, and Application. Remember TCP/IP is a 4-layer model."
            }
            lower.contains("admin") || lower.contains("verify") -> {
                "🛡️ As an Administrator, you can verify student accounts, monitor discussion channels, update redemption catalog items, and define milestone rewards in the Admin Dashboard."
            }
            else -> {
                "Greetings! I am the ZSP-28 Gemini Intelligence assistant for Zahira College Mawanella. I can answer questions on the Sri Lankan A/L syllabus (Combined Maths, Physics, Chemistry, Biology, ICT), help you practice quizzes, and guide you through the Discussion and Redemption centers."
            }
        }
    }
}
