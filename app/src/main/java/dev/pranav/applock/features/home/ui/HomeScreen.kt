package dev.pranav.applock.features.home.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.pranav.applock.R
import dev.pranav.applock.core.navigation.Screen
import dev.pranav.applock.core.utils.appLockRepository
import dev.pranav.applock.core.utils.isAccessibilityServiceEnabled
import dev.pranav.applock.core.utils.openAccessibilitySettings
import dev.pranav.applock.data.model.LockType
import dev.pranav.applock.data.repository.BackendImplementation
import dev.pranav.applock.ui.components.AccessibilityServiceGuideDialog
import dev.pranav.applock.ui.components.MenuBottomSheetContent
import dev.pranav.applock.ui.theme.GothamFontFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val appLockRepository = context.appLockRepository()
    var isAppLockEnabled by remember { mutableStateOf(appLockRepository.isProtectEnabled()) }
    var currentLockType by remember { mutableStateOf(appLockRepository.getLockType()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    // This BackHandler will exit the app when the back button is pressed on the HomeScreen
    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(Unit) {
        if (appLockRepository.getBackendImplementation() == BackendImplementation.ACCESSIBILITY && !context.isAccessibilityServiceEnabled()) {
            showAccessibilityDialog = true
        }
    }

    if (isAppLockEnabled) {
        when (currentLockType) {
            LockType.PIN -> {
                ActivatedScreen(
                    onDeactivate = {
                        isAppLockEnabled = false
                        appLockRepository.setProtectEnabled(false)
                    }
                )
            }
            LockType.TYPING_GAME -> {
                SeriousModeActivatedScreen(
                    onDeactivate = {
                        isAppLockEnabled = false
                        appLockRepository.setProtectEnabled(false)
                    }
                )
            }
        }
    } else {
        when (currentLockType) {
            LockType.PIN -> {
                CasualScreen(
                    onActivate = {
                        isAppLockEnabled = true
                        appLockRepository.setProtectEnabled(true)
                    },
                    onShowMenu = { showBottomSheet = true },
                    onSwitchLockType = {
                        appLockRepository.setLockType(LockType.TYPING_GAME)
                        currentLockType = LockType.TYPING_GAME
                        Toast.makeText(context, "Switched to Serious Mode", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            LockType.TYPING_GAME -> {
                SeriousModeScreen(
                    onActivate = {
                        isAppLockEnabled = true
                        appLockRepository.setProtectEnabled(true)
                    },
                    onShowMenu = { showBottomSheet = true },
                    onSwitchLockType = {
                        appLockRepository.setLockType(LockType.PIN)
                        currentLockType = LockType.PIN
                        Toast.makeText(context, "Switched to Casual Mode", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    if (showAccessibilityDialog) {
        AccessibilityServiceGuideDialog(
            onOpenSettings = {
                openAccessibilitySettings(context)
                showAccessibilityDialog = false
            },
            onDismiss = {
                showAccessibilityDialog = false
            }
        )
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
private fun CasualScreen(
    onActivate: () -> Unit,
    onShowMenu: () -> Unit,
    onSwitchLockType: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar from original CasualScreen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Drinking Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01AB7B))
            ) {
                Text(text = "Get Consult")
            }
        }
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "This is Casual Mode.",
            color = Color(0xFF32CD32),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = GothamFontFamily
        )
         Image(
             painter = painterResource(id = R.drawable.casual_mode_illustration),
             contentDescription = "Casual Mode Illustration",
             modifier = Modifier.size(400.dp)
         )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Party? Celebrate with your loved ones..",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = TextAlign.Center,
            fontFamily = GothamFontFamily,
            lineHeight = 32.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom Bar from original CasualScreen
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onActivate,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A86B)),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
            )
            {
                Icon(
                    painter = painterResource(id = R.drawable.nightlife),
                    contentDescription = "Activate",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Activate", color = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onSwitchLockType,
                modifier = Modifier
                    .size(56.dp)
                    .border(BorderStroke(1.dp, Color.Black), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Switch Mode",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onShowMenu,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Black, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.weight(0.2f))
    }
}

@Composable
private fun SeriousModeScreen(
    onActivate: () -> Unit,
    onShowMenu: () -> Unit,
    onSwitchLockType: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBEA)) // Creamy background from image
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Drinking Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF06262))
            ) {
                Text(text = "Get Consult")
            }
        }
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "This is Serious Mode.",
            color = Color(0xFFD32F2F), // Red color from image
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = GothamFontFamily
        )
        Spacer(modifier = Modifier.height(16.dp))
         Image(
             painter = painterResource(id = R.drawable.hangover_cuate),
             contentDescription = "Serious Mode Illustration",
             modifier = Modifier.size(400.dp)
         )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Are You Going To\nDrink Heavily Alone?",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = TextAlign.Center,
            fontFamily = GothamFontFamily,
            lineHeight = 32.sp
        )
        Spacer(modifier = Modifier.weight(1f))

        // Bottom Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onActivate,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF06262)),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.nightlife),
                    contentDescription = "Activate",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Activate", color = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onSwitchLockType,
                modifier = Modifier
                    .size(56.dp)
                    .border(BorderStroke(1.dp, Color.Black), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Switch Mode",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onShowMenu,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Black, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.weight(0.2f))
    }
}

@Composable
private fun ActivatedScreen(onDeactivate: () -> Unit) {
    // This is the original green activated screen for Casual Mode
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00A86B))
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Content from original ActivatedScreen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Drinking Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
            )
            Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text(text = "Get Consult", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Casual Mode Activated.",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = GothamFontFamily
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.drinking_shots_cuate),
            contentDescription = null,
            modifier = Modifier.size(300.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Have Fun Drinking\nwith your friends.",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            fontFamily = GothamFontFamily
        )
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDeactivate,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 32.dp)
        ) {
            Text("LOCKED", color = Color.White)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}


@Composable
private fun SeriousModeActivatedScreen(onDeactivate: () -> Unit) {
    // This is the new red activated screen for Serious Mode
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF06262)) // Red background from image
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Drinking Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
            )
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text(text = "Get Consult", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Serious Mode Activated.",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = GothamFontFamily
        )
        Spacer(modifier = Modifier.height(16.dp))
         Image(
             painter = painterResource(id = R.drawable.drinking_shots_cuate),
             contentDescription = "Serious Mode Activated Illustration",
             modifier = Modifier.size(300.dp)
         )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Drink Well, Stay Safe.",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            fontFamily = GothamFontFamily
        )
        Spacer(modifier = Modifier.weight(1f))

        // Locked Button
        Button(
            onClick = onDeactivate,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 32.dp)
        ) {
            Text("LOCKED", color = Color.White)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
internal fun MenuBottomSheetContent(navController: NavController, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ... (Content from original MenuBottomSheetContent)
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
