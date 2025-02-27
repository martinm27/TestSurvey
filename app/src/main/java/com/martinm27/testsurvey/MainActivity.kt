package com.martinm27.testsurvey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.martinm27.testsurvey.ui.landing.LandingScreen
import com.martinm27.testsurvey.ui.survey.compose.SurveyScreen
import com.martinm27.testsurvey.ui.theme.TestSurveyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestSurveyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestSurveyApp(innerPadding)
                }
            }
        }
    }
}

@Composable
fun TestSurveyApp(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "landing") {
        composable("landing") {
            LandingScreen(
                modifier = Modifier.padding(innerPadding),
                navigateToQuestions = {
                    navController.navigate("survey")
                }
            )
        }

        composable("survey") {
            SurveyScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}