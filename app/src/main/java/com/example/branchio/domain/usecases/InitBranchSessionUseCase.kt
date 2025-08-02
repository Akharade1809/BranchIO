package com.example.branchio.domain.usecases

import android.app.Activity
import android.net.Uri
import com.example.branchio.domain.repository.BranchRepository
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.BranchError
import io.branch.referral.util.LinkProperties

class InitBranchSessionUseCase(
    private val branchRepository: BranchRepository
) {
    operator fun invoke(
        activity: Activity,
        data: Uri?,
        callback: (BranchUniversalObject?, LinkProperties?, BranchError?) -> Unit
    ) {
        branchRepository.initBranchSession( activity, data, callback)
    }
}