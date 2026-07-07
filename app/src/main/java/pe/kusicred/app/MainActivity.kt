package pe.kusicred.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import pe.kusicred.app.features.admission.ui.screen.ChildRegistrationScreen
import pe.kusicred.app.features.admission.ui.screen.ChildSelectorScreen
import pe.kusicred.app.features.dashboard.ui.screen.HomeScreen
import pe.kusicred.app.features.growth.ui.screen.AddGrowthRecordScreen
import pe.kusicred.app.features.growth.ui.screen.GrowthChartScreen
import pe.kusicred.app.features.growth.ui.screen.GrowthHistoryScreen
import pe.kusicred.app.features.milestones.ui.screen.MilestoneDetailScreen
import pe.kusicred.app.features.milestones.ui.screen.MilestoneEvaluationScreen
import pe.kusicred.app.features.milestones.ui.screen.StimulationGuideScreen
import pe.kusicred.app.features.onboarding.ui.SplashScreen
import pe.kusicred.app.features.onboarding.ui.WelcomeScreen
import pe.kusicred.app.features.auth.ui.screen.LoginScreen
import pe.kusicred.app.features.auth.ui.screen.SignupScreen
import pe.kusicred.app.features.premium.ui.screen.PdfExportScreen
import pe.kusicred.app.features.premium.ui.screen.PremiumPaywallScreen
import pe.kusicred.app.features.vaccines.ui.screen.IronTrackerScreen
import pe.kusicred.app.features.vaccines.ui.screen.VaccineCalendarScreen
import pe.kusicred.app.features.vaccines.ui.screen.VaccineDetailScreen
import pe.kusicred.app.ui.theme.KUSICREDTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KUSICREDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KusiCREDNavGraph()
                }
            }
        }
    }
}

@Composable
fun KusiCREDNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ---- Onboarding ----
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = { isLoggedIn ->
                    val destination = if (isLoggedIn) Screen.ChildSelector.route else Screen.Welcome.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            val authViewModel: pe.kusicred.app.features.auth.ui.screen.AuthViewModel = hiltViewModel()
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate("login") {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onGuestLogin = {
                    authViewModel.loginAsGuest {
                        navController.navigate(Screen.ChildSelector.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ChildSelector.route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Screen.ChildSelector.route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // ---- Admission ----
        composable(Screen.ChildSelector.route) {
            val authViewModel: pe.kusicred.app.features.auth.ui.screen.AuthViewModel = hiltViewModel()
            ChildSelectorScreen(
                onChildSelected = { childId ->
                    navController.navigate(Screen.Home.createRoute(childId)) {
                        popUpTo(Screen.ChildSelector.route) { inclusive = false }
                    }
                },
                onAddNewChild = {
                    navController.navigate(Screen.ChildRegistration.route)
                },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0)
                        }
                    }
                }
            )
        }

        composable(Screen.ChildRegistration.route) {
            ChildRegistrationScreen(
                onRegistrationComplete = { childId ->
                    navController.navigate(Screen.Home.createRoute(childId)) {
                        popUpTo(Screen.ChildSelector.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Dashboard ----
        composable(
            Screen.Home.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStack ->
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            HomeScreen(
                childId = childId,
                onNavigateToGrowthChart = { navController.navigate(Screen.GrowthChart.createRoute(childId)) },
                onNavigateToAddGrowth = { navController.navigate(Screen.AddGrowthRecord.createRoute(childId)) },
                onNavigateToVaccines = { navController.navigate(Screen.VaccineCalendar.createRoute(childId)) },
                onNavigateToMilestones = { navController.navigate(Screen.MilestoneEvaluation.createRoute(childId)) },
                onNavigateToIron = { navController.navigate(Screen.IronTracker.createRoute(childId)) },
                onNavigateToPremium = { navController.navigate(Screen.PremiumPaywall.route) },
                onNavigateToPdfExport = { navController.navigate(Screen.PdfExport.createRoute(childId)) },
                onChangeChild = {
                    navController.navigate(Screen.ChildSelector.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ---- Growth ----
        composable(
            Screen.GrowthChart.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStack ->
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            GrowthChartScreen(
                childId = childId,
                onBack = { navController.popBackStack() },
                onAddRecord = { navController.navigate(Screen.AddGrowthRecord.createRoute(childId)) },
                onViewHistory = { navController.navigate(Screen.GrowthHistory.createRoute(childId)) }
            )
        }

        composable(
            Screen.AddGrowthRecord.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStack ->
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            AddGrowthRecordScreen(
                childId = childId,
                onRecordSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.GrowthHistory.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStack ->
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            GrowthHistoryScreen(
                childId = childId,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Vaccines ----
        composable(
            Screen.VaccineCalendar.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStack ->
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            VaccineCalendarScreen(
                childId = childId,
                onBack = { navController.popBackStack() },
                onVaccineClick = { recordId ->
                    navController.navigate(Screen.VaccineDetail.createRoute(recordId, childId))
                },
                onIronTracker = { navController.navigate(Screen.IronTracker.createRoute(childId)) }
            )
        }

        composable(
            Screen.VaccineDetail.route,
            arguments = listOf(
                navArgument("vaccineRecordId") { type = NavType.StringType },
                navArgument("childId") { type = NavType.StringType }
            )
        ) { backStack ->
            val recordId = backStack.arguments?.getString("vaccineRecordId") ?: return@composable
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            VaccineDetailScreen(
                vaccineRecordId = recordId,
                childId = childId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.IronTracker.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStack ->
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            IronTrackerScreen(
                childId = childId,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Milestones ----
        composable(
            Screen.MilestoneEvaluation.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStack ->
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            MilestoneEvaluationScreen(
                childId = childId,
                onBack = { navController.popBackStack() },
                onMilestoneDetail = { milestoneId ->
                    navController.navigate(Screen.MilestoneDetail.createRoute(milestoneId, childId))
                },
                onStimulation = { milestoneId ->
                    navController.navigate(Screen.StimulationGuide.createRoute(milestoneId))
                }
            )
        }

        composable(
            Screen.MilestoneDetail.route,
            arguments = listOf(
                navArgument("milestoneId") { type = NavType.StringType },
                navArgument("childId") { type = NavType.StringType }
            )
        ) { backStack ->
            val milestoneId = backStack.arguments?.getString("milestoneId") ?: return@composable
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            MilestoneDetailScreen(
                milestoneId = milestoneId,
                childId = childId,
                onBack = { navController.popBackStack() },
                onStimulation = { navController.navigate(Screen.StimulationGuide.createRoute(milestoneId)) }
            )
        }

        composable(
            Screen.StimulationGuide.route,
            arguments = listOf(navArgument("milestoneId") { type = NavType.StringType })
        ) { backStack ->
            val milestoneId = backStack.arguments?.getString("milestoneId") ?: return@composable
            StimulationGuideScreen(
                milestoneId = milestoneId,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Premium ----
        composable(Screen.PremiumPaywall.route) {
            PremiumPaywallScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Screen.PdfExport.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStack ->
            val childId = backStack.arguments?.getString("childId") ?: return@composable
            PdfExportScreen(
                childId = childId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
