package com.medops.adapter.in.web.resolver;

import com.medops.adapter.in.annotation.AdminSession;
import com.medops.domain.model.Admin;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


@Component
public class AdminSessionResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        var annotation = parameter.hasParameterAnnotation(AdminSession.class);
        var parameterType = parameter.getParameterType().equals(Admin.class);

        return (annotation && parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
