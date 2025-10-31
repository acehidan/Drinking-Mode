package dev.ace.applock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.ace.applock.core.navigation.Screen
import dev.ace.applock.ui.theme.GothamFontFamily

@Composable
fun MenuBottomSheetContent(navController: NavController, onDismiss: () -> Unit) {
    val quotesWithHighlights = remember {
        listOf(
            "Over Half of the University students experienced Blackouts while drunk" to "Blackouts",
            "It's okay to not be okay. Reaching out to someone is a sign of Strength, Not Weakness" to "Strength, Not Weakness",
            "Sometimes we drink to feel connected. A real conversation, even a short one, Works Way Better" to "Works Way Better",
            "The confidence from a drink is just a rental. The Real Confidence Is Yours To Own" to "The Real Confidence Is Yours To Own",
            "Heavy drinking can mess with your deep sleep, which makes everything Feel Tougher The Next Day" to "Feel Tougher The Next Day",
            "A glass of water right now is a high-five to your future self. Seriously, They'll Thank You" to "They'll Thank You",
            "Your body processes about one drink per hour. Anything more is just Waiting In Line to be processed" to "Waiting In Line",
            "Drinking to forget something? Your brain is just hitting 'snooze' on the feeling, Not Deleting It" to "Not Deleting It",
            "Feeling down? Alcohol is a depressant, so it can actually make loneliness feel Heavier Tomorrow" to "Heavier Tomorrow",
            "A Quick Text To A Friend can give you a better, longer-lasting buzz than the next drink" to "A Quick Text To A Friend"
        )
    }
    val (randomQuote, highlightWord) = remember { quotesWithHighlights.random() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = buildAnnotatedString {
                    val parts = randomQuote.split(highlightWord, ignoreCase = true, limit = 2)
                    append("\"")
                    append(parts.getOrNull(0) ?: "")
                    withStyle(style = SpanStyle(color = Color(0xFF01A87B))) {
                        // Find the original casing of the highlight word
                        val startIndex = randomQuote.indexOf(highlightWord, ignoreCase = true)
                        if (startIndex != -1) {
                            append(randomQuote.substring(startIndex, startIndex + highlightWord.length))
                        }
                    }
                    append(parts.getOrNull(1) ?: "")
                    append("\"")
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 40.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Drinking Knowledge",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
        HorizontalDivider(color = Color.Gray, thickness = 1.dp)

        Text(
            "Home",
            color = Color(0xFFF5F5DC), // Creamy white
            fontWeight = FontWeight.Bold,
            fontFamily = GothamFontFamily,
            fontSize = 24.sp,
            modifier = Modifier.clickable {
                onDismiss()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        Text(
            "App List",
            color = Color(0xFFF5F5DC),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.clickable {
                onDismiss()
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        Column {
            Text(
                "Community",
                color = Color.Gray, // Disabled look
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            Text("Coming Soon", color = Color.Gray, fontSize = 14.sp)
        }
        Text(
            "Setting",
            color = Color(0xFFF5F5DC),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.clickable {
                onDismiss()
                navController.navigate(Screen.Settings.route)
            }
        )
    }
}
