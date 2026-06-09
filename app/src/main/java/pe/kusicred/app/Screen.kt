package pe.kusicred.app

sealed class Screen(val route: String) {
    // Onboarding
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")

    // Admission
    object ChildRegistration : Screen("child_registration")
    object ChildSelector : Screen("child_selector")

    // Dashboard
    object Home : Screen("home/{childId}") {
        fun createRoute(childId: String) = "home/$childId"
    }

    // Growth
    object GrowthChart : Screen("growth_chart/{childId}") {
        fun createRoute(childId: String) = "growth_chart/$childId"
    }
    object AddGrowthRecord : Screen("add_growth/{childId}") {
        fun createRoute(childId: String) = "add_growth/$childId"
    }
    object GrowthHistory : Screen("growth_history/{childId}") {
        fun createRoute(childId: String) = "growth_history/$childId"
    }

    // Vaccines
    object VaccineCalendar : Screen("vaccine_calendar/{childId}") {
        fun createRoute(childId: String) = "vaccine_calendar/$childId"
    }
    object VaccineDetail : Screen("vaccine_detail/{vaccineRecordId}/{childId}") {
        fun createRoute(vaccineRecordId: String, childId: String) = "vaccine_detail/$vaccineRecordId/$childId"
    }
    object IronTracker : Screen("iron_tracker/{childId}") {
        fun createRoute(childId: String) = "iron_tracker/$childId"
    }

    // Milestones
    object MilestoneEvaluation : Screen("milestone_eval/{childId}") {
        fun createRoute(childId: String) = "milestone_eval/$childId"
    }
    object MilestoneDetail : Screen("milestone_detail/{milestoneId}/{childId}") {
        fun createRoute(milestoneId: String, childId: String) = "milestone_detail/$milestoneId/$childId"
    }
    object StimulationGuide : Screen("stimulation/{milestoneId}") {
        fun createRoute(milestoneId: String) = "stimulation/$milestoneId"
    }

    // Premium
    object PremiumPaywall : Screen("premium")
    object PdfExport : Screen("pdf_export/{childId}") {
        fun createRoute(childId: String) = "pdf_export/$childId"
    }
}
