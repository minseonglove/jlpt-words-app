package com.minseonglove.jlptwords

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.minseonglove.jlptwords.androidapp.R
import com.minseonglove.jlptwords.navigation.AppNavHost
import com.minseonglove.jlptwords.ui.theme.JLPTWordsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            MobileAds.initialize(this@MainActivity) {
                Log.d("MainTest", "MobileAds initialized")
            }
        }
        // 태블릿은 매니페스트의 세로 고정을 풀어 회전을 허용한다. Android 16(API 36)부터는 대화면에서
        // 방향 고정이 시스템에 의해 무시되므로, 그 이전 버전도 같은 동작이 되도록 여기서 맞춘다.
        if (!resources.getBoolean(R.bool.is_portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        // 상단바가 시스템 다크모드와 무관하게 항상 검정 텍스처라, 상태바 아이콘도 밝은색으로 고정한다.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            JLPTWordsTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
