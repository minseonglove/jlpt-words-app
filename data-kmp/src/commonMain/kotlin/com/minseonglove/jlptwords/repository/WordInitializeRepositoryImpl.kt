package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.ContentMetaDataSource
import com.minseonglove.jlptwords.datasource.PreferenceDataSource
import com.minseonglove.jlptwords.datasource.WordDataSource
import com.minseonglove.jlptwords.datasource.WordInitializeDataSource
import com.minseonglove.jlptwords.entity.AllContentsSyncProgress
import com.minseonglove.jlptwords.entity.AllContentsSyncResult
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SyncMode
import com.minseonglove.jlptwords.entity.WordsInitializeProgress
import com.minseonglove.jlptwords.service.dto.ContentUpdateDate
import com.minseonglove.jlptwords.service.dto.ExampleSentenceDto
import com.minseonglove.jlptwords.util.runCatchingCancellable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.updateAndGet

class WordInitializeRepositoryImpl(
    private val initializer: WordInitializeDataSource,
    private val wordDataSource: WordDataSource,
    private val preferenceDataSource: PreferenceDataSource,
    private val kanjiInfoRepository: KanjiInfoRepository,
    private val contentMetaDataSource: ContentMetaDataSource,
) : WordInitializeRepository {
    override suspend fun initializeWordsIfEmpty(level: JLPTLevel): Flow<WordsInitializeProgress> =
        flow {
            seedDatesFromSnapshotMeta()
            val content = wordDataSource.getContentUpdateDate(level) // wordsDate, examplesDate
            val readingsDate = wordDataSource.getReadingsUpdateDate()

            if (content.wordsDate == 0L) {
                emit(WordsInitializeProgress.ERROR)
                return@flow
            }

            // 핵심 데이터(요미·단어). 학습 자체가 불가해지므로 실패 시 중단한다.
            val coreDidWork =
                runCatchingCancellable {
                    val readingsSynced = syncReadingsIfNeeded(readingsDate)
                    val wordsSynced = syncWordsIfNeeded(level, content.wordsDate)
                    readingsSynced || wordsSynced
                }.getOrElse {
                    emit(WordsInitializeProgress.ERROR)
                    return@flow
                }

            // 부가 데이터(예문·한자)는 단계별로 독립 처리한다.
            // 한쪽 실패가 다른 쪽 동기화나 학습 진입을 막지 않도록(예: 예문 적재 실패 → 한자 사전 동기화는 그대로 시도).
            val examplesDidWork =
                runCatchingCancellable {
                    syncExamplesIfNeeded(level, content.examplesDate)
                }.getOrElse {
                    println("initializeWordsIfEmpty($level): 예문 동기화 실패 - $it")
                    false
                }

            // 한자 사전은 급수와 무관한 전역 데이터라 앞 단계가 끝난 뒤 한 번만 확인한다.
            val kanjiDidWork =
                runCatchingCancellable {
                    kanjiInfoRepository.syncIfNeeded()
                }.getOrElse {
                    println("initializeWordsIfEmpty($level): 한자 사전 동기화 실패 - $it")
                    false
                }

            val didWork = coreDidWork || examplesDidWork || kanjiDidWork
            emit(if (didWork) WordsInitializeProgress.COMPLETE else WordsInitializeProgress.ALREADY_UP_TO_DATE)
        }.flowOn(Dispatchers.Default)

    /** 요미 사전은 급수와 무관한 전역 데이터라 날짜만 비교한다. */
    private suspend fun FlowCollector<WordsInitializeProgress>.syncReadingsIfNeeded(
        readingsDate: Long,
    ): Boolean {
        if (readingsDate <= preferenceDataSource.getReadingsUpdateTime()) return false

        emit(WordsInitializeProgress.UPDATING)
        initializer.syncReadings()
        preferenceDataSource.setReadingsUpdateTime(readingsDate)
        return true
    }

    /**
     * 단어 본체. 비어 있으면 첫 적재, 아니면 갱신이다.
     *
     * 날짜가 같아도 품사가 비어 있으면 다시 받는다. 마이그레이션으로 컬럼만 생기고 값이 비어
     * 있는 경우가 있고, 같은 재적재가 (표기, 발음) 분리로 누락됐던 동형이의어 행도 함께 복구한다.
     */
    private suspend fun FlowCollector<WordsInitializeProgress>.syncWordsIfNeeded(
        level: JLPTLevel,
        wordsDate: Long,
    ): Boolean {
        val localWords = preferenceDataSource.getWordsUpdateTime(level)

        if (wordDataSource.getWordCountByLevel(level.code) == 0) {
            emit(WordsInitializeProgress.INITIALIZING)
            initializer.initializeWords(level)
            preferenceDataSource.setWordsUpdateTime(level, wordsDate)
            return true
        }

        val needsUpdate =
            wordsDate > localWords ||
                wordDataSource.getWordCountMissingPartOfSpeech(level.code) > 0
        if (needsUpdate.not()) return false

        emit(WordsInitializeProgress.UPDATING)
        initializer.updateWords(level)
        preferenceDataSource.setWordsUpdateTime(level, wordsDate)
        return true
    }

    /**
     * 예문. 날짜가 같아도 건수가 0이면 다시 받는다 (적재 실패·스키마 재생성 등).
     *
     * 표제어가 제외된 부분 적재면 날짜를 저장하지 않아 다음 실행에서 다시 받게 한다.
     * 건수가 0 이 아니게 되어 0건 가드로는 더 이상 잡히지 않기 때문이다.
     */
    private suspend fun FlowCollector<WordsInitializeProgress>.syncExamplesIfNeeded(
        level: JLPTLevel,
        examplesDate: Long,
    ): Boolean {
        val localExamples = preferenceDataSource.getExamplesUpdateTime(level)
        val hasNoExamples = wordDataSource.getExampleCountByLevel(level.code) == 0
        if (examplesDate <= localExamples && hasNoExamples.not()) return false

        emit(WordsInitializeProgress.UPDATING)
        val droppedKinds =
            initializer.syncExamples(
                level = level,
                knownKanji = initializer.loadKnownKanji(),
            )
        if (droppedKinds == 0) {
            preferenceDataSource.setExamplesUpdateTime(level, examplesDate)
        } else {
            println("initializeWordsIfEmpty($level): 예문 부분 적재(표제어 ${droppedKinds}종 제외) — 날짜 미저장")
        }
        return true
    }

    // 스플래시 체류 시간을 줄이기 위한 병렬화:
    // - 메타데이터(급수·요미·한자 날짜)는 content_update_date 컬렉션 1회 조회로 받는다
    //   (문서별 개별 조회는 순차 왕복 3회가 되어 이미 최신인 실행에서도 수백 ms 를 소모했다).
    // - 요미·한자 사전은 단어·예문과 독립이라 별도 브랜치로 병렬 수행한다.
    //   (예문 적재는 요미 사전을 참조하지 않는다 — reading 은 표시 시점에 조회. ExampleDao 참고)
    // - 예문 다운로드(전체 페이로드의 대부분)는 단어 다운로드와 병렬로 미리 받아두고,
    //   표제어 필터(knownKanji)가 필요한 적재만 전 급수 단어 적재(배리어) 뒤에 수행한다.
    // Room 쓰기는 단일 트랜잭션 큐에서 직렬화되므로 병렬 이득은 네트워크 구간에서 나온다.
    //
    // 단계 진행 상태는 실제 다운로드가 발생할 때만 방출한다 — 이미 최신인 실행에서
    // 단계 문구가 순간적으로 스쳐 지나가는 것을 막기 위함. 급수별 진행률(done/total)은
    // 병렬 자식 코루틴에서 방출해야 하므로 channelFlow 를 쓴다. 브랜치가 병렬이라 방출
    // 순서는 뒤섞일 수 있고, 표시 우선순위는 프레젠테이션 계층이 정한다.
    //
    // flowOn(Default): Firestore 응답의 수만 건 map 파싱이 collector(Main) 스레드에서
    // 돌지 않도록 한다.
    override fun initializeAllContents(): Flow<AllContentsSyncProgress> =
        channelFlow {
            send(AllContentsSyncProgress.Checking)
            seedDatesFromSnapshotMeta()
            val dates = wordDataSource.getAllContentUpdateDates()

            val readingsDeferred = async { syncReadingsIfNeeded(dates.readingsDate) }
            val kanjiDeferred =
                async {
                    runCatchingCancellable {
                        kanjiInfoRepository.syncIfNeeded(
                            remoteDate = dates.kanjiDate,
                            onSyncStart = { mode -> send(AllContentsSyncProgress.Kanji(mode)) },
                        )
                    }.getOrElse {
                        println("initializeAllContents: 한자 사전 동기화 실패 - $it")
                        false
                    }
                }

            val preps = JLPTLevel.entries.map { level -> checkLevelWords(level, dates.forLevel(level)) }

            // 예문 선다운로드 시작 — 단어 다운로드와 네트워크 구간이 겹친다.
            val exampleDownloads =
                preps.mapNotNull { prep ->
                    val mode = checkLevelExamples(prep) ?: return@mapNotNull null
                    ExampleDownload(
                        prep = prep,
                        mode = mode,
                        dtos =
                            async {
                                runCatchingCancellable { initializer.fetchExamples(prep.level) }.getOrElse {
                                    println("initializeAllContents(${prep.level}): 예문 다운로드 실패 - $it")
                                    null
                                }
                            },
                    )
                }

            val wordsDidWork = downloadLevelWords(preps.filter { it.wordsSyncMode != null })

            // 예문 표제어 필터(knownKanji)는 전 급수 단어가 적재된 뒤 1회만 만든다.
            val knownKanji = initializer.loadKnownKanji()
            val examplesDidWork = insertLevelExamples(exampleDownloads, knownKanji)

            val readingsDidWork = readingsDeferred.await()
            val kanjiDidWork = kanjiDeferred.await()
            val didWork = readingsDidWork || kanjiDidWork || wordsDidWork || examplesDidWork

            // 핵심 데이터(단어·예문) 확보 검증. 오프라인이어도 로컬에 이미 있으면 통과한다.
            val allReady =
                JLPTLevel.entries.all { level ->
                    wordDataSource.getWordCountByLevel(level.code) > 0 &&
                        wordDataSource.getExampleCountByLevel(level.code) > 0
                }
            val result =
                when {
                    !allReady -> AllContentsSyncResult.MISSING
                    didWork -> AllContentsSyncResult.UPDATED
                    else -> AllContentsSyncResult.UP_TO_DATE
                }
            send(AllContentsSyncProgress.Finished(result))
        }.flowOn(Dispatchers.Default)

    /**
     * 번들 스냅샷 DB 첫 실행 시 동기화 기준 날짜를 시딩한다.
     * 스냅샷은 데이터만 담고 있고 동기화 날짜는 DataStore 에 있어서, 시딩 없이는 첫 실행에서
     * "로컬 날짜 0 < 원격 날짜" 로 판정되어 전량 재다운로드가 일어난다.
     * DataStore 에 이미 날짜가 있는 항목(기존 설치·동기화 완료)은 건드리지 않는다.
     */
    private suspend fun seedDatesFromSnapshotMeta() {
        runCatchingCancellable {
            val snapshotDate = contentMetaDataSource.getSnapshotDate() ?: return
            JLPTLevel.entries.forEach { level ->
                if (preferenceDataSource.getWordsUpdateTime(level) == 0L) {
                    preferenceDataSource.setWordsUpdateTime(level, snapshotDate)
                }
                if (preferenceDataSource.getExamplesUpdateTime(level) == 0L) {
                    preferenceDataSource.setExamplesUpdateTime(level, snapshotDate)
                }
            }
            if (preferenceDataSource.getReadingsUpdateTime() == 0L) {
                preferenceDataSource.setReadingsUpdateTime(snapshotDate)
            }
            if (preferenceDataSource.getKanjiUpdateTime() == 0L) {
                preferenceDataSource.setKanjiUpdateTime(snapshotDate)
            }
        }.onFailure {
            println("seedDatesFromSnapshotMeta: 스냅샷 날짜 시딩 실패 - $it")
        }
    }

    /**
     * 요미(발음) 사전 동기화. 실패해도 false 만 반환하고 진행한다 —
     * 로컬에 이미 있으면 표시는 가능하고, 요미는 핵심 데이터 검증 대상이 아니다.
     */
    private suspend fun ProducerScope<AllContentsSyncProgress>.syncReadingsIfNeeded(remoteDate: Long): Boolean =
        runCatchingCancellable {
            val localDate = preferenceDataSource.getReadingsUpdateTime()
            if (remoteDate > localDate) {
                send(
                    AllContentsSyncProgress.Readings(
                        mode = if (localDate == 0L) SyncMode.INITIALIZE else SyncMode.UPDATE,
                    ),
                )
                initializer.syncReadings()
                preferenceDataSource.setReadingsUpdateTime(remoteDate)
                true
            } else {
                false
            }
        }.getOrElse {
            println("initializeAllContents: 요미 사전 동기화 실패 - $it")
            false
        }

    /** 급수별 단어 동기화 필요 여부. [wordsSyncMode] 가 null 이면 이미 최신이거나 날짜 확인 실패로 스킵. */
    private data class LevelSyncPrep(
        val level: JLPTLevel,
        val content: ContentUpdateDate,
        val wordsSyncMode: SyncMode?,
    )

    /**
     * 한 급수의 단어 동기화 필요 여부를 판단한다(없으면 적재, 날짜 갱신 시 차분 업데이트).
     * 오프라인 등으로 날짜를 못 받으면(wordsDate == 0) 이 급수는 건너뛴다 — 다음 스플래시에서 재시도된다.
     */
    private suspend fun checkLevelWords(
        level: JLPTLevel,
        content: ContentUpdateDate,
    ): LevelSyncPrep {
        if (content.wordsDate == 0L) return LevelSyncPrep(level, content, null)
        val mode =
            runCatchingCancellable {
                when {
                    wordDataSource.getWordCountByLevel(level.code) == 0 -> SyncMode.INITIALIZE
                    content.wordsDate > preferenceDataSource.getWordsUpdateTime(level) -> SyncMode.UPDATE
                    // 품사 미채움 가드: 마이그레이션으로 컬럼만 생기고 값이 비어 있으면 날짜가 같아도 재적재한다.
                    // 같은 재적재가 (표기, 발음) 분리로 누락됐던 동형이의어 행도 함께 복구한다.
                    wordDataSource.getWordCountMissingPartOfSpeech(level.code) > 0 -> SyncMode.UPDATE
                    else -> null
                }
            }.getOrElse {
                println("initializeAllContents($level): 단어 상태 확인 실패 - $it")
                null
            }
        return LevelSyncPrep(level, content, mode)
    }

    /**
     * 다운로드가 필요한 급수들의 단어 본체를 병렬로 받으며 급수 단위 진행률을 방출한다.
     * 급수별 실패는 로그만 남기고 다른 급수를 막지 않는다.
     * @return 새로 받은 단어가 있으면 true
     */
    private suspend fun ProducerScope<AllContentsSyncProgress>.downloadLevelWords(targets: List<LevelSyncPrep>): Boolean {
        if (targets.isEmpty()) return false
        val mode =
            if (targets.any { it.wordsSyncMode == SyncMode.INITIALIZE }) SyncMode.INITIALIZE else SyncMode.UPDATE
        val doneCount = MutableStateFlow(0)
        send(AllContentsSyncProgress.Words(mode, done = 0, total = targets.size))
        return targets
            .map { prep ->
                async {
                    val success =
                        runCatchingCancellable {
                            if (prep.wordsSyncMode == SyncMode.INITIALIZE) {
                                initializer.initializeWords(prep.level)
                            } else {
                                initializer.updateWords(prep.level)
                            }
                            preferenceDataSource.setWordsUpdateTime(prep.level, prep.content.wordsDate)
                            true
                        }.getOrElse {
                            println("initializeAllContents(${prep.level}): 단어 동기화 실패 - $it")
                            false
                        }
                    send(
                        AllContentsSyncProgress.Words(
                            mode = mode,
                            done = doneCount.updateAndGet { it + 1 },
                            total = targets.size,
                        ),
                    )
                    success
                }
            }.awaitAll()
            .any { it }
    }

    /** 한 급수의 예문 동기화 필요 여부. null 이면 이미 최신이거나 스킵. */
    private suspend fun checkLevelExamples(prep: LevelSyncPrep): SyncMode? {
        if (prep.content.wordsDate == 0L) return null
        return runCatchingCancellable {
            when {
                // 예문 0건 가드: 적재 실패·스키마 재생성 등으로 비어 있으면 날짜가 같아도 재적재한다.
                wordDataSource.getExampleCountByLevel(prep.level.code) == 0 -> SyncMode.INITIALIZE
                prep.content.examplesDate > preferenceDataSource.getExamplesUpdateTime(prep.level) -> SyncMode.UPDATE
                else -> null
            }
        }.getOrElse {
            println("initializeAllContents(${prep.level}): 예문 상태 확인 실패 - $it")
            null
        }
    }

    /** 급수별 예문 선다운로드. 다운로드 실패 시 [dtos] 가 null 을 돌려주고 해당 급수는 적재를 건너뛴다. */
    private class ExampleDownload(
        val prep: LevelSyncPrep,
        val mode: SyncMode,
        val dtos: Deferred<List<ExampleSentenceDto>?>,
    )

    /**
     * 미리 받아둔 급수별 예문을 [knownKanji] 필터로 걸러 적재하며 급수 단위 진행률을 방출한다.
     * 급수별로 자신의 다운로드 완료만 기다리므로 느린 급수가 다른 급수의 적재를 막지 않는다.
     * @return 새로 받은 예문이 있으면 true
     */
    private suspend fun ProducerScope<AllContentsSyncProgress>.insertLevelExamples(
        downloads: List<ExampleDownload>,
        knownKanji: Set<String>,
    ): Boolean {
        if (downloads.isEmpty()) return false
        val mode =
            if (downloads.any { it.mode == SyncMode.INITIALIZE }) SyncMode.INITIALIZE else SyncMode.UPDATE
        val doneCount = MutableStateFlow(0)
        send(AllContentsSyncProgress.Examples(mode, done = 0, total = downloads.size))
        return downloads
            .map { download ->
                async {
                    val prep = download.prep
                    val success =
                        runCatchingCancellable {
                            val dtos = download.dtos.await()
                            if (dtos != null) {
                                val droppedKinds = initializer.insertExamples(prep.level, dtos, knownKanji)
                                // 표제어가 제외됐다면 부분 적재다. 날짜를 저장하지 않아 다음 실행에서 다시 받게 한다
                                // (건수가 0 이 아니게 되어 0건 가드로는 더 이상 잡히지 않으므로).
                                if (droppedKinds == 0) {
                                    preferenceDataSource.setExamplesUpdateTime(prep.level, prep.content.examplesDate)
                                } else {
                                    println(
                                        "insertLevelExamples(${prep.level}): 예문 부분 적재(표제어 ${droppedKinds}종 제외) — 날짜 미저장",
                                    )
                                }
                                true
                            } else {
                                false
                            }
                        }.getOrElse {
                            println("initializeAllContents(${prep.level}): 예문 적재 실패 - $it")
                            false
                        }
                    send(
                        AllContentsSyncProgress.Examples(
                            mode = mode,
                            done = doneCount.updateAndGet { it + 1 },
                            total = downloads.size,
                        ),
                    )
                    success
                }
            }.awaitAll()
            .any { it }
    }
}
