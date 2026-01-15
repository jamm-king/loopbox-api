package com.jammking.loopbox.domain.exception.project

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidProjectOwnerException(
    val projectId: ProjectId,
    val userId: UserId,
    val ownerUserId: UserId
): StateViolationException(
    errorCode = ErrorCode.INVALID_PROJECT_OWNER,
    message = "State violation: Invalid project owner: projectId=${projectId.value}, userId=${userId.value}"
)
