package com.medops.adapter.in.web.controller;


import com.medops.adapter.in.web.request.UserLoginRequest;
import com.medops.adapter.in.web.request.UserRegisterRequest;
import com.medops.common.response.Api;
import com.medops.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사용자 API")
public interface UserApiControllerSpec {

    @Operation(
        summary = "회원 가입",
        requestBody = @RequestBody(
            content = {
                @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "회원 가입",
                            value = """
                                {
                                    "email": "test@gmail.com",
                                    "password": "1234abc!",
                                    "name": "홍길동"
                                }
                                """
                        )
                    }
                )
            }
        )
    )
    Api<User> register(
        UserRegisterRequest request
    );

    @Operation(
        summary = "로그인",
        requestBody = @RequestBody(
            content = {
                @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "로그인",
                            value = "{\n    \"email\": \"test@gmail.com\",\n    \"password\": \"1234abc!\"\n}\n"
                        )
                    }
                )
            }
        )
    )
    Api<String> login(
        UserLoginRequest request
    );

    @Operation(
        summary = "내 정보"
    )
    Api<User> me(User user);
}
