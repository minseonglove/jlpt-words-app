package com.minseonglove.jlptwords.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.adzero.AdZeroDialog
import com.minseonglove.jlptwords.ui.base.MolluDefaultTopBar
import com.minseonglove.jlptwords.ui.base.MolluSwitch
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import com.minseonglove.jlptwords.util.openUrl
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.inquiry_email
import jlptwords.presentation_kmp.generated.resources.inquiry_model
import jlptwords.presentation_kmp.generated.resources.inquiry_os_version
import jlptwords.presentation_kmp.generated.resources.privacy_policy_url
import jlptwords.presentation_kmp.generated.resources.setting_app_info
import jlptwords.presentation_kmp.generated.resources.setting_japanese_language
import jlptwords.presentation_kmp.generated.resources.setting_japanese_language_description
import jlptwords.presentation_kmp.generated.resources.setting_no_browser_app
import jlptwords.presentation_kmp.generated.resources.setting_no_email_app
import jlptwords.presentation_kmp.generated.resources.setting_opensource
import jlptwords.presentation_kmp.generated.resources.setting_title
import jlptwords.presentation_kmp.generated.resources.settings_inquiry
import jlptwords.presentation_kmp.generated.resources.settings_privacy_policy
import jlptwords.presentation_kmp.generated.resources.settings_version
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * 환경 설정 화면(mollu, Figma 99. Setting).
 * 앱 언어 토글은 설정 값 저장까지만 수행하고, 실제 동작은 추후 구현한다(ViewModel TODO 참고).
 */
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = koinViewModel(),
    navigateToOpenSourceLicenses: () -> Unit,
    navigateToBack: () -> Unit,
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SettingSideEffect.OpenInquiry -> {
                val email = getString(Res.string.inquiry_email)
                val modelInfo =
                    getString(
                        Res.string.inquiry_model,
                        sideEffect.inquiryInfo.modelName,
                    )
                val versionInfo =
                    getString(
                        Res.string.inquiry_os_version,
                        sideEffect.inquiryInfo.osVersion,
                    )
                val mailUrl =
                    buildMailtoUrl(
                        email = email,
                        body = "$modelInfo\n$versionInfo\n\n",
                    )
                if (openUrl(mailUrl).not()) {
                    val message = getString(Res.string.setting_no_email_app)
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }
            }

            SettingSideEffect.OpenPrivacyPolicy -> {
                val url = getString(Res.string.privacy_policy_url)
                if (openUrl(url).not()) {
                    val message = getString(Res.string.setting_no_browser_app)
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }
            }

            SettingSideEffect.OpenOpenSourceLicenses -> {
                navigateToOpenSourceLicenses()
            }

            SettingSideEffect.NavigateToBack -> {
                navigateToBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    MolluTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MolluTheme.colorScheme.backgroundNormal,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                MolluDefaultTopBar(
                    title = stringResource(Res.string.setting_title),
                    adZeroRemainingHours = state.adZeroRemainingHours,
                    onBackClick = viewModel::onBackClick,
                    onAdZeroClick = viewModel::onAdZeroClick,
                    onSettingsClick = null,
                )
            },
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                SettingContent(
                    isJapaneseLanguageEnabled = state.isJapaneseLanguageEnabled,
                    version = state.version,
                    onJapaneseLanguageToggle = viewModel::onJapaneseLanguageToggle,
                    onPrivacyPolicyClick = viewModel::onPrivacyPolicyClick,
                    onOpenSourceLicensesClick = viewModel::onOpenSourceLicensesClick,
                    onInquiryClick = viewModel::onInquiryClick,
                )

                if (state.isAdZeroDialogShown) {
                    AdZeroDialog(
                        onDismissRequest = viewModel::onAdZeroDialogDismiss,
                    )
                }
            }
        }
    }
}

/** 광고 다이얼로그 표시 여부 등 무관한 State 변경에 재구성되지 않도록 필요한 값만 받는다. */
@Composable
private fun SettingContent(
    isJapaneseLanguageEnabled: Boolean,
    version: String,
    onJapaneseLanguageToggle: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onOpenSourceLicensesClick: () -> Unit,
    onInquiryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        JapaneseLanguageSection(
            isEnabled = isJapaneseLanguageEnabled,
            onToggle = onJapaneseLanguageToggle,
        )
        SettingDivider()
        AppInfoSection(
            version = version,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onOpenSourceLicensesClick = onOpenSourceLicensesClick,
        )
        SettingDivider()
        InquirySection(
            onClick = onInquiryClick,
        )
    }
}

@Composable
private fun JapaneseLanguageSection(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                // 토글의 48dp 터치 박스가 세로 여백 일부를 대신하므로 vertical 패딩을 줄인다.
                .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.setting_japanese_language),
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.black,
            )
            Spacer(modifier = Modifier.weight(1f))
            MolluSwitch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.setting_japanese_language_description),
            style = MolluTheme.typography.caption1,
            color = MolluTheme.colorScheme.labelAlternative,
        )
    }
}

@Composable
private fun AppInfoSection(
    version: String,
    onPrivacyPolicyClick: () -> Unit,
    onOpenSourceLicensesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
    ) {
        Text(
            text = stringResource(Res.string.setting_app_info),
            style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            color = MolluTheme.colorScheme.black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        )
        SettingMenuRow(
            title = stringResource(Res.string.settings_privacy_policy),
            onClick = onPrivacyPolicyClick,
        )
        SettingMenuRow(
            title = stringResource(Res.string.setting_opensource),
            onClick = onOpenSourceLicensesClick,
        )
        SettingMenuRow(
            title = stringResource(Res.string.settings_version),
            value = version,
        )
    }
}

@Composable
private fun InquirySection(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Text(
        text = stringResource(Res.string.settings_inquiry),
        style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
        color = MolluTheme.colorScheme.highlightBlue,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                }.padding(horizontal = 20.dp, vertical = 20.dp),
    )
}

@Composable
private fun SettingMenuRow(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onClick()
                        }
                    } else {
                        Modifier
                    },
                ).padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MolluTheme.typography.body1,
            color = MolluTheme.colorScheme.black,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (value != null) {
            Text(
                text = value,
                style = MolluTheme.typography.body1,
                color = MolluTheme.colorScheme.black,
            )
        }
    }
}

@Composable
private fun SettingDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = MolluTheme.colorScheme.lineNormal,
    )
}

private fun buildMailtoUrl(
    email: String,
    body: String = "",
): String =
    buildString {
        append("mailto:$email")
        val params = mutableListOf<String>()
        if (body.isNotEmpty()) {
            params.add("body=${body.encodeURLParameter()}")
        }
        if (params.isNotEmpty()) {
            append("?${params.joinToString("&")}")
        }
    }

/** RFC 3986 의 unreserved 문자만 남기고 나머지는 UTF-8 바이트 단위로 퍼센트 인코딩한다. */
private fun String.encodeURLParameter(): String =
    encodeToByteArray().joinToString("") { byte ->
        val code = byte.toInt() and 0xFF
        val char = code.toChar()
        if ((code < 0x80 && char.isLetterOrDigit()) || char in URL_UNRESERVED_SYMBOLS) {
            char.toString()
        } else {
            "%" + code.toString(HEX_RADIX).uppercase().padStart(2, '0')
        }
    }

private const val URL_UNRESERVED_SYMBOLS = "-_.~"

private const val HEX_RADIX = 16
