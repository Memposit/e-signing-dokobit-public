package com.memposit.esigning.dokobit.zuul;

import com.memposit.esigning.dokobit.dto.CustomHttpServletRequestWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.*;

import static com.memposit.esigning.dokobit.common.Constants.Auth.*;
import static com.memposit.esigning.dokobit.common.Constants.MobileId.*;
import static com.memposit.esigning.dokobit.common.Constants.Status.OK;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostAuthZuulFilter extends ZuulFilter {

    @Value("${dokobit.api.frontUrl}")
    private String redirectUrl;
    private final ObjectMapper objectMapper;

    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = ctx.getRequest();
        final String requestURI = httpServletRequest.getRequestURI();

        try {
            if (requestURI.contains(CODE)) {
                String responseBody = ctx.getResponseBody();
                if (responseBody.contains(URL)) {
                    RequestContext.getCurrentContext().getResponse().sendRedirect(objectMapper.readTree(responseBody).get(URL).asText());
                }
            } else if (requestURI.contains(AUTH_REDIRECT)) {
                String responseBody = ctx.getResponseBody();
                if (responseBody.contains(STATUS)) {
                    saveMobileIdCookie(ctx, responseBody);
                } else
                    log.error("The request does not contain the required parameter :" +
                            " {} for user authentication in kevin and response status.", STATUS);
            }
        } catch (Exception e) {
            log.error("Error occurred in zuul post filter", e);
        }
        return null;
    }

    private void saveMobileIdCookie(RequestContext ctx, String responseBody) throws IOException {
        JsonNode responseBodyNode = objectMapper.readTree(responseBody);
        String status = responseBodyNode.get(STATUS).asText();
        if (status.equals(OK) && responseBodyNode.has(CODE) && responseBodyNode.has(MOBILE_ID_PHONE)) {
            CustomHttpServletRequestWrapper customHttpServletRequestWrapper = new CustomHttpServletRequestWrapper(ctx.getRequest());
            customHttpServletRequestWrapper.addCookie(getMobileIdCodeCookie(responseBodyNode));
            customHttpServletRequestWrapper.addCookie(getMobileIdPhoneCookie(responseBodyNode));
            ctx.setRequest(customHttpServletRequestWrapper);
            RequestContext.getCurrentContext().getResponse().sendRedirect(redirectUrl);
        }
    }

    private static Cookie getMobileIdPhoneCookie(JsonNode responseBodyNode) {
        final Cookie phoneCookie = new Cookie(MOBILE_ID_PHONE, responseBodyNode.get(MOBILE_ID_PHONE).asText());
        phoneCookie.setHttpOnly(true);
        phoneCookie.setSecure(true);
        phoneCookie.setMaxAge(2592000);
        return phoneCookie;
    }

    private Cookie getMobileIdCodeCookie(JsonNode responseBodyNode) {
        final Cookie codeCookie = new Cookie(MOBILE_ID_CODE, responseBodyNode.get(CODE).asText());
        codeCookie.setHttpOnly(true);
        codeCookie.setSecure(true);
        codeCookie.setMaxAge(2592000);
        return codeCookie;
    }

    @Override
    public boolean shouldFilter() {
        boolean shouldfilter = false;
        final RequestContext ctx = RequestContext.getCurrentContext();
        String URI = ctx.getRequest().getRequestURI();

        if (URI.contains(CODE) |
                URI.contains(AUTH_REDIRECT)) {
            shouldfilter = true;
        }

        return shouldfilter;
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public String filterType() {
        return "post";
    }

}
