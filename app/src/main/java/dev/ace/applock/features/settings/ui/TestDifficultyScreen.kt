package dev.ace.applock.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.ace.applock.AppLockApplication
import dev.ace.applock.data.repository.Difficulty
import dev.ace.applock.data.repository.PreferencesRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDifficultyScreen(navController: NavController) {
    var selectedGameType by remember { mutableStateOf("Typing Game") }
    val context = LocalContext.current
    val application = context.applicationContext as AppLockApplication
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(application.preferencesRepository))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Serious Mode Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFFBEA)
                )
            )
        },
        containerColor = Color(0xFFFFFBEA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select the game and difficulty you'll need to pass to unlock apps. Make it hard enough to stop your drunk self, but possible for you to pass when sober.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Game Type Selection
            SectionTitle(text = "Choose Game Type")
            OptionSelector(
                options = listOf("Math Game", "Typing Game"),
                selectedOption = selectedGameType,
                onOptionSelected = { selectedGameType = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Difficulty Selection
            SectionTitle(text = "Choose Difficulty")
            OptionSelector(
                options = Difficulty.entries.map { it.name },
                selectedOption = uiState.selectedDifficulty.name,
                onOptionSelected = { viewModel.setGameDifficulty(Difficulty.valueOf(it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Example Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You'll have to answer 3 questions like")
                    Spacer(modifier = Modifier.height(16.dp))
                    WordPreview(word = uiState.exampleWord)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Set Mode Button
            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C896))
            ) {
                Text("Set Mode Settings", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun OptionSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF00C896) else Color.Transparent)
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun WordPreview(word: String) {
    Row(
        modifier = Modifier
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        word.forEach { char ->
            Text(
                text = char.toString().uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

class SettingsViewModelFactory(private val repository: PreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
