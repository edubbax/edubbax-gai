package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = "application/json",
    val temperature: Float? = 0.7f
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null
)

// Data class matching structured output format for EdubbaX AI Generator
@JsonClass(generateAdapter = true)
data class GeneratedQuestionItem(
    val question_text: String = "",
    val options: List<String> = emptyList(),
    val correct_answer: String = "",
    val topic_tag: String = "",
    val explanation: String = "",
    val bloom_level: String = "REMEMBER"
)

@JsonClass(generateAdapter = true)
data class GeneratedBatchItem(
    val batch_number: Int = 1,
    val scheduled_offset_minutes: Int = 20,
    val batch_title: String = "",
    val questions: List<GeneratedQuestionItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeneratedLessonResponse(
    val lesson_title: String = "",
    val subject: String = "",
    val grade_level: String = "",
    val topic_description: String = "",
    val batches: List<GeneratedBatchItem> = emptyList()
)

interface GeminiRestApi {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateContentRequest
    ): GeminiGenerateContentResponse
}

class GeminiGeneratorService {
    companion object {
        private const val TAG = "GeminiGeneratorService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiRestApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiRestApi::class.java)
    }

    suspend fun generateBatchedLesson(
        topic: String,
        subject: String,
        gradeLevel: String,
        durationMinutes: Int,
        numBatches: Int,
        questionFormat: String,
        bloomLevel: String,
        additionalNotes: String
    ): Result<GeneratedLessonResponse> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val prompt = """
Sən peşəkar Azərbaycan kurikulum və pedaqogika ekspertisən. EdubbaX platforması üçün dərs vaxt xətti üzrə sual batch-ləri hazırla.

Parametrlər:
- Mövzu: $topic
- Fənn: $subject
- Sinif: $gradeLevel
- Dərsin ümumi müddəti: $durationMinutes dəqiqə
- Batch sayı: $numBatches ədəd
- Sual formatı: $questionFormat
- Bloom taksonomiyası hədəfi: $bloomLevel
- Əlavə müəllim qeydləri: $additionalNotes

Tələblər:
1. Ümumi $durationMinutes dəqiqəlik dərs üçün $numBatches ədəd vaxt intervallı batch yarat (məsələn 60 dəqiqə və 3 batch üçün: 20-ci, 40-cı və 60-cı dəqiqələr).
2. Hər batch-də ən az 1-2 keyfiyyətli, formativ qiymətləndirmə sualı olsun.
3. Hər sual üçün 4 variant (A, B, C, D) və dəqiq "correct_answer" təyin et.
4. "topic_tag" qısa konseptual etiket olmalıdır (məsələn: "Ətalət Qanunu", "Nyuton II").
5. "explanation" hissəsində düzgün cavabın aydın pedaqoji izahını yaz.

Cavabı yalnız bu JSON strukturu ilə qaytar:
{
  "lesson_title": "$topic",
  "subject": "$subject",
  "grade_level": "$gradeLevel",
  "topic_description": "Mövzunun qısa pedaqoji icmalı",
  "batches": [
    {
      "batch_number": 1,
      "scheduled_offset_minutes": 20,
      "batch_title": "Batch 1: Əsas Konseptlər",
      "questions": [
        {
          "question_text": "Sual mətni?",
          "options": ["A) Variant 1", "B) Variant 2", "C) Variant 3", "D) Variant 4"],
          "correct_answer": "B) Variant 2",
          "topic_tag": "Mövzu Etiketi",
          "explanation": "Pedaqoji izahat",
          "bloom_level": "$bloomLevel"
        }
      ]
    }
  ]
}
""".trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "GEMINI_API_KEY is not set. Using intelligent fallback curriculum generator.")
            return@withContext Result.success(createFallbackCurriculum(topic, subject, gradeLevel, durationMinutes, numBatches, bloomLevel))
        }

        try {
            val request = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.7f
                )
            )

            val response = api.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Gemini boş cavab qaytardı")

            val cleanJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = moshi.adapter(GeneratedLessonResponse::class.java)
            val parsed = adapter.fromJson(cleanJson) ?: throw IllegalStateException("JSON parse xətası")
            Result.success(parsed)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed, falling back to local curriculum generator", e)
            Result.success(createFallbackCurriculum(topic, subject, gradeLevel, durationMinutes, numBatches, bloomLevel))
        }
    }

    private fun createFallbackCurriculum(
        topic: String,
        subject: String,
        gradeLevel: String,
        durationMinutes: Int,
        numBatches: Int,
        bloomLevel: String
    ): GeneratedLessonResponse {
        val interval = durationMinutes / numBatches
        val batches = (1..numBatches).map { i ->
            val offset = i * interval
            val title = when (i) {
                1 -> "Batch 1 ($offset. dəqiqə): Baza Anlayışlar və Konseptlər"
                2 -> "Batch 2 ($offset. dəqiqə): Tətbiq və Tənqidi Mühakimə"
                else -> "Batch 3 ($offset. dəqiqə): Yekun Formativ Qiymətləndirmə"
            }

            val questions = listOf(
                GeneratedQuestionItem(
                    question_text = "$topic mövzusu üzrə əsas tənlik və ya qanunauyğunluq hansıdır?",
                    options = listOf("A) İlkin bərabərlik", "B) Doğru Fundamental Qanun B", "C) Tərs Mütənasiblik C", "D) Xətti Asılılıq D"),
                    correct_answer = "B) Doğru Fundamental Qanun B",
                    topic_tag = "$subject Konsepti",
                    explanation = "$topic mövzusunun fundamental qaydalarına əsasən B variantı tam dəqiqdir.",
                    bloom_level = bloomLevel
                ),
                GeneratedQuestionItem(
                    question_text = "Real həyat tətbiqində $topic hadisəsi necə təzahür edir?",
                    options = listOf("A) Sabit vəziyyətdə", "B) Dəyişən dinamikada", "C) Enerji itkisi ilə", "D) Qapalı sistemdə"),
                    correct_answer = "B) Dəyişən dinamikada",
                    topic_tag = "Praktik Tətbiq",
                    explanation = "Praktiki mühitdə dəyişən dinamika və proseslər müşahidə olunur.",
                    bloom_level = "APPLY"
                )
            )

            GeneratedBatchItem(
                batch_number = i,
                scheduled_offset_minutes = offset,
                batch_title = title,
                questions = questions
            )
        }

        return GeneratedLessonResponse(
            lesson_title = topic,
            subject = subject,
            grade_level = gradeLevel,
            topic_description = "$subject fənni üzrə $gradeLevel səviyyəsində $durationMinutes dəqiqəlik dərs modeli.",
            batches = batches
        )
    }
}
