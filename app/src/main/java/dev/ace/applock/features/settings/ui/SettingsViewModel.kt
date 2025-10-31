package dev.ace.applock.features.settings.ui

import androidx.lifecycle.ViewModel
import dev.ace.applock.data.repository.Difficulty
import dev.ace.applock.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TestDifficultyUiState(
    val selectedDifficulty: Difficulty = Difficulty.Medium,
    val exampleWord: String = "friend"
)

class SettingsViewModel(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TestDifficultyUiState())
    val uiState = _uiState.asStateFlow()

    private val easyWords = listOf("home", "safe", "stop", "water", "sleep")
    private val mediumWords = listOf("animals", "alcohol", "friend", "please", "secure")
    private val hardWords = listOf("tomorrow", "security", "remember", "emergency", "continue")

    init {
        val currentDifficulty = preferencesRepository.getGameDifficulty()
        _uiState.update {
            it.copy(
                selectedDifficulty = currentDifficulty,
                exampleWord = getExampleWordFor(currentDifficulty)
            )
        }
    }

    fun setGameDifficulty(difficulty: Difficulty) {
        preferencesRepository.setGameDifficulty(difficulty)
        _uiState.update {
            it.copy(
                selectedDifficulty = difficulty,
                exampleWord = getExampleWordFor(difficulty)
            )
        }
    }

    private fun getExampleWordFor(difficulty: Difficulty): String {
        return when (difficulty) {
            Difficulty.Easy -> easyWords.random()
            Difficulty.Medium -> mediumWords.random()
            Difficulty.Hard -> hardWords.random()
        }
    }
}
