package com.minseonglove.jlptwords.di

import com.minseonglove.jlptwords.tts.TTSManager
import org.koin.dsl.module

actual val ttsModule =
    module {
        // 주입받는 ViewModel 이 각자 init/release 로 엔진을 소유한다. 싱글턴으로 공유하면
        // 뒤에 생성된 화면이 나갈 때의 release 가 앞 화면의 엔진까지 닫아 TTS 가 죽는다.
        factory { TTSManager(get()) }
    }
