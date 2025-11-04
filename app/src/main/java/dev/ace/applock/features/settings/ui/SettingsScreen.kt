package dev.ace.applock.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.ace.applock.R
import dev.ace.applock.core.navigation.Screen
import dev.ace.applock.ui.components.MenuBottomSheetContent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFFBEA), // Creamy background
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                shape = CircleShape,
                containerColor = Color.Black
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(id = R.string.settings_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SettingsCard(title = stringResource(id = R.string.settings_section_safety)) {
                SettingsRow(
                    icon = Icons.Default.Security,
                    text = stringResource(id = R.string.settings_item_change_pin),
                    onClick = { navController.navigate(Screen.ChangePassword.route) }
                )
                SettingsRow(
                    icon = Icons.Default.Vibration,
                    text = stringResource(id = R.string.settings_item_test_difficulty),
                    onClick = { navController.navigate(Screen.TestDifficulty.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsCard(title = stringResource(id = R.string.settings_section_account)) {
                SettingsRow(
                    icon = Icons.Default.Policy,
                    text = stringResource(id = R.string.settings_item_privacy_policy),
                    onClick = { /* TODO */ }
                )
                SettingsRow(
                    icon = Icons.Default.Policy, // Using same icon as placeholder
                    text = stringResource(id = R.string.settings_item_terms_conditions),
                    onClick = { /* TODO */ }
                )
                SettingsRow(
                    icon = Icons.Default.SupportAgent,
                    text = stringResource(id = R.string.settings_item_customer_support),
                    onClick = { navController.navigate(Screen.ContactSupport.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsCard(title = stringResource(id = R.string.settings_section_in_app)) {
                SettingsRow(
                    icon = Icons.Default.Language,
                    text = stringResource(id = R.string.settings_item_languages),
                    onClick = { navController.navigate(Screen.Language.route) }
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.Black,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            MenuBottomSheetContent(navController = navController) {
                showBottomSheet = false
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 16.sp)
        }
    }
}
