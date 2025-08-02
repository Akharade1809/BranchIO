package com.example.branchio.domain.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.branchio.domain.entity.BranchLinkData
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resumeWithException

interface BranchRepository {
    suspend fun generateBranchLink(data : BranchLinkData) : Result<String>

    fun initBranchSession(
        activity: Activity,
        data: Uri?,
        callback: (BranchUniversalObject?, LinkProperties?, BranchError?) -> Unit)

    fun reinitBranchSession(
        activity: Activity,
        callback: (JSONObject?, BranchError?) -> Unit
    )

    suspend fun handleDeepLink(intent : Intent) : Result<BranchLinkData>

}


class BranchRepositoryImpl(
    private val context: Context
) : BranchRepository {

    fun createBranchUniversalObject(data : BranchLinkData) : BranchUniversalObject{
        return BranchUniversalObject()
            .setTitle(data.title)
            .setContentDescription(data.description)
            .setContentImageUrl(data.imageUrl)
            .setContentMetadata(
                ContentMetadata().apply {
                    data.metadata.forEach { (key, value) ->
                        addCustomMetadata(key, value)
                    }
                }
            )
    }


    override suspend fun generateBranchLink(data : BranchLinkData): Result<String> {
        return try {
            // Define the content you want to share
            val buo = createBranchUniversalObject(data)

            // Define the link properties
            val linkProperties = LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing")
                .setCampaign("content 123 launch")
                .setStage("new user")
                .addControlParameter("\$deeplink_path", "deeplink")
                .addControlParameter("\$android_deeplink_path", "deeplink")
                .addControlParameter("\$canonical_url", "https://n2ujk.test-app.link/deeplink")





            // Suspend until Branch link is generated
            val link = suspendCancellableCoroutine<String> { continuation ->
                buo.generateShortUrl(context, linkProperties) { url, error ->
                    if (error == null && url != null) {
                        continuation.resume(url) {}
                    } else {
                        continuation.resumeWithException(
                            (error ?: Exception("Unknown error")) as Throwable
                        )
                    }
                }
            }
            println(link)

            Result.success(link)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun initBranchSession(
        activity: Activity,
        data: Uri?,
        callback: (BranchUniversalObject?, LinkProperties?, BranchError?) -> Unit
    ) {
        Branch.sessionBuilder(activity)
            .withCallback { buo, linkProps, error ->
                callback(buo, linkProps, error)
            }
            .withData(data)
            .init()

    }

    override fun reinitBranchSession(
        activity: Activity,
        callback: (JSONObject?, BranchError?) -> Unit
    ) {
        Branch.sessionBuilder(activity)
            .withCallback { referringParams, error ->
                callback(referringParams, error)
            }
            .reInit()
    }

    override suspend fun handleDeepLink(intent: Intent): Result<BranchLinkData> {
        return suspendCancellableCoroutine { continuation ->
            Branch.sessionBuilder(context as Activity?)
                .withCallback { buo, linkProps, errors ->
                    if(errors != null){
                        continuation.resumeWithException(Exception(errors.message))
                    } else {
                        val data = BranchLinkData(
                            title = buo?.title ?: "",
                            description = buo?.description ?: "",
                            imageUrl = buo?.imageUrl ?: "",
                            metadata = buo?.contentMetadata?.customMetadata ?: emptyMap(),
                        )
                        continuation.resume(Result.success(data), onCancellation = null)
                    }
                }
                .withData(intent.data)
                .init()
        }
    }

}