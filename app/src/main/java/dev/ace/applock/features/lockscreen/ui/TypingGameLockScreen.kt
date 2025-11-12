
package dev.ace.applock.features.lockscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ace.applock.data.repository.Difficulty
import dev.ace.applock.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel to manage the state of the Typing Game Lock Screen
class TypingGameViewModel(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    private val easyWords = listOf("home", "safe", "stop", "water", "sleep", "taxi", "lock", "keys", "calm", "open", "door", "help", "done", "wait", "food")
    private val mediumWords = listOf("animals", "alcohol", "friend", "please", "secure", "sober", "morning", "tonight", "unlock", "password", "breathe", "control", "finish", "hello", "coffee")
    private val hardWords = listOf("tomorrow", "security", "remember", "emergency", "continue", "decision", "important", "message", "different", "everything", "goodnight", "hydrate", "responsible", "challenge", "patience")
    
    private fun getWordListForDifficulty(difficulty: Difficulty): List<String> {
        return when (difficulty) {
            Difficulty.Easy -> easyWords
            Difficulty.Medium -> mediumWords
            Difficulty.Hard -> hardWords
        }
    }

    private fun getRandomWord(): String {
        val difficulty = preferencesRepository.getGameDifficulty()
        return getWordListForDifficulty(difficulty).random()
    }
    
    // Initialize the state with the first word directly.
    private val _uiState = MutableStateFlow(
        TypingGameUiState(wordToType = getRandomWord())
    )
    val uiState = _uiState.asStateFlow()


    private fun generateNewWord() {
        _uiState.update {
            it.copy(
                wordToType = getRandomWord(),
                inputText = TextFieldValue(""),
                isError = false
            )
        }
    }

    fun onTextChanged(textValue: TextFieldValue) {
        if (textValue.text.length <= _uiState.value.wordToType.length) {
            _uiState.update { it.copy(inputText = textValue, isError = false) }
        }
    }

    fun onUnlockClicked(onAttempt: (String) -> Boolean, onUnlock: () -> Unit) {
        val isCorrect = onAttempt(_uiState.value.inputText.text)
        if (isCorrect) {
            val newWordsTypedCount = _uiState.value.wordsTypedCount + 1
            _uiState.update { it.copy(wordsTypedCount = newWordsTypedCount) }
            if (newWordsTypedCount == 3) {
                onUnlock()
            } else {
                generateNewWord()
            }
        } else {
            _uiState.update { it.copy(isError = true, inputText = TextFieldValue("")) }
        }
    }
}

// Data class for the UI state
data class TypingGameUiState(
    val wordToType: String = "",
    val inputText: TextFieldValue = TextFieldValue(""),
    val isError: Boolean = false,
    val wordsTypedCount: Int = 0
)

// Composable for the Typing Game Lock Screen
@Composable
fun TypingGameLockScreen(
    preferencesRepository: PreferencesRepository,
    onWordAttempt: (String) -> Boolean,
    onUnlock: () -> Unit
) {
    val viewModel: TypingGameViewModel = viewModel(factory = TypingGameViewModelFactory(preferencesRepository))
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val customColor = Color(0xFF01A87B)

    // Request focus for the keyboard to appear automatically
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E1E1E) // Dark background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Type the following Words",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(64.dp))

            CustomInputField(
                value = uiState.inputText,
                onValueChange = { viewModel.onTextChanged(it) },
                hintWord = uiState.wordToType,
                isError = uiState.isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            if (uiState.isError) {
                Text(
                    text = "Incorrect word. Please try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Page indicators
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { index ->
                    val color = if (index < uiState.wordsTypedCount) customColor else Color.Gray
                    Box(modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape))
                }
            }

            // Using a Spacer with weight to push the buttons to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // This button replaces the auto-unlock logic to prevent bugs
            Button(
                onClick = { viewModel.onUnlockClicked(onWordAttempt, onUnlock) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = customColor)
            ) {
                val buttonText = if (uiState.wordsTypedCount < 2) "Next" else "Unlock"
                Text(buttonText, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /*TODO for emergency*/ }) {
                    Text("Emergency", color = Color.White)
                }

                Button(
                    onClick = { /* Locked button does nothing for now */ },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("LOCKED", color = Color.White)
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun CustomInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    hintWord: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isError) MaterialTheme.colorScheme.error else Color.Gray

    BasicTextField(
        value = value,
        onValueChange = {
            onValueChange(it.copy(selection = TextRange(it.text.length)))
        },
        modifier = modifier,
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 28.sp),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Hint Text (placeholder) - Always visible
                Text(
                    text = hintWord,
                    style = TextStyle(fontSize = 28.sp),
                    color = Color.White.copy(alpha = 0.5f)
                )
                
                // Actual text field is drawn on top of the hint
                innerTextField()
            }
        }
    )
}

class TypingGameViewModelFactory(private val preferencesRepository: PreferencesRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TypingGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TypingGameViewModel(preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
