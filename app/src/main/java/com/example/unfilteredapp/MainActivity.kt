package com.example.unfilteredapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.example.unfilteredapp.data.repository.AuthRepository
import com.example.unfilteredapp.ui.screens.*
import com.example.unfilteredapp.ui.theme.UnfilteredAppTheme
import com.example.unfilteredapp.viewmodel.AuthState
import com.example.unfilteredapp.viewmodel.AuthViewModel
import com.example.unfilteredapp.viewmodel.MoodAnalyticsViewModel
import com.example.unfilteredapp.data.repository.MoodRepository
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import com.example.unfilteredapp.data.repository.JournalRepository
import com.example.unfilteredapp.viewmodel.JournalViewModel
import com.example.unfilteredapp.viewmodel.ChatViewModel
import com.example.unfilteredapp.data.repository.ChatRepository
import com.example.unfilteredapp.data.repository.SpotifyRepository
import com.example.unfilteredapp.viewmodel.MusicViewModel
import com.example.unfilteredapp.viewmodel.MoodViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnfilteredAppTheme {
                MainContainer()
            }
        }
    }
}

@Serializable sealed interface Screen {
    @Serializable data object Splash : Screen
    @Serializable data object Login : Screen
    @Serializable data object Signup : Screen
    @Serializable data object Journal : Screen
    @Serializable data object Music : Screen
    @Serializable data object MoodCategory : Screen
    @Serializable data class MoodSubSelection(val modeType: String) : Screen
    @Serializable data object MoodSummary : Screen
    @Serializable data object Rooms : Screen
    @Serializable data class Chat(val roomId: Int, val roomName: String, val moodTag: String, val roomDescription: String? = null) : Screen
    @Serializable data object Detox : Screen
    @Serializable data object Analytics : Screen
}

sealed class BottomNavScreen(val route: Screen, val icon: ImageVector, val label: String) {
    object Journal : BottomNavScreen(Screen.Journal, Icons.Default.AutoStories, "Journal")
    object Music : BottomNavScreen(Screen.Music, Icons.Default.MusicNote, "Music")
    object Mood : BottomNavScreen(Screen.MoodCategory, Icons.Default.Face, "Mood")
    object Rooms : BottomNavScreen(Screen.Rooms, Icons.Default.Forum, "Rooms")
    object Detox : BottomNavScreen(Screen.Detox, Icons.Default.Psychology, "Detox")
}

@Composable
fun MainContainer() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { AuthRepository(context) }
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
        }
    )

    val analyticsViewModel: MoodAnalyticsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MoodAnalyticsViewModel(MoodRepository(context)) as T
            }
        }
    )

    val journalViewModel: JournalViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return JournalViewModel(JournalRepository(context)) as T
            }
        }
    )

    val chatViewModel: ChatViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(com.example.unfilteredapp.data.repository.ChatRepository(context)) as T
            }
        }
    )

    val musicViewModel: MusicViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MusicViewModel(SpotifyRepository()) as T
            }
        }
    )

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate(Screen.Login) {
                popUpTo(0) { inclusive = true }
            }
            authViewModel.resetState()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val routeName = currentDestination?.route ?: ""
    val showNavigation = routeName.isNotEmpty() && 
                        !routeName.contains("Login") && 
                        !routeName.contains("Signup") &&
                        !routeName.contains("Chat") &&
                        !routeName.contains("Splash")

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showNavigation,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                CustomBottomAppBar(navController)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AppNavigation(navController, authViewModel, analyticsViewModel, journalViewModel, chatViewModel, musicViewModel)
        }
    }
}

@Composable
fun CustomBottomAppBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val items = listOf(
        BottomNavScreen.Journal,
        BottomNavScreen.Music,
        BottomNavScreen.Mood,
        BottomNavScreen.Rooms,
        BottomNavScreen.Detox
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 12.dp)
            .height(72.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                spotColor = MaterialTheme.colorScheme.primary
            ),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentDestination?.hierarchy?.any { 
                    it.route?.contains(item.route::class.simpleName ?: "") == true 
                } == true

                val animatedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.25f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun AppNavigation(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel,
    analyticsViewModel: MoodAnalyticsViewModel,
    journalViewModel: JournalViewModel,
    chatViewModel: ChatViewModel,
    musicViewModel: MusicViewModel
) {
    val moodViewModel: MoodViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        enterTransition = { 
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)
            )
        },
        exitTransition = { 
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)
            )
        },
        popEnterTransition = { 
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)
            )
        },
        popExitTransition = { 
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)
            )
        }
    ) {
        composable<Screen.Splash> {
            SplashScreen(onSplashFinished = {
                val destination = if (authViewModel.isLoggedIn()) Screen.MoodCategory else Screen.Login
                navController.navigate(destination) {
                    popUpTo(Screen.Splash) { inclusive = true }
                }
            })
        }
        composable<Screen.Login> {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignup = { navController.navigate(Screen.Signup) },
                onLoginSuccess = { 
                    navController.navigate(Screen.MoodCategory) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Signup> {
            SignupScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Screen.Login) },
                onSignupSuccess = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Signup) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.MoodCategory> {
            MoodCategoryScreen(
                onCategorySelected = { modeType ->
                    navController.navigate(Screen.MoodSubSelection(modeType))
                },
                onViewAnalytics = { navController.navigate(Screen.Analytics) },
                onLogout = { authViewModel.logout() },
                authViewModel = authViewModel
            )
        }
        composable<Screen.MoodSubSelection> { backStackEntry ->
            val subSelection: Screen.MoodSubSelection = backStackEntry.toRoute()
            MoodSubSelectionScreen(
                modeType = subSelection.modeType,
                onMoodSelected = { moodTag ->
                    moodViewModel.selectMood(moodTag)
                    
                    // Call the API to store the mood in the backend
                    val parts = moodTag.split(":")
                    if (parts.size == 2) {
                        analyticsViewModel.logMood(parts[0], parts[1])
                    }
                    
                    navController.navigate(Screen.MoodSummary)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Screen.MoodSummary> {
            MoodSummaryScreen(
                onBack = {
                    navController.navigate(Screen.MoodCategory) {
                        popUpTo(Screen.MoodCategory) { inclusive = true }
                    }
                },
                viewModel = moodViewModel,
                onNavigateToMusic = {
                    navController.navigate(Screen.Music) {
                        popUpTo(Screen.MoodCategory) { inclusive = false }
                    }
                },
                onNavigateToRooms = {
                    navController.navigate(Screen.Rooms) {
                        popUpTo(Screen.MoodCategory) { inclusive = false }
                    }
                },
                onNavigateToJournal = {
                    navController.navigate(Screen.Journal) {
                        popUpTo(Screen.MoodCategory) { inclusive = false }
                    }
                }
            )
        }
        composable<Screen.Journal> {
            JournalScreen(onBack = { navController.popBackStack() }, viewModel = journalViewModel)
        }
        composable<Screen.Music> {
            val selectedMood by moodViewModel.selectedMood.collectAsState()
            val finalMood = selectedMood?.split(":")?.getOrNull(1) ?: "Calm"
            MusicScreen(
                viewModel = musicViewModel,
                mood = finalMood,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable<Screen.Rooms> { 
            RoomsScreen(viewModel = chatViewModel, onRoomClick = { room ->
                navController.navigate(Screen.Chat(room.id, room.name, room.mood_tag, room.description))
            }) 
        }
        composable<Screen.Chat> { backStackEntry ->
            val chatRoute: Screen.Chat = backStackEntry.toRoute()
            
            ChatScreen(
                room = com.example.unfilteredapp.data.model.Room(
                    chatRoute.roomId, 
                    chatRoute.roomName, 
                    chatRoute.moodTag, 
                    chatRoute.roomDescription
                ),
                authViewModel = authViewModel,
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable<Screen.Detox> { 
            DetoxScreen(onBack = { navController.popBackStack() }) 
        }
        composable<Screen.Analytics> { 
            AnalyticsScreen(
                viewModel = analyticsViewModel,
                onBack = { navController.popBackStack() }
            ) 
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), 
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.HourglassEmpty, 
                contentDescription = null, 
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$name Screen", 
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
