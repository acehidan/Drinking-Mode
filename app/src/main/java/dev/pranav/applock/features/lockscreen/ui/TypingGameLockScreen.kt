package dev.pranav.applock.features.lockscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel to manage the state of the Typing Game Lock Screen
class TypingGameViewModel : ViewModel() {

    private val words = listOf("Alcohol", "Sunshine", "Adventure", "Technology", "Happiness")
    private var currentWordIndex = 0

    // Initialize the state with the first word directly.
    private val _uiState = MutableStateFlow(
        TypingGameUiState(wordToType = words[currentWordIndex])
    )
    val uiState = _uiState.asStateFlow()


    private fun generateNewWord() {
        _uiState.update {
            it.copy(
                wordToType = words[currentWordIndex],
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

    fun onUnlockClicked(onAttempt: (String) -> Boolean) {
        val isCorrect = onAttempt(_uiState.value.inputText.text)
        if (!isCorrect) {
            _uiState.update { it.copy(isError = true, inputText = TextFieldValue("")) }
        }
    }

    fun nextWord() {
        currentWordIndex = (currentWordIndex + 1) % words.size
        generateNewWord()
    }
}

// Data class for the UI state
data class TypingGameUiState(
    val wordToType: String = "",
    val inputText: TextFieldValue = TextFieldValue(""),
    val isError: Boolean = false
)

// Composable for the Typing Game Lock Screen
@Composable
fun TypingGameLockScreen(
    viewModel: TypingGameViewModel,
    onWordAttempt: (String) -> Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

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
                repeat(3) {
                    Box(modifier = Modifier
                        .size(10.dp)
                        .background(Color.Gray, CircleShape))
                }
            }

            // Using a Spacer with weight to push the buttons to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // This button replaces the auto-unlock logic to prevent bugs
            Button(
                onClick = { viewModel.onUnlockClicked(onWordAttempt) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Text("Unlock", fontSize = 18.sp)
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
                // Hint Text (placeholder)
                if (value.text.isEmpty()) {
                    Text(
                        text = hintWord,
                        style = TextStyle(fontSize = 28.sp),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                // Actual text field
                innerTextField()
            }
        }
    )
}
