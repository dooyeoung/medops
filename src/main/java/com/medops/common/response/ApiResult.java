package com.medops.common.response;

import com.medops.common.error.ErrorCode;
import com.medops.common.error.ErrorCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResult {

    private Integer resultCode;
    private String resultMessage;
    private String resultDescription;

    public static ApiResult OK(){
        return ApiResult.builder()
            .resultCode(ErrorCode.OK.getErrorCode())
            .resultMessage(ErrorCode.OK.getDescription())
            .resultDescription("성공")
            .build();
    }

    public static ApiResult ERROR(ErrorCodeInterface errorCodeInterface){
        return ApiResult.builder()
            .resultCode(errorCodeInterface.getErrorCode())
            .resultMessage(errorCodeInterface.getDescription())
            .resultDescription("성공")
            .build();
    }

    public static ApiResult ERROR(ErrorCodeInterface errorCodeInterface, Throwable tw){
        return ApiResult.builder()
            .resultCode(errorCodeInterface.getErrorCode())
            .resultMessage(errorCodeInterface.getDescription())
            .resultDescription(tw.getLocalizedMessage())
            .build();
    }

    public static ApiResult ERROR(ErrorCodeInterface errorCodeInterface, String description){
        return ApiResult.builder()
            .resultCode(errorCodeInterface.getErrorCode())
            .resultMessage(errorCodeInterface.getDescription())
            .resultDescription(description)
            .build();
    }
}