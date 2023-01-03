package me.erikpelli.jdigital;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

class ErrorMap extends LinkedHashMap<String, Object> {
    ErrorMap(Map<String, Object> map) {
        super(map);
    }
}

/**
 * Set a predefined JSON response schema for all the errors, compatible with the old application.
 * {
 * success: false,
 * error: String,
 * result: {}
 * }
 */
@Component
class CustomErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        var message = errorAttributes.getOrDefault("message", "");
        errorAttributes.clear();

        errorAttributes.put("success", false);
        errorAttributes.put("error", message);
        errorAttributes.put("result", Map.of());

        return new ErrorMap(errorAttributes);
    }
}

/**
 * Set a predefined JSON response schema for the successful responses, compatible with the old application.
 * {
 * success: true,
 * result: Object
 * }
 */
@ControllerAdvice
class CustomResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object dataToSend, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(dataToSend instanceof ErrorMap)) {
            return Map.of("success", true, "result", dataToSend);
        }
        return dataToSend;
    }
}
