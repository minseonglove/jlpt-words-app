package com.minseonglove.jlptwords.ui.selection.level

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.navigation.LevelSelectionRoute
import com.minseonglove.jlptwords.navigation.dto.LevelSelectionDTO
import com.minseonglove.jlptwords.navigation.type.LevelSelectionNavType
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.usecase.GetAllLevelSummaries
import com.minseonglove.jlptwords.usecase.SetLastSelectedLevel
import kotlinx.collections.immutable.toPersistentList
import org.orbitmvi.orbit.Container
import kotlin.reflect.typeOf

class LevelSelectionViewModel(
    savedStateHandle: SavedStateHandle,
    private val getAllLevelSummaries: GetAllLevelSummaries,
    private val setLastSelectedLevel: SetLastSelectedLevel,
) : BaseViewModel<LevelSelectionState, LevelSelectionSideEffect>() {
    override val container: Container<LevelSelectionState, LevelSelectionSideEffect> =
        container(
            initialState =
                run {
                    val lastSelectedLevel =
                        savedStateHandle
                            .toRoute<LevelSelectionRoute>(
                                typeMap = mapOf(typeOf<LevelSelectionDTO>() to LevelSelectionNavType()),
                            ).levelSelectionDTO
                            .currentLevel

                    LevelSelectionState(
                        lastSelectedLevel = lastSelectedLevel,
                        selectedLevel = lastSelectedLevel,
                    )
                },
        )

    fun onCreate() {
        LevelSelectionIntent.Initialize.post()
    }

    fun onLevelSelectionTabClick(level: JLPTLevel) {
        LevelSelectionIntent.ChangeSelectedLevel(level = level).post()
    }

    fun onNavigationButtonClick() {
        LevelSelectionIntent.NavigateToBack.post()
    }

    fun onConfirmButtonClick() {
        LevelSelectionIntent.NavigateToSessionSelectionScreen.post()
    }

    private fun LevelSelectionIntent.post() =
        intent {
            when (this@post) {
                LevelSelectionIntent.Initialize -> {
                    val levelSummaries = getAllLevelSummaries.invoke()

                    reduce {
                        state.copy(
                            levelSummaries = levelSummaries.toPersistentList(),
                            isLoading = false,
                        )
                    }
                }

                is LevelSelectionIntent.NavigateToSessionSelectionScreen -> {
                    setLastSelectedLevel.invoke(
                        level = state.selectedLevel,
                    )

                    postSideEffect(
                        LevelSelectionSideEffect.NavigateToSessionSelectionScreen(
                            level = state.selectedLevel,
                        ),
                    )
                }

                is LevelSelectionIntent.ChangeSelectedLevel -> {
                    reduce {
                        state.copy(
                            selectedLevel = level,
                        )
                    }
                }

                LevelSelectionIntent.NavigateToBack -> {
                    postSideEffect(LevelSelectionSideEffect.NavigateToBack)
                }
            }
        }
}
