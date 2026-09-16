package com.minseonglove.jlptwords.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.util.isSystemLanguageJapanese
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    private val lastSelectedLevelKey = intPreferencesKey("last_selected_level")
    private val lastInterstitialAdShowTimeKey = longPreferencesKey("last_interstitial_ad_show_time")
    private val adZeroTimeKey = longPreferencesKey("ad_zero_time")
    private val wordsUpdateTimeKeys =
        hashMapOf(
            JLPTLevel.N1 to longPreferencesKey("n1_words_update"),
            JLPTLevel.N2 to longPreferencesKey("n2_words_update"),
            JLPTLevel.N3 to longPreferencesKey("n3_words_update"),
            JLPTLevel.N4 to longPreferencesKey("n4_words_update"),
            JLPTLevel.N5 to longPreferencesKey("n5_words_update"),
        )
    private val examplesUpdateTimeKeys =
        hashMapOf(
            JLPTLevel.N1 to longPreferencesKey("n1_examples_update"),
            JLPTLevel.N2 to longPreferencesKey("n2_examples_update"),
            JLPTLevel.N3 to longPreferencesKey("n3_examples_update"),
            JLPTLevel.N4 to longPreferencesKey("n4_examples_update"),
            JLPTLevel.N5 to longPreferencesKey("n5_examples_update"),
        )
    private val readingsUpdateTimeKey = longPreferencesKey("readings_update")
    private val kanjiUpdateTimeKey = longPreferencesKey("kanji_update")

    // 홈 '오늘의 예문' (자정 기준 하루 유지)
    private val dailyExampleDayKey = intPreferencesKey("daily_example_day")
    private val dailyExampleLevelKey = intPreferencesKey("daily_example_level")
    private val dailyExampleKanjiKey = stringPreferencesKey("daily_example_kanji")
    private val dailyExamplePronunciationKey = stringPreferencesKey("daily_example_pronunciation")
    private val dailyExampleOrderKey = intPreferencesKey("daily_example_order")

    // 홈 '학습한 단어' 수의 자정 기준 스냅샷
    private val studiedWordBaselineValueKey = intPreferencesKey("studied_word_baseline_value")
    private val studiedWordBaselineDayKey = intPreferencesKey("studied_word_baseline_day")
    private val studiedWordLastSeenKey = intPreferencesKey("studied_word_last_seen")

    // 학습 도움말을 한 번이라도 닫아본 적 있는지 (최초 학습 시 자동 노출 판정)
    private val studyHelpSeenKey = booleanPreferencesKey("study_help_seen")

    // 환경 설정 (학습 알림·앱 언어)
    private val studyNotificationEnabledKey = booleanPreferencesKey("study_notification_enabled")
    private val studyNotificationHourKey = intPreferencesKey("study_notification_hour")
    private val studyNotificationMinuteKey = intPreferencesKey("study_notification_minute")
    private val japaneseLanguageEnabledKey = booleanPreferencesKey("japanese_language_enabled")

    suspend fun getLastSelectedLevel(): JLPTLevel? {
        return runCatching {
            val preferences = dataStore.data.first()
            val levelCode = preferences[lastSelectedLevelKey]
            levelCode?.let { JLPTLevel.fromCode(it) }
        }.getOrDefault(null)
    }

    suspend fun setLastSelectedLevel(
        level: JLPTLevel,
    ) {
        dataStore.edit { preferences ->
            preferences[lastSelectedLevelKey] = level.code
        }
    }

    suspend fun getLastInterstitialAdShowTime(): Long {
        return runCatching {
            val preferences = dataStore.data.first()
            preferences[lastInterstitialAdShowTimeKey] ?: 0L
        }.getOrDefault(0L)
    }

    suspend fun setLastInterstitialAdShowTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[lastInterstitialAdShowTimeKey] = time
        }
    }

    suspend fun getAdZeroTime(): Long {
        return runCatching {
            val preferences = dataStore.data.first()
            preferences[adZeroTimeKey] ?: 0L
        }.getOrDefault(0L)
    }

    suspend fun setAdZeroTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[adZeroTimeKey] = time
        }
    }

    suspend fun getWordsUpdateTime(
        level: JLPTLevel,
    ): Long {
        val key = wordsUpdateTimeKeys[level] ?: return 0L
        return runCatching {
            val preferences = dataStore.data.first()
            preferences[key] ?: 0L
        }.getOrDefault(0L)
    }

    suspend fun setWordsUpdateTime(
        level: JLPTLevel,
        time: Long,
    ) {
        val key = wordsUpdateTimeKeys[level] ?: return
        dataStore.edit { preferences ->
            preferences[key] = time
        }
    }

    suspend fun getExamplesUpdateTime(level: JLPTLevel): Long {
        val key = examplesUpdateTimeKeys[level] ?: return 0L
        return runCatching { dataStore.data.first()[key] ?: 0L }.getOrDefault(0L)
    }

    suspend fun setExamplesUpdateTime(
        level: JLPTLevel,
        time: Long,
    ) {
        val key = examplesUpdateTimeKeys[level] ?: return
        dataStore.edit { it[key] = time }
    }

    suspend fun getReadingsUpdateTime(): Long = runCatching { dataStore.data.first()[readingsUpdateTimeKey] ?: 0L }.getOrDefault(0L)

    suspend fun setReadingsUpdateTime(time: Long) {
        dataStore.edit { it[readingsUpdateTimeKey] = time }
    }

    suspend fun getKanjiUpdateTime(): Long = runCatching { dataStore.data.first()[kanjiUpdateTimeKey] ?: 0L }.getOrDefault(0L)

    suspend fun setKanjiUpdateTime(time: Long) {
        dataStore.edit { it[kanjiUpdateTimeKey] = time }
    }

    suspend fun getDailyExampleRecord(): DailyExampleRecord? {
        return runCatching {
            val preferences = dataStore.data.first()
            DailyExampleRecord(
                day = preferences[dailyExampleDayKey] ?: return null,
                levelCode = preferences[dailyExampleLevelKey] ?: return null,
                kanji = preferences[dailyExampleKanjiKey] ?: return null,
                pronunciation = preferences[dailyExamplePronunciationKey] ?: return null,
                exampleOrder = preferences[dailyExampleOrderKey] ?: return null,
            )
        }.getOrDefault(null)
    }

    suspend fun setDailyExampleRecord(record: DailyExampleRecord) {
        dataStore.edit { preferences ->
            preferences[dailyExampleDayKey] = record.day
            preferences[dailyExampleLevelKey] = record.levelCode
            preferences[dailyExampleKanjiKey] = record.kanji
            preferences[dailyExamplePronunciationKey] = record.pronunciation
            preferences[dailyExampleOrderKey] = record.exampleOrder
        }
    }

    suspend fun getStudiedWordSnapshot(): StudiedWordSnapshot? {
        return runCatching {
            val preferences = dataStore.data.first()
            StudiedWordSnapshot(
                baselineValue = preferences[studiedWordBaselineValueKey] ?: return null,
                baselineDay = preferences[studiedWordBaselineDayKey] ?: return null,
                lastSeenValue = preferences[studiedWordLastSeenKey] ?: return null,
            )
        }.getOrDefault(null)
    }

    suspend fun setStudiedWordSnapshot(snapshot: StudiedWordSnapshot) {
        dataStore.edit { preferences ->
            preferences[studiedWordBaselineValueKey] = snapshot.baselineValue
            preferences[studiedWordBaselineDayKey] = snapshot.baselineDay
            preferences[studiedWordLastSeenKey] = snapshot.lastSeenValue
        }
    }

    suspend fun getSettingPreferencesRecord(): SettingPreferencesRecord {
        return runCatching {
            val preferences = dataStore.data.first()
            SettingPreferencesRecord(
                isStudyNotificationEnabled = preferences[studyNotificationEnabledKey] ?: false,
                notificationHour = preferences[studyNotificationHourKey] ?: DEFAULT_NOTIFICATION_HOUR,
                notificationMinute = preferences[studyNotificationMinuteKey] ?: 0,
                // 설정을 한 번도 바꾸지 않았으면(키 부재) 기기 언어를 따르고, 일본어가 아니면 한국어로 폴백한다.
                isJapaneseLanguageEnabled = preferences[japaneseLanguageEnabledKey] ?: isSystemLanguageJapanese(),
            )
        }.getOrDefault(SettingPreferencesRecord())
    }

    fun observeJapaneseLanguageEnabled(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[japaneseLanguageEnabledKey] ?: isSystemLanguageJapanese()
        }

    // 파일 손상·알 수 없는 코드는 급수 미선택(null)으로 다뤄 구독자(화면 진입)가 죽지 않게 한다.
    fun observeLastSelectedLevel(): Flow<JLPTLevel?> =
        dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                preferences[lastSelectedLevelKey]?.let { code ->
                    runCatching { JLPTLevel.fromCode(code) }.getOrNull()
                }
            }

    // 읽기에 실패하면 '본 적 없음'으로 다뤄, 안내를 놓치는 쪽보다 한 번 더 보는 쪽을 택한다.
    suspend fun getStudyHelpSeen(): Boolean = runCatching { dataStore.data.first()[studyHelpSeenKey] ?: false }.getOrDefault(false)

    suspend fun setStudyHelpSeen() {
        dataStore.edit { it[studyHelpSeenKey] = true }
    }

    suspend fun setStudyNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[studyNotificationEnabledKey] = enabled }
    }

    suspend fun setStudyNotificationTime(
        hour: Int,
        minute: Int,
    ) {
        dataStore.edit { preferences ->
            preferences[studyNotificationHourKey] = hour
            preferences[studyNotificationMinuteKey] = minute
        }
    }

    suspend fun setJapaneseLanguageEnabled(enabled: Boolean) {
        dataStore.edit { it[japaneseLanguageEnabledKey] = enabled }
    }
}

/** 학습 알림 시각 기본값(22시 = 오후 10:00, 디자인 기본값). */
private const val DEFAULT_NOTIFICATION_HOUR = 22

/** 홈 '오늘의 예문' 영속 데이터. [day] 는 epoch 기준 일수(자정 갱신 판정), [exampleOrder] 는 단어 예문 목록 내 순번. */
data class DailyExampleRecord(
    val day: Int,
    val levelCode: Int,
    val kanji: String,
    val pronunciation: String,
    val exampleOrder: Int,
)

/**
 * 홈 '학습한 단어' 수의 자정 기준 스냅샷.
 * [baselineDay] 의 자정 기준값이 [baselineValue], 마지막 관측값이 [lastSeenValue] 다.
 * 날짜가 바뀌면 직전 관측값을 새 기준값으로 승격시켜 '오늘 변경 여부'를 판정한다.
 */
data class StudiedWordSnapshot(
    val baselineValue: Int,
    val baselineDay: Int,
    val lastSeenValue: Int,
)

/** 환경 설정 영속 데이터. 미설정 키는 기본값(알림 OFF·오후 10:00·일본어 모드 OFF)으로 채운다. */
data class SettingPreferencesRecord(
    val isStudyNotificationEnabled: Boolean = false,
    val notificationHour: Int = DEFAULT_NOTIFICATION_HOUR,
    val notificationMinute: Int = 0,
    val isJapaneseLanguageEnabled: Boolean = false,
)
