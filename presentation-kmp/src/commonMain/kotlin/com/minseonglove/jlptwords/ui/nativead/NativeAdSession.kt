package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ad.PlatformNativeAd
import com.minseonglove.jlptwords.ui.base.molluButtonBackground
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.ad_action_label
import org.jetbrains.compose.resources.stringResource

/**
 * 종료 다이얼로그용 네이티브 광고 카드(mollu). 미디어(검정 외곽선) + 아이콘·문구 + 열기 CTA.
 * CTA 클릭은 광고 SDK 가 [NativeAdCallToActionView] 를 통해 등록하므로, 내부 버튼은 시각 전용
 * (자체 clickable 을 두지 않는다 — 두면 광고 클릭 등록을 가로챈다).
 */
@Composable
fun NativeAdSession(
    modifier: Modifier,
    nativeAd: PlatformNativeAd,
) {
    NativeAdView(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 광고 미디어
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(144.dp)
                            .border(
                                width = 0.25.dp,
                                color = MolluTheme.colorScheme.black,
                                shape = RectangleShape,
                            ),
                ) {
                    NativeAdMediaView(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MolluTheme.colorScheme.black),
                    )
                    NativeAdBadge()
                    NativeAdChoicesView(
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }

                // 광고 아이콘 및 문구
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeAdIconView(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .border(
                                    width = 0.25.dp,
                                    color = MolluTheme.colorScheme.black,
                                    shape = RectangleShape,
                                ),
                    ) {
                        NativeAdImage(
                            modifier = Modifier.fillMaxSize(),
                            nativeAd = nativeAd,
                        )
                    }

                    NativeAdHeadlineView {
                        Text(
                            text = nativeAd.headline.orEmpty(),
                            style = MolluTheme.typography.caption1,
                            color = MolluTheme.colorScheme.labelAlternative,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // 열기 CTA (시각 전용 검정 텍스처 버튼, 클릭은 광고뷰가 등록)
            NativeAdCallToActionView(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .molluButtonBackground(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            nativeAd.callToAction
                                ?: stringResource(Res.string.ad_action_label),
                        style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                        color = MolluTheme.colorScheme.white,
                    )
                }
            }

            // 네이티브 광고 객체 바인딩
            NativeAdBinder(nativeAd = nativeAd)
        }
    }
}
