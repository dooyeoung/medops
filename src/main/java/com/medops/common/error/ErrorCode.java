package com.medops.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode implements ErrorCodeInterface{

    OK(200, 200, "성공"),
    BAD_REQUEST(400, 400, "잘못된 요청"),
    SERVER_ERROR(500, 500, "서버에러"),
    NULL_POINT(500, 512, "널 포인트"),
    ;


    private final Integer HttpStatusCode;
    private final Integer errorCode;
    private final String description;

}
