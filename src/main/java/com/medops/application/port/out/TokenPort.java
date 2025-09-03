package com.medops.application.port.out;

import com.medops.domain.enums.TokenType;

public interface TokenPort {
    String generateToken(String id, TokenType tokenType);
    String parseUserIdFromToken(String token);
    TokenType parseTokenType(String token);
}
