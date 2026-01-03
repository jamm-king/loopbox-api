package com.jammking.loopbox.application.exception

class NotificationPortException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.NOTIFICATION_PORT,
    message = message
)