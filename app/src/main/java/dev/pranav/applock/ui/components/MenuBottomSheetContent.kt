package dev.pranav.applock.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.pranav.applock.core.navigation.Screen

@Composable
fun MenuBottomSheetContent(navController: NavController, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                buildAnnotatedString {
                    append("\"Over Half Of The University Students Experienced ")
                    withStyle(style = SpanStyle(color = Color(0xFF32CD32))) {
                        append("Blackouts")
                    }
                    append(" While Drunk\"")
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Drinking Knowledge",
                color = Color.White,
                fontSize = 14.sp
            )
        }
        HorizontalDivider(color = Color.Gray, thickness = 1.dp)

        Text(
            "Home",
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
            "App List",
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
                "Community",
                color = Color.Gray, // Disabled look
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text("Coming Soon", color = Color.Gray, fontSize = 14.sp)
        }
        Text(
            "Setting",
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
