package com.example.branchio.util

import android.util.Log
import org.json.JSONObject
import com.example.branchio.domain.entity.BranchLinkData

object BranchLinkExtractor {

    private const val TAG = "BranchLinkExtractor"

    /**
     * Extracts Branch link data from JSONObject referringParams
     * @param referringParams The JSONObject received from Branch callback
     * @return BranchLinkData with extracted information
     */
    fun extractBranchLinkData(referringParams: JSONObject?): BranchLinkData {
        if (referringParams == null) {
            Log.w(TAG, "referringParams is null, returning empty BranchLinkData")
            return createEmptyBranchLinkData()
        }

        Log.d(TAG, "Extracting data from: ${referringParams.toString()}")

        return try {
            BranchLinkData(
                title = extractTitle(referringParams),
                description = extractDescription(referringParams),
                imageUrl = extractImageUrl(referringParams),
                metadata = extractCustomMetadata(referringParams)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting Branch link data", e)
            createEmptyBranchLinkData()
        }
    }

    /**
     * Extracts title from Branch parameters
     * Looks for ~title, title, ~canonical_identifier as fallbacks
     */
    private fun extractTitle(params: JSONObject): String {
        return params.optString("~title", "").ifEmpty {
            params.optString("title", "").ifEmpty {
                params.optString("~canonical_identifier", "")
            }
        }
    }

    /**
     * Extracts description from Branch parameters
     * Looks for ~description, description, ~content_description as fallbacks
     */
    private fun extractDescription(params: JSONObject): String {
        return params.optString("~description", "").ifEmpty {
            params.optString("description", "").ifEmpty {
                params.optString("~content_description", "")
            }
        }
    }

    /**
     * Extracts image URL from Branch parameters
     * Looks for ~image_url, image_url, ~content_image_url as fallbacks
     */
    private fun extractImageUrl(params: JSONObject): String {
        return params.optString("~image_url", "").ifEmpty {
            params.optString("image_url", "").ifEmpty {
                params.optString("~content_image_url", "")
            }
        }
    }

    /**
     * Extracts custom metadata (parameters without Branch prefixes)
     * Filters out Branch reserved parameters and collects custom key-value pairs
     */
    private fun extractCustomMetadata(params: JSONObject): Map<String, String> {
        val metadata = mutableMapOf<String, String>()

        // Extract direct custom parameters (no Branch prefixes)
        val keys = params.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (isCustomParameter(key)) {
                val value = params.optString(key, "")
                if (value.isNotEmpty()) {
                    metadata[key] = value
                }
            }
        }

        // Extract from ~metadata object if it exists
        params.optJSONObject("~metadata")?.let { metadataObject ->
            val metadataKeys = metadataObject.keys()
            while (metadataKeys.hasNext()) {
                val key = metadataKeys.next()
                val value = metadataObject.optString(key, "")
                if (value.isNotEmpty()) {
                    metadata[key] = value
                }
            }
        }

        return metadata.toMap()
    }

    /**
     * Determines if a parameter key is a custom parameter
     * Returns false for Branch reserved keys and prefixes
     */
    private fun isCustomParameter(key: String): Boolean {
        return !key.startsWith("~") &&
                !key.startsWith("$") &&
                !key.startsWith("+") &&
                !BRANCH_RESERVED_KEYS.contains(key)
    }

    /**
     * Creates an empty BranchLinkData object
     */
    private fun createEmptyBranchLinkData(): BranchLinkData {
        return BranchLinkData(
            title = "",
            description = "",
            imageUrl = "",
            metadata = emptyMap()
        )
    }

    /**
     * Branch reserved keys that don't use prefixes but should be excluded from metadata
     */
    private val BRANCH_RESERVED_KEYS = setOf(
        "clicked_branch_link",
        "is_first_session",
        "identity_id",
        "session_id",
        "link_click_id",
        "browser_fingerprint_id",
        "device_fingerprint_id",
        "~referring_link"
    )

    /**
     * Utility method to check if deep link data contains meaningful content
     */
    fun hasValidContent(branchLinkData: BranchLinkData): Boolean {
        return branchLinkData.title.isNotEmpty() ||
                branchLinkData.description.isNotEmpty() ||
                branchLinkData.imageUrl.isNotEmpty() ||
                branchLinkData.metadata.isNotEmpty()
    }

    /**
     * Utility method to get a specific metadata value safely
     */
    fun getMetadataValue(branchLinkData: BranchLinkData, key: String): String? {
        return branchLinkData.metadata[key]
    }

    /**
     * Utility method to check if a specific metadata key exists
     */
    fun hasMetadataKey(branchLinkData: BranchLinkData, key: String): Boolean {
        return branchLinkData.metadata.containsKey(key)
    }

    /**
     * Debug method to log all extracted data
     */
    fun logExtractedData(branchLinkData: BranchLinkData) {
        Log.d(TAG, "Extracted BranchLinkData:")
        Log.d(TAG, "  Title: ${branchLinkData.title}")
        Log.d(TAG, "  Description: ${branchLinkData.description}")
        Log.d(TAG, "  ImageUrl: ${branchLinkData.imageUrl}")
        Log.d(TAG, "  Metadata: ${branchLinkData.metadata}")
    }
}
