package com.minseonglove.jlptwords.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** 앱에 번들된 사전 적재 스냅샷 DB 의 리소스 이름. 생성은 data-kmp 의 PrepopulatedDbGenerator. */
private const val PREPOPULATED_DB_RESOURCE = "jlpt_words_prepopulated"

@OptIn(ExperimentalForeignApi::class)
fun getDatabaseBuilder(): RoomDatabase.Builder<JLPTWordsDatabase> {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    val dbFilePath = requireNotNull(documentDirectory?.path) + "/${JLPTWordsDatabase.DATABASE_NAME}"
    copyPrepopulatedDbIfMissing(dbFilePath)
    return Room.databaseBuilder<JLPTWordsDatabase>(
        name = dbFilePath,
    )
}

/**
 * DB 파일이 없으면(첫 실행) 번들 스냅샷을 복사해 첫 실행 콘텐츠 다운로드를 생략한다.
 * 스냅샷이 번들에 없거나 복사에 실패하면 빈 DB 로 시작한다 — 기존 온라인 초기화 경로가 채운다.
 */
@OptIn(ExperimentalForeignApi::class)
private fun copyPrepopulatedDbIfMissing(dbFilePath: String) {
    val fileManager = NSFileManager.defaultManager
    if (fileManager.fileExistsAtPath(dbFilePath)) return
    val bundledPath =
        NSBundle.mainBundle.pathForResource(PREPOPULATED_DB_RESOURCE, ofType = "db")
            ?: return
    // 임시 파일에 쓴 뒤 이동한다. 곧바로 목적지에 복사하면 도중에 앱이 죽었을 때 잘린 파일이 남고,
    // 다음 실행은 "파일 있음"으로 판단해 깨진 DB 를 그대로 연다.
    val tempPath = "$dbFilePath.tmp"
    fileManager.removeItemAtPath(tempPath, error = null)
    val copied =
        fileManager.copyItemAtPath(
            srcPath = bundledPath,
            toPath = tempPath,
            error = null,
        )
    if (!copied) {
        println("copyPrepopulatedDbIfMissing: 번들 스냅샷 복사 실패")
        return
    }
    val moved =
        fileManager.moveItemAtPath(
            srcPath = tempPath,
            toPath = dbFilePath,
            error = null,
        )
    if (!moved) {
        fileManager.removeItemAtPath(tempPath, error = null)
        println("copyPrepopulatedDbIfMissing: 번들 스냅샷 이동 실패")
    }
}
