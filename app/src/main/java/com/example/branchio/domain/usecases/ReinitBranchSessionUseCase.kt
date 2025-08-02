package com.example.branchio.domain.usecases

import android.app.Activity
import android.content.Context
import com.example.branchio.domain.repository.BranchRepository
import io.branch.referral.Branch
import io.branch.referral.BranchError
import org.json.JSONObject

class ReinitBranchSessionUseCase(
    private val branchRepository: BranchRepository
) {
    operator fun invoke(
        activity: Activity,
        callback: (JSONObject?, BranchError?) -> Unit
    ) {
        branchRepository.reinitBranchSession(activity, callback)
    }
}