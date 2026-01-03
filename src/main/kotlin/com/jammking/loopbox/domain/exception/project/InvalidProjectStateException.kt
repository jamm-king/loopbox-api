package com.jammking.loopbox.domain.exception.project

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.project.ProjectStatus
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidProjectStateException(
    val projectId: ProjectId,
    val currentStatus: ProjectStatus,
    val attemptedAction: String
): StateViolationException(
    errorCode = ErrorCode.INVALID_PROJECT_STATE,
    message = "State violation: Cannot $attemptedAction when project is $currentStatus: projectId=${projectId.value}"
)