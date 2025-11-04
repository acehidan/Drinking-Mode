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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.ace.applock.R
import dev.ace.applock.core.navigation.Screen

@Composable
fun MenuBottomSheetContent(navController: NavController, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val quotesWithHighlights = remember {
        val quotes = context.resources.getStringArray(R.array.motivational_quotes)
        val highlights = context.resources.getStringArray(R.array.motivational_quotes_highlights)
        quotes.zip(highlights)
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
                text = stringResource(id = R.string.menu_subtitle),
                color = Color.White,
                fontSize = 14.sp
            )
        }
        HorizontalDivider(color = Color.Gray, thickness = 1.dp)

        Text(
            stringResource(id = R.string.menu_home),
            color = Color(0xFFF5F5DC), // Creamy white
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.clickable {
                onDismiss()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        Text(
            stringResource(id = R.string.menu_app_list),
            color = Color(0xFFF5F5DC),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
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
                stringResource(id = R.string.menu_community),
                color = Color.Gray, // Disabled look
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text(stringResource(id = R.string.menu_coming_soon), color = Color.Gray, fontSize = 14.sp)
        }
        Text(
            stringResource(id = R.string.menu_settings),
            color = Color(0xFFF5F5DC),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.clickable {
                onDismiss()
                navController.navigate(Screen.Settings.route)
            }
        )
    }
}
