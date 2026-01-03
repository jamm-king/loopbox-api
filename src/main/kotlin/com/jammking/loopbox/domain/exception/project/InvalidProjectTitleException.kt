package com.jammking.loopbox.domain.exception.project

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.ValidationException

class InvalidProjectTitleException(
    val projectId: ProjectId,
    val title: String
): ValidationException(
    errorCode = ErrorCode.INVALID_PROJECT_TITLE,
    message = "Validation: Invalid title for project: title=$title, projectId=${projectId.value}"
)