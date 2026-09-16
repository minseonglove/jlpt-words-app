package com.minseonglove.jlptwords.datasource

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.UIKit.UIDevice
import platform.posix.uname
import platform.posix.utsname

actual class DeviceInfoDataSource {
    /**
     * UIDevice.model 은 "iPhone"/"iPad" 만 돌려줘 기종을 구분할 수 없다.
     * uname 의 machine 은 "iPhone16,2" 처럼 모델별로 갈리므로 문의 대응에 쓸 수 있다
     * (시뮬레이터에서는 호스트 아키텍처 "arm64" 가 나온다).
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getModelName(): String =
        memScoped {
            val systemInfo = alloc<utsname>()
            uname(systemInfo.ptr)
            systemInfo.machine.toKString()
        }

    actual suspend fun getOsVersion(): String {
        return "iOS ${UIDevice.currentDevice.systemVersion}"
    }
}
