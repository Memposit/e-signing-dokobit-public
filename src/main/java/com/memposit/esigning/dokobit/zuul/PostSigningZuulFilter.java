package com.memposit.esigning.dokobit.zuul;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memposit.esigning.dokobit.common.Constants.Auth;
import com.memposit.esigning.dokobit.common.Constants.FileMetaData;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSigningZuulFilter extends ZuulFilter {
    public static final String SESSION_SIGNING_TOKEN = "session_signing_token";
    private final ObjectMapper objectMapper;

    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        try {
            String responseBody = ctx.getResponseBody();
            if (responseBody.contains(Auth.TOKEN)) {
                JsonNode responseBodyNode = objectMapper.readTree(responseBody);
                final Cookie codeCookie = new Cookie(SESSION_SIGNING_TOKEN, responseBodyNode.get(
                    Auth.TOKEN).asText());
                codeCookie.setHttpOnly(true);
                codeCookie.setSecure(true);
                codeCookie.setMaxAge(2592000);
            }
        } catch (Exception e) {
            log.error("Error occurred in zuul post filter", e);
        }
        return null;
    }

    @Override
    public boolean shouldFilter() {
        boolean shouldfilter = false;
        final RequestContext ctx = RequestContext.getCurrentContext();
        String URI = ctx.getRequest().getRequestURI();

        if (URI.contains(FileMetaData.SIGNING)) {
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
