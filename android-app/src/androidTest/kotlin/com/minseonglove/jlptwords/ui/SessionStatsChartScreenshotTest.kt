package com.minseonglove.jlptwords.ui

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.minseonglove.jlptwords.ui.study.component.SessionStatsChart
import com.minseonglove.jlptwords.ui.study.component.accuracyAxisOf
import com.minseonglove.jlptwords.ui.study.component.timeAxisOf
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * SessionStatsChart 를 에뮬레이터에서 실제 렌더링해 PNG 로 저장한다(디자인 대조용).
 * pull: adb pull /sdcard/Android/data/com.minseonglove.jlptwords/files/session_stats_chart.png
 */
@RunWith(AndroidJUnit4::class)
class SessionStatsChartScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun captureSessionStatsChart() {
        composeRule.setContent {
            MolluTheme {
                Column(modifier = Modifier.background(Color(0xFFEEEEEE)).padding(8.dp)) {
                    // 일반 케이스 (시간 하강 / 정답률 dip 후 상승), 실제 회차 4~9
                    ChartBox(
                        times = persistentListOf(93, 88, 84, 80, 77, 74),
                        accuracies = persistentListOf(62, 58, 64, 68, 72, 75),
                        startRound = 4,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // 창이 한 칸 밀린 뒤 — 축과 8회차 높이가 위 그래프와 같아야 한다
                    ChartBox(
                        times = persistentListOf(88, 84, 80, 77, 74, 70),
                        accuracies = persistentListOf(58, 64, 68, 72, 75, 78),
                        allTimes = persistentListOf(93, 88, 84, 80, 77, 74, 70),
                        allAccuracies = persistentListOf(62, 58, 64, 68, 72, 75, 78),
                        startRound = 5,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // 차이가 미미한 케이스 — 최소 폭 덕에 선이 스트립을 꽉 채우지 않아야 한다
                    ChartBox(
                        times = persistentListOf(76, 75, 75, 74, 74, 74),
                        accuracies = persistentListOf(100, 100, 100, 100, 100, 100),
                        startRound = 1,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // 첫 회차 — 점이 가운데 하나뿐이어도 값 라벨이 점 옆에 붙어야 한다
                    ChartBox(
                        times = persistentListOf(40),
                        accuracies = persistentListOf(100),
                        startRound = 1,
                    )
                }
            }
        }
        composeRule.waitForIdle()

        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // 내부 filesDir 에 저장 (run-as 로 pull 가능, 수동 설치 시 uninstall 안 되게 운용)
        val file = File(context.filesDir, "session_stats_chart.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}

@Composable
private fun ChartBox(
    times: ImmutableList<Int>,
    accuracies: ImmutableList<Int>,
    startRound: Int,
    allTimes: ImmutableList<Int> = times,
    allAccuracies: ImmutableList<Int> = accuracies,
) {
    SessionStatsChart(
        times = times,
        accuracies = accuracies,
        timeAxis = timeAxisOf(allTimes),
        accuracyAxis = accuracyAxisOf(allAccuracies),
        timeLabel = "시간",
        accuracyLabel = "정답률",
        xAxisLabel = "회차",
        timeFormatter = { "${it / 60}:${(it % 60).toString().padStart(2, '0')}" },
        startRound = startRound,
        modifier =
            Modifier
                .width(354.dp)
                .height(146.dp)
                .background(MolluTheme.colorScheme.white),
    )
}
