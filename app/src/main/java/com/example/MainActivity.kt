package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.model.Lesson
import com.example.model.Quiz
import com.example.ui.components.AppTopBar
import com.example.ui.components.CustomNavigationBar
import com.example.ui.screens.AiGeneratorScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BatchReviewScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LessonLobbyScreen
import com.example.ui.screens.LiveSessionHostScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizDetailScreen
import com.example.ui.screens.QuizLibraryScreen
import com.example.ui.screens.StudentInteractiveScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.EdubbaXTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.GeneratorViewModel
import com.example.ui.viewmodel.LiveSessionViewModel
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    private val generatorViewModel by viewModels<GeneratorViewModel>()
    private val liveSessionViewModel by viewModels<LiveSessionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Firebase init check", e)
        }
        enableEdgeToEdge()
        setContent {
            EdubbaXTheme {
                EdubbaXApp(
                    mainViewModel = mainViewModel,
                    generatorViewModel = generatorViewModel,
                    liveSessionViewModel = liveSessionViewModel
                )
            }
        }
    }
}

@Composable
fun EdubbaXApp(
    mainViewModel: MainViewModel,
    generatorViewModel: GeneratorViewModel,
    liveSessionViewModel: LiveSessionViewModel
) {
    val currentDestination by mainViewModel.currentDestination.collectAsState()
    val selectedQuizId by mainViewModel.selectedQuizId.collectAsState()
    val profile by mainViewModel.profile.collectAsState()
    val quizzes by mainViewModel.quizzes.collectAsState()
    val lessons by mainViewModel.lessons.collectAsState()
    val northStarMetric by mainViewModel.northStarMetric.collectAsState()

    var activeLobbyLesson by remember { mutableStateOf<Lesson?>(null) }
    var currentStudentName by remember { mutableStateOf("Ayan Quliyeva") }

    val selectedQuiz = selectedQuizId?.let { id -> quizzes.find { it.id == id } }

    val hideTopAndBottomBars = selectedQuiz != null ||
            currentDestination == AppDestination.LIVE_SESSION ||
            currentDestination == AppDestination.STUDENT_MODE ||
            currentDestination == AppDestination.LESSON_LOBBY ||
            currentDestination == AppDestination.REVIEW_BATCHES ||
            currentDestination == AppDestination.AUTH

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!hideTopAndBottomBars) {
                AppTopBar(
                    profile = profile,
                    onProfileClick = { mainViewModel.navigateTo(AppDestination.PROFILE) }
                )
            }
        },
        bottomBar = {
            if (!hideTopAndBottomBars) {
                CustomNavigationBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { dest ->
                        mainViewModel.navigateTo(dest)
                    }
                )
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (hideTopAndBottomBars) PaddingValues() else innerPadding)
                .background(BackgroundDark)
        ) {
            if (selectedQuiz != null) {
                QuizDetailScreen(
                    quiz = selectedQuiz,
                    onBack = { mainViewModel.closeQuizDetail() },
                    onLaunchLive = {
                        liveSessionViewModel.initializeWithQuiz(selectedQuiz)
                        mainViewModel.closeQuizDetail()
                        mainViewModel.navigateTo(AppDestination.LIVE_SESSION)
                    },
                    onDelete = {
                        mainViewModel.deleteQuiz(selectedQuiz.id)
                        mainViewModel.closeQuizDetail()
                    }
                )
            } else {
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { destination ->
                    when (destination) {
                        AppDestination.AUTH -> {
                            AuthScreen(
                                onTeacherLoginSuccess = { teacher ->
                                    mainViewModel.navigateTo(AppDestination.DASHBOARD)
                                },
                                onStudentPinJoin = { pin, name ->
                                    currentStudentName = if (name.isNotBlank()) name else "Şagird"
                                    liveSessionViewModel.setStudentIdentity("std_" + System.currentTimeMillis(), currentStudentName)
                                    mainViewModel.navigateTo(AppDestination.STUDENT_MODE)
                                }
                            )
                        }

                        AppDestination.DASHBOARD -> {
                            DashboardScreen(
                                profile = profile,
                                lessons = lessons,
                                northStarMetric = northStarMetric,
                                onNavigate = { dest -> mainViewModel.navigateTo(dest) },
                                onOpenLessonLobby = { lesson ->
                                    activeLobbyLesson = lesson
                                    mainViewModel.navigateTo(AppDestination.LESSON_LOBBY)
                                },
                                onStartLiveLesson = { lesson ->
                                    liveSessionViewModel.initializeWithLesson(lesson)
                                    mainViewModel.navigateTo(AppDestination.LIVE_SESSION)
                                },
                                onReviewLesson = { lesson ->
                                    activeLobbyLesson = lesson
                                    mainViewModel.navigateTo(AppDestination.REVIEW_BATCHES)
                                }
                            )
                        }

                        AppDestination.AI_GENERATOR -> {
                            AiGeneratorScreen(
                                viewModel = generatorViewModel,
                                onSaveToLibrary = { newQuiz ->
                                    mainViewModel.saveNewQuiz(newQuiz)
                                    mainViewModel.navigateTo(AppDestination.LIBRARY)
                                },
                                onLaunchLive = { newQuiz ->
                                    mainViewModel.saveNewQuiz(newQuiz)
                                    liveSessionViewModel.initializeWithQuiz(newQuiz)
                                    mainViewModel.navigateTo(AppDestination.LIVE_SESSION)
                                },
                                onReviewBatches = { generatedLesson ->
                                    mainViewModel.saveNewLesson(generatedLesson)
                                    activeLobbyLesson = generatedLesson
                                    mainViewModel.navigateTo(AppDestination.REVIEW_BATCHES)
                                }
                            )
                        }

                        AppDestination.REVIEW_BATCHES -> {
                            BatchReviewScreen(
                                generatorViewModel = generatorViewModel,
                                onSaveAndOpenLobby = { lesson ->
                                    generatorViewModel.saveLessonToFirestore(
                                        onSuccess = { savedLesson ->
                                            mainViewModel.saveNewLesson(savedLesson)
                                            activeLobbyLesson = savedLesson
                                            mainViewModel.navigateTo(AppDestination.LESSON_LOBBY)
                                        },
                                        onError = {
                                            mainViewModel.saveNewLesson(lesson)
                                            activeLobbyLesson = lesson
                                            mainViewModel.navigateTo(AppDestination.LESSON_LOBBY)
                                        }
                                    )
                                },
                                onBack = {
                                    mainViewModel.navigateTo(AppDestination.AI_GENERATOR)
                                }
                            )
                        }

                        AppDestination.LESSON_LOBBY -> {
                            val lesson = activeLobbyLesson ?: lessons.firstOrNull() ?: Lesson(
                                id = "les_default",
                                title = "Dinamika və Nyuton Qanunları",
                                subject = "Fizika",
                                gradeLevel = "9-cu sinif",
                                durationMinutes = 60
                            )
                            LessonLobbyScreen(
                                lesson = lesson,
                                onStartLiveSession = { l ->
                                    liveSessionViewModel.initializeWithLesson(l)
                                    mainViewModel.navigateTo(AppDestination.LIVE_SESSION)
                                },
                                onBack = {
                                    mainViewModel.navigateTo(AppDestination.DASHBOARD)
                                }
                            )
                        }

                        AppDestination.LIVE_SESSION -> {
                            LiveSessionHostScreen(
                                viewModel = liveSessionViewModel,
                                onFinishSession = {
                                    mainViewModel.navigateTo(AppDestination.DASHBOARD)
                                }
                            )
                        }

                        AppDestination.STUDENT_MODE -> {
                            StudentInteractiveScreen(
                                viewModel = liveSessionViewModel,
                                studentName = currentStudentName,
                                onExit = {
                                    mainViewModel.navigateTo(AppDestination.DASHBOARD)
                                }
                            )
                        }

                        AppDestination.LIBRARY -> {
                            QuizLibraryScreen(
                                quizzes = quizzes,
                                onQuizClick = { id -> mainViewModel.openQuizDetail(id) },
                                onLaunchLive = { quiz ->
                                    liveSessionViewModel.initializeWithQuiz(quiz)
                                    mainViewModel.navigateTo(AppDestination.LIVE_SESSION)
                                },
                                onToggleFavorite = { id -> mainViewModel.toggleFavorite(id) },
                                onAddNewQuiz = { mainViewModel.navigateTo(AppDestination.AI_GENERATOR) }
                            )
                        }

                        AppDestination.ANALYTICS -> {
                            AnalyticsScreen()
                        }

                        AppDestination.PROFILE -> {
                            ProfileScreen(
                                profile = profile,
                                onLogout = {
                                    mainViewModel.navigateTo(AppDestination.AUTH)
                                },
                                onStudentMode = {
                                    mainViewModel.navigateTo(AppDestination.STUDENT_MODE)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
