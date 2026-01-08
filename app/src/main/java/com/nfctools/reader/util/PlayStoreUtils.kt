package com.nfctools.reader.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

/**
 * Google Play 商店相关工具类
 */
object PlayStoreUtils {
    
    private const val PACKAGE_NAME = "com.nfctools.reader"
    private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=$PACKAGE_NAME"
    private const val MARKET_URL = "market://details?id=$PACKAGE_NAME"
    
    /**
     * 请求应用内评分
     */
    suspend fun requestInAppReview(activity: Activity): Boolean {
        return try {
            val reviewManager = ReviewManagerFactory.create(activity)
            val reviewInfo = reviewManager.requestReviewFlow().await()
            reviewManager.launchReviewFlow(activity, reviewInfo).await()
            true
        } catch (e: Exception) {
            // 如果应用内评分失败，打开 Play 商店
            openPlayStore(activity)
            false
        }
    }
    
    /**
     * 打开 Play 商店应用页面
     */
    fun openPlayStore(context: Context) {
        try {
            // 尝试使用 Play 商店应用打开
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // 如果没有 Play 商店，使用浏览器打开
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
    
    /**
     * 分享应用
     */
    fun shareApp(context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "NFC Reader & Writer")
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this NFC Reader & Writer app!\n$PLAY_STORE_URL"
            )
        }
        val chooserIntent = Intent.createChooser(shareIntent, "Share via")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
    
    /**
     * 打开隐私政策页面
     */
    fun openPrivacyPolicy(context: Context, url: String) {
        openUrl(context, url)
    }
    
    /**
     * 打开服务条款页面
     */
    fun openTermsOfService(context: Context, url: String) {
        openUrl(context, url)
    }
    
    /**
     * 打开 URL
     */
    private fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // URL 打开失败
        }
    }
    
    /**
     * 发送反馈邮件
     */
    fun sendFeedback(context: Context, email: String = "support@nfctools.example.com") {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "NFC Reader & Writer Feedback")
            putExtra(
                Intent.EXTRA_TEXT,
                """
                App Version: ${getAppVersion(context)}
                Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                Android Version: ${android.os.Build.VERSION.RELEASE}
                
                Feedback:
                
                """.trimIndent()
            )
        }
        try {
            val chooserIntent = Intent.createChooser(intent, "Send feedback via")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            // 邮件发送失败
        }
    }
    
    /**
     * 获取应用版本号
     */
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
