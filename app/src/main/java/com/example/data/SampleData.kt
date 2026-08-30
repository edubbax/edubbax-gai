package com.example.data

import com.example.model.BatchStatus
import com.example.model.BloomLevel
import com.example.model.BloomStat
import com.example.model.DifficultQuestion
import com.example.model.DifficultyLevel
import com.example.model.FormativeInterventionEvent
import com.example.model.Lesson
import com.example.model.LessonStatus
import com.example.model.LiveQuestionStat
import com.example.model.LiveSession
import com.example.model.LiveSessionStatus
import com.example.model.LiveStudent
import com.example.model.NorthStarInterventionMetric
import com.example.model.ParentReportSummary
import com.example.model.QuestionType
import com.example.model.Quiz
import com.example.model.QuizBatch
import com.example.model.QuizOption
import com.example.model.QuizQuestion
import com.example.model.TeacherAnalytics
import com.example.model.TeacherProfile
import com.example.model.TopicMastery

object SampleData {

    val northStarMetric = NorthStarInterventionMetric(
        totalInterventions = 42,
        thisWeekInterventions = 8,
        topicMasteryGainPercent = 34,
        resolvedMisconceptionsCount = 38
    )

    val sampleLessons: List<Lesson> = listOf(
        Lesson(
            id = "les_1",
            title = "Nyuton Qanunları və Dinamika Əsasları",
            subject = "Fizika",
            gradeLevel = "9-cu sinif",
            topicDescription = "Ətalət prinsipi, təcilin qüvvə ilə əlaqəsi və təsir-əks təsir qanununun qrafik və konseptual tətbiqi.",
            questionFormat = QuestionType.MULTIPLE_CHOICE,
            durationMinutes = 60,
            numQuizzes = 3,
            status = LessonStatus.LIVE,
            uniqueLinkSlug = "edubbax.live/fiz-92",
            pinCode = "839 214",
            meaningfulInterventionsCount = 4,
            totalStudents = 26,
            averageScore = 76,
            createdAt = "Bugün, 10:30",
            batches = listOf(
                QuizBatch(
                    id = "b1",
                    sequenceNumber = 1,
                    scheduledOffsetMinutes = 20,
                    title = "Giriş: Ətalət və Nyutonun I Qanunu",
                    status = BatchStatus.COMPLETED,
                    averageAccuracy = 88,
                    totalSubmissions = 26,
                    questions = listOf(
                        QuizQuestion(
                            id = "b1_q1",
                            text = "Cismə təsir edən qüvvələrin əvəzləyicisi sıfıra bərabərdirsə, cisim hansı hərəkətdə olar?",
                            topicTag = "Nyuton I Qanunu",
                            bloomLevel = BloomLevel.UNDERSTAND,
                            explanation = "Əvəzləyici qüvvə sıfır olduqda cisim sükunətdə qalar və ya düzxətli bərabərsürətli hərəkət edər.",
                            options = listOf(
                                QuizOption("o1", "Yalnız sükunətdə qalar", false),
                                QuizOption("o2", "Bərabəryeyinləşən hərəkət edər", false),
                                QuizOption("o3", "Sükunətdə qalar və ya düzxətli bərabərsürətli hərəkət edər", true),
                                QuizOption("o4", "Çevrə üzrə fırlanar", false)
                            )
                        )
                    )
                ),
                QuizBatch(
                    id = "b2",
                    sequenceNumber = 2,
                    scheduledOffsetMinutes = 40,
                    title = "Tətbiq: Nyutonun II Qanunu və Təcil",
                    status = BatchStatus.ACTIVE,
                    averageAccuracy = 54,
                    totalSubmissions = 24,
                    questions = listOf(
                        QuizQuestion(
                            id = "b2_q1",
                            text = "Kütləsi 4 kq olan cismə 12 N qüvvə təsir edərsə, cismin aldığı təcil nə qədər olar?",
                            topicTag = "Dinamika / Təcil",
                            bloomLevel = BloomLevel.APPLY,
                            explanation = "a = F / m = 12 N / 4 kq = 3 m/s².",
                            options = listOf(
                                QuizOption("o1", "48 m/s²", false),
                                QuizOption("o2", "3 m/s²", true),
                                QuizOption("o3", "0.33 m/s²", false),
                                QuizOption("o4", "8 m/s²", false)
                            )
                        ),
                        QuizQuestion(
                            id = "b2_q2",
                            text = "Qüvvə 2 dəfə artdıqda, kütlə sabit qalarsa təcil necə dəyişər?",
                            topicTag = "Düz Mütənasiblik",
                            bloomLevel = BloomLevel.UNDERSTAND,
                            explanation = "Nyutonun II qanununa görə təcil qüvvə ilə düz mütənasibdir.",
                            options = listOf(
                                QuizOption("o1", "2 dəfə azalar", false),
                                QuizOption("o2", "Dəyişməz", false),
                                QuizOption("o3", "2 dəfə artar", true),
                                QuizOption("o4", "4 dəfə artar", false)
                            )
                        )
                    )
                ),
                QuizBatch(
                    id = "b3",
                    sequenceNumber = 3,
                    scheduledOffsetMinutes = 60,
                    title = "Yekun: Nyutonun III Qanunu və Real Situasiyalar",
                    status = BatchStatus.PENDING,
                    averageAccuracy = 0,
                    totalSubmissions = 0,
                    questions = listOf(
                        QuizQuestion(
                            id = "b3_q1",
                            text = "Ağır yük maşını kiçik minik avtomobili ilə toqquşduqda təsir və əks-təsir qüvvələri haqqında hansı fikir doğrudur?",
                            topicTag = "Nyuton III Qanunu",
                            bloomLevel = BloomLevel.ANALYZE,
                            explanation = "Nyutonun III qanununa görə təsir və əks-təsir qüvvələri həmişə modulca bərabər, istiqamətcə əksdir.",
                            options = listOf(
                                QuizOption("o1", "Yük maşınının tətbiq etdiyi qüvvə daha böyükdür", false),
                                QuizOption("o2", "Hər iki cismə təsir edən qüvvələr modulca bərabərdir", true),
                                QuizOption("o3", "Minik avtomobilinin qüvvəsi böyükdür", false),
                                QuizOption("o4", "Qüvvə sürətlərdən asılı olaraq təyin olunur", false)
                            )
                        )
                    )
                )
            )
        ),
        Lesson(
            id = "les_2",
            title = "DNT Quruluşu və Replikasiya Mexanizmi",
            subject = "Biologiya",
            gradeLevel = "10-cu sinif",
            topicDescription = "Nukleotidlər, komplementarlıq prinsipi və polimeraza fermentləri.",
            questionFormat = QuestionType.MULTIPLE_CHOICE,
            durationMinutes = 45,
            numQuizzes = 2,
            status = LessonStatus.SCHEDULED,
            uniqueLinkSlug = "edubbax.live/bio-44",
            pinCode = "512 904",
            meaningfulInterventionsCount = 2,
            totalStudents = 22,
            averageScore = 84,
            createdAt = "Sabah, 09:00",
            batches = emptyList()
        ),
        Lesson(
            id = "les_3",
            title = "Kvadratik Tənliklər və Viyet Teoremi",
            subject = "Riyaziyyat",
            gradeLevel = "8-ci sinif",
            topicDescription = "Diskriminant düsturu, köklərin cəmi və hasili ilə tənlik qurulması.",
            questionFormat = QuestionType.MULTIPLE_CHOICE,
            durationMinutes = 60,
            numQuizzes = 3,
            status = LessonStatus.COMPLETED,
            uniqueLinkSlug = "edubbax.live/riy-81",
            pinCode = "109 483",
            meaningfulInterventionsCount = 6,
            totalStudents = 28,
            averageScore = 81,
            createdAt = "Dünən",
            batches = emptyList()
        )
    )

    val sampleInterventions: List<FormativeInterventionEvent> = listOf(
        FormativeInterventionEvent(
            id = "int_1",
            batchId = "b2",
            topicTag = "Dinamika / Təcil Hesablanması",
            questionText = "Kütləsi 4 kq olan cismə 12 N qüvvə təsir edərsə...",
            errorRatePercent = 46,
            commonMisconception = "Şagirdlərin 35%-i qüvvə ilə kütləni vuraraq 48 m/s² seçib.",
            suggestedPrompt = "Lövhədə a = F/m düsturunu vahidlərlə (N/kq = m/s²) nümayiş etdirin.",
            isResolved = false
        ),
        FormativeInterventionEvent(
            id = "int_2",
            batchId = "b1",
            topicTag = "Nyuton I Qanunu / Ətalət",
            questionText = "Cismə təsir edən qüvvələrin əvəzləyicisi sıfıra bərabərdirsə...",
            errorRatePercent = 22,
            commonMisconception = "Düzxətli bərabərsürətli hərəkət halı unudulmuşdu.",
            suggestedPrompt = "Kosmosda hərəkət edən peyk nümunəsi verildi.",
            isResolved = true,
            resolvedNote = "Dərs daxilində izah edildi, şagirdlərin 96%-i təkrar sualda düzgün cavab verdi."
        )
    )

    val sampleQuizzes: List<Quiz> = listOf(
        Quiz(
            id = "q1",
            title = "DNT Quruluşu və Genetika Əsasları",
            subject = "Biologiya",
            gradeLevel = "10-cu sinif",
            description = "DNT replikasiyası, transkripsiya prosesi və genetik kodun translyasiyası üzrə dərinləşdirilmiş konseptual testlər.",
            difficulty = DifficultyLevel.MEDIUM,
            totalPlays = 142,
            averageScore = 82,
            createdAt = "Bugün",
            tags = listOf("Genetika", "Molekulyar", "DNT"),
            questions = listOf(
                QuizQuestion(
                    id = "q1_1",
                    text = "DNT molekulunda quanin nukleotidi ilə sitozin arasında neçə hidrogen rabitəsi mövcuddur?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    bloomLevel = BloomLevel.REMEMBER,
                    points = 10,
                    timeLimitSeconds = 25,
                    explanation = "Quanin və sitozin arasında 3 hidrogen rabitəsi, adenin və timin arasında isə 2 hidrogen rabitəsi yaranır.",
                    options = listOf(
                        QuizOption("o1", "1 hidrogen rabitəsi", false),
                        QuizOption("o2", "2 hidrogen rabitəsi", false),
                        QuizOption("o3", "3 hidrogen rabitəsi", true),
                        QuizOption("o4", "4 hidrogen rabitəsi", false)
                    )
                ),
                QuizQuestion(
                    id = "q1_2",
                    text = "Transkripsiya prosesi zamanı hansı ferment RNT zəncirinin sintezini həyata keçirir?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    bloomLevel = BloomLevel.UNDERSTAND,
                    points = 15,
                    timeLimitSeconds = 30,
                    explanation = "RNT-polimeraza fermenti DNT matrisi üzərində komplementarlıq prinsipi ilə RNT sintez edir.",
                    options = listOf(
                        QuizOption("o1", "DNT-polimeraza", false),
                        QuizOption("o2", "RNT-polimeraza", true),
                        QuizOption("o3", "DNT-helikaza", false),
                        QuizOption("o4", "Liqaza fermenti", false)
                    )
                ),
                QuizQuestion(
                    id = "q1_3",
                    text = "Əgər DNT zəncirində 1200 nukleotidin 30%-i timindirsə, quanin nukleotidlərinin sayını müəyyən edin.",
                    type = QuestionType.MULTIPLE_CHOICE,
                    bloomLevel = BloomLevel.APPLY,
                    points = 20,
                    timeLimitSeconds = 45,
                    explanation = "T=30% olarsa, A=30% olur (cəmi 60%). Q+S=40%, deməli Q=20%. 1200 nukleotidin 20%-i = 240 quanindir.",
                    options = listOf(
                        QuizOption("o1", "360", false),
                        QuizOption("o2", "240", true),
                        QuizOption("o3", "180", false),
                        QuizOption("o4", "480", false)
                    )
                ),
                QuizQuestion(
                    id = "q1_4",
                    text = "DNT-də replikasiya zamanı kəsilməz zəncir 5' -> 3' istiqamətində sintez olunur.",
                    type = QuestionType.TRUE_FALSE,
                    bloomLevel = BloomLevel.UNDERSTAND,
                    points = 10,
                    timeLimitSeconds = 20,
                    explanation = "Bəli, DNT-polimeraza yeni zənciri yalnız 5' -> 3' istiqamətində sintez edə bilir.",
                    options = listOf(
                        QuizOption("o1", "Doğru", true),
                        QuizOption("o2", "Yanlış", false)
                    )
                ),
                QuizQuestion(
                    id = "q1_5",
                    text = "Aşağıdakılardan hansı Okazaki fraqmentlərinin birləşməsini təmin edir?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    bloomLevel = BloomLevel.ANALYZE,
                    points = 20,
                    timeLimitSeconds = 30,
                    explanation = "DNT-liqaza geridə qalan zəncirdəki Okazaki parçalarını fosfodiefir rabitələri ilə birləşdirir.",
                    options = listOf(
                        QuizOption("o1", "Helikaza", false),
                        QuizOption("o2", "Topoizomeraza", false),
                        QuizOption("o3", "DNT-Liqaza", true),
                        QuizOption("o4", "Primaza", false)
                    )
                )
            )
        ),
        Quiz(
            id = "q2",
            title = "Azərbaycan Səfəvilər Dövləti və İntibah",
            subject = "Tarix",
            gradeLevel = "9-cu sinif",
            description = "Şah İsmayıl Xətainin hakimiyyət dövrü, Çaldıran döyüşü və Səfəvi diplomatiyası haqqında tənqidi təhlil sualları.",
            difficulty = DifficultyLevel.HARD,
            totalPlays = 98,
            averageScore = 74,
            createdAt = "Dünən",
            tags = listOf("Səfəvilər", "Orta əsrlər", "Azərbaycan Tarixi"),
            questions = listOf(
                QuizQuestion(
                    id = "q2_1",
                    text = "Səfəvilər dövlətinin əsası neçənci ildə və hansı hadisə ilə qoyulmuşdur?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    bloomLevel = BloomLevel.REMEMBER,
                    points = 10,
                    timeLimitSeconds = 25,
                    explanation = "1501-ci ildə Şərur döyüşündən sonra Təbrizdə Şah İsmayılın şah elan edilməsi ilə.",
                    options = listOf(
                        QuizOption("o1", "1500-cü il Cabanı döyüşü", false),
                        QuizOption("o2", "1501-ci il Təbrizin fəthi", true),
                        QuizOption("o3", "1514-cü il Çaldıran döyüşü", false),
                        QuizOption("o4", "1538-ci il Şirvanşahların süqutu", false)
                    )
                )
            )
        ),
        Quiz(
            id = "q3",
            title = "Triqonometriya və Vektorlar Alqoritmi",
            subject = "Riyaziyyat",
            gradeLevel = "11-ci sinif",
            description = "Sinus və kosinus teoremələri, skalyar hasil və fəzada koordinat sistemləri.",
            difficulty = DifficultyLevel.EXPERT,
            totalPlays = 210,
            averageScore = 68,
            createdAt = "3 gün əvvəl",
            tags = listOf("Həndəsə", "Triqonometriya", "Cəbr"),
            questions = listOf(
                QuizQuestion(
                    id = "q3_1",
                    text = "sin²(x) + cos²(x) bərabərliyi bütün həqiqi x ədədləri üçün hansı qiyməti alır?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    bloomLevel = BloomLevel.REMEMBER,
                    points = 10,
                    timeLimitSeconds = 20,
                    explanation = "Əsas triqonometrik eynilik hər zaman 1-ə bərabərdir.",
                    options = listOf(
                        QuizOption("o1", "0", false),
                        QuizOption("o2", "1", true),
                        QuizOption("o3", "2", false),
                        QuizOption("o4", "tg(x)", false)
                    )
                )
            )
        )
    )

    val initialLiveStudents: List<LiveStudent> = listOf(
        LiveStudent("s1", "Ayan Quliyeva", score = 3850, streak = 4, isAnswered = true, isCorrect = true, responseTimeMs = 1240),
        LiveStudent("s2", "Murad Əliyev", score = 3620, streak = 3, isAnswered = true, isCorrect = true, responseTimeMs = 1580),
        LiveStudent("s3", "Leyla Kərimova", score = 3410, streak = 3, isAnswered = true, isCorrect = false, responseTimeMs = 2100),
        LiveStudent("s4", "Rəşad Hüseynov", score = 3190, streak = 2, isAnswered = true, isCorrect = true, responseTimeMs = 1890),
        LiveStudent("s5", "Nərgiz Məmmədli", score = 2950, streak = 2, isAnswered = true, isCorrect = true, responseTimeMs = 2450),
        LiveStudent("s6", "Elvin Bağırov", score = 2800, streak = 1, isAnswered = false, isCorrect = false, responseTimeMs = 0),
        LiveStudent("s7", "Fidan Nəzərova", score = 2650, streak = 2, isAnswered = true, isCorrect = false, responseTimeMs = 3100),
        LiveStudent("s8", "Teymur Qasımov", score = 2400, streak = 1, isAnswered = false, isCorrect = false, responseTimeMs = 0)
    )

    val initialLiveSession: LiveSession = LiveSession(
        id = "ls_839214",
        pinCode = "839 214",
        quizTitle = "Nyuton Qanunları və Dinamika",
        subject = "Fizika • 9-cu sinif",
        currentQuestionIndex = 0,
        totalQuestions = 3,
        status = LiveSessionStatus.QUESTION_ACTIVE,
        connectedStudents = initialLiveStudents,
        currentTimerSeconds = 25,
        maxTimerSeconds = 30,
        questionStats = listOf(
            LiveQuestionStat(optionIndex = 0, count = 2, percentage = 0.14f, isCorrect = false),
            LiveQuestionStat(optionIndex = 1, count = 8, percentage = 0.57f, isCorrect = true),
            LiveQuestionStat(optionIndex = 2, count = 3, percentage = 0.21f, isCorrect = false),
            LiveQuestionStat(optionIndex = 3, count = 1, percentage = 0.08f, isCorrect = false)
        )
    )

    val teacherAnalytics: TeacherAnalytics = TeacherAnalytics(
        totalQuizzesCreated = 28,
        totalStudentsEngaged = 412,
        activeLiveSessionsCount = 19,
        overallAverageScore = 79,
        bloomStats = listOf(
            BloomStat(BloomLevel.REMEMBER, percentage = 0.88f, questionCount = 42, avgScore = 89),
            BloomStat(BloomLevel.UNDERSTAND, percentage = 0.81f, questionCount = 38, avgScore = 82),
            BloomStat(BloomLevel.APPLY, percentage = 0.74f, questionCount = 31, avgScore = 75),
            BloomStat(BloomLevel.ANALYZE, percentage = 0.65f, questionCount = 24, avgScore = 66),
            BloomStat(BloomLevel.EVALUATE, percentage = 0.58f, questionCount = 16, avgScore = 61),
            BloomStat(BloomLevel.CREATE, percentage = 0.52f, questionCount = 9, avgScore = 54)
        ),
        topicMasteries = listOf(
            TopicMastery("DNT və Nuklein turşuları", masteryPercent = 86, totalAttempts = 230, trendDelta = +6),
            TopicMastery("Mitoz və Meyoz bölünmə", masteryPercent = 79, totalAttempts = 190, trendDelta = +3),
            TopicMastery("Nyuton Qanunları və Təcil", masteryPercent = 64, totalAttempts = 310, trendDelta = +12),
            TopicMastery("Kvadratik Tənliklər və Diskriminant", masteryPercent = 78, totalAttempts = 145, trendDelta = +8)
        ),
        difficultQuestions = listOf(
            DifficultQuestion(
                questionText = "Kütləsi 4 kq olan cismə 12 N qüvvə təsir edərsə, təcili tapın",
                quizTitle = "Dinamika və Nyuton II Qanunu",
                errorRate = 46,
                commonWrongAnswer = "48 m/s² (Qüvvə ilə kütlə vurulub)"
            ),
            DifficultQuestion(
                questionText = "Okazaki parçalarını birləşdirən fosfodiefir rabitəsini quran ferment?",
                quizTitle = "DNT Replikasiyası",
                errorRate = 38,
                commonWrongAnswer = "DNT-polimeraza (Doğru: DNT-Liqaza)"
            )
        )
    )

    val sampleParentReport = ParentReportSummary(
        studentName = "Ayan Quliyeva",
        lessonTitle = "Fizika: Nyuton Qanunları və Dinamika",
        date = "30 Avqust 2026",
        participationRate = "100% (Bütün 3 batch tamamlandı)",
        understandingScore = "92% (Yüksək Anlama)",
        strongTopics = listOf("Nyuton I Qanunu", "Ətalət Prinsipi", "Dinamika və Təcil"),
        reviewTopics = listOf("Nyuton III Qanununda əks-təsir istiqaməti"),
        teacherAiNote = "Ayan dərsdə fəal iştirak etdi və intervallı sualların əksəriyyətini ilk cəhddən düzgün cavablandırdı. Formativ müdaxilə zamanı Nyuton III qanununun istiqamət prinsipini tam mənimsədi."
    )

    val teacherProfile: TeacherProfile = TeacherProfile()
}

