package com.example.branchio.domain.repository

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.branchio.data.models.Product
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.util.BRANCH_STANDARD_EVENT
import io.branch.referral.util.BranchEvent
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.CurrencyType
import io.branch.referral.util.LinkProperties
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.util.Calendar
import kotlin.coroutines.resumeWithException

interface BranchRepository {
    suspend fun generateBranchLink(data : Product) : Result<String>

    fun initBranchSession(
        activity: Activity,
        data: Uri?,
        callback: (BranchUniversalObject?, LinkProperties?, BranchError?) -> Unit)

    fun reinitBranchSession(
        activity: Activity,
        callback: (JSONObject?, BranchError?) -> Unit
    )

    fun trackEvent(
        eventName : String,
        buo : BranchUniversalObject? = null,
        eventData : Map<String, Any> = emptyMap()
    )

    fun trackContentView(buo: BranchUniversalObject)

    fun trackPurchase(buo: BranchUniversalObject, revenue: Double, currency: String = "USD")

    fun trackAddToCart(buo: BranchUniversalObject)
}


class BranchRepositoryImpl(
    private val context: Context
) : BranchRepository {

    override suspend fun generateBranchLink(data: Product): Result<String> {
        return try {

            val metadata = mapOf(
                "product_id" to data.id.toString(),
                "product_title" to data.title,
                "product_price" to data.price.toString(),
                "product_category" to data.category,
                "rating" to data.rating.rate.toString(),
                "page_type" to "product_detail",
                "image_url" to data.image
            )

            val buo = BranchUniversalObject()
                .setCanonicalIdentifier("content/${data.id}")
                .setTitle(data.title)
                .setContentDescription("Check out this amazing ${data.category} for just $${data.price}!")
                .setContentImageUrl(data.image)
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(ContentMetadata().apply {
                    metadata.forEach { (key, value) -> addCustomMetadata(key, value) }
                    addCustomMetadata("creation_timestamp", System.currentTimeMillis().toString())
                    addCustomMetadata("app_version", "1.0.0")
                })

            val linkProperties: LinkProperties = LinkProperties()
                .setChannel("product_sharing")
                .setFeature("product_detail")
                .setCampaign("ecommerce_sharing")
                .setStage("product_view")
                .addControlParameter("\$desktop_url", "https://example.com/home")
                .addControlParameter("custom", "data")
                .addControlParameter("\$og_image_url", data.image)
                .addControlParameter("custom_random", Calendar.getInstance().timeInMillis.toString())

            metadata.forEach { (key, value) ->
                linkProperties.addControlParameter(key, value)
            }


            linkProperties.addControlParameter("page_type", "product_detail")

            val link = suspendCancellableCoroutine<String> { continuation ->
                buo.generateShortUrl(context, linkProperties) { url, error ->
                    if (error == null && url != null) {
                        continuation.resume(url, onCancellation = null)
                    } else {
                        val exception = Exception(error?.message ?: "Unknown error generating Branch link")
                        continuation.resumeWithException(exception)
                    }
                }
            }

            Log.d("BranchRepository", "Generated product share link: $link")
            Result.success(link)

        } catch (e: Exception) {
            Log.e("BranchRepository", "Failed to generate product link", e)
            Result.failure(e)
        }
    }


    override fun initBranchSession(
        activity: Activity,
        data: Uri?,
        callback: (BranchUniversalObject?, LinkProperties?, BranchError?) -> Unit
    ) {
        Log.d("BranchRepository", " Initializing Branch session with data: $data")

        Branch.sessionBuilder(activity)
            .withCallback { buo, linkProps, error ->
                Log.d("BranchRepository", "Branch init callback triggered")
                Log.d("BranchRepository", " BUO: ${buo?.title}, LinkProps: ${linkProps?.channel}, Error: ${error?.message}")

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


    override fun trackEvent(
        eventName: String,
        buo: BranchUniversalObject?,
        eventData: Map<String, Any>
    ) {
       try {
           val event = BranchEvent(eventName).apply {
               buo?.let {
                   addContentItems(it)
               }

               eventData.forEach { (key, value) ->
                   addCustomDataProperty(key,value.toString())
               }

               setDescription("Event tracked from Android Application")
               setCustomerEventAlias("android_${eventName.lowercase()}")
           }
           event.logEvent(context)
           Log.d("BranchRepository", " Event tracked: $eventName with BUO: ${buo != null}")
           Log.d("BranchRepository", " Event tracked: $eventName with BUO: ${buo}")
       } catch (e  : Exception){
           Log.e("BranchRepository", "Failed to track event: $eventName", e)
       }

    }

    override fun trackContentView(buo: BranchUniversalObject) {
        BranchEvent(BRANCH_STANDARD_EVENT.VIEW_ITEM)
            .addContentItems(buo)
            .logEvent(context)

        Log.d("BranchRepository", " Content view tracked: ${buo.title}")
    }

    override fun trackPurchase(
        buo: BranchUniversalObject,
        revenue: Double,
        currency: String
    ) {
        BranchEvent(BRANCH_STANDARD_EVENT.PURCHASE).apply {
            addContentItems(buo)
            setRevenue(revenue)
            setCurrency(CurrencyType.USD)
        }.logEvent(context)

        Log.d("BranchRepository", " Purchase tracked: $revenue $currency")
    }

    override fun trackAddToCart(buo: BranchUniversalObject) {
        BranchEvent(BRANCH_STANDARD_EVENT.ADD_TO_CART)
            .addContentItems(buo)
            .setDescription("User added item to cart")
            .logEvent(context)

        Log.d("BranchRepository", " Add to cart tracked: ${buo.title}")
    }

}