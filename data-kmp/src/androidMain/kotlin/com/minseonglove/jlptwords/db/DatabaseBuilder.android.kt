package com.minseonglove.jlptwords.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

/** 앱에 번들된 사전 적재 스냅샷 DB 의 assets 경로. 생성은 data-kmp 의 PrepopulatedDbGenerator. */
private const val PREPOPULATED_DB_ASSET = "database/jlpt_words_prepopulated.db"

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<JLPTWordsDatabase> {
    val dbFile = context.getDatabasePath(JLPTWordsDatabase.DATABASE_NAME)
    copyPrepopulatedDbIfMissing(context, dbFile)
    return Room.databaseBuilder<JLPTWordsDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
}

/**
 * DB 파일이 없으면(첫 실행) 번들 스냅샷을 복사해 첫 실행 콘텐츠 다운로드를 생략한다.
 * 임시 파일에 쓴 뒤 rename 해 복사 도중 크래시로 깨진 파일이 남지 않게 한다.
 * 스냅샷이 없거나 복사에 실패하면 빈 DB 로 시작한다 — 기존 온라인 초기화 경로가 채운다.
 */
private fun copyPrepopulatedDbIfMissing(
    context: Context,
    dbFile: File,
) {
    if (dbFile.exists()) return
    runCatching {
        dbFile.parentFile?.mkdirs()
        val tmpFile = File(dbFile.parentFile, "${dbFile.name}.tmp")
        context.assets.open(PREPOPULATED_DB_ASSET).use { input ->
            tmpFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (!tmpFile.renameTo(dbFile)) {
            tmpFile.delete()
            error("스냅샷 DB rename 실패")
        }
    }.onFailure {
        println("copyPrepopulatedDbIfMissing: 번들 스냅샷 복사 실패 - $it")
    }
}
