package com.memposit.esigning.dokobit.zuul;

import com.memposit.esigning.dokobit.dto.CustomHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.memposit.esigning.dokobit.common.Constants.Auth.ACCESS_TOKEN;
import static com.memposit.esigning.dokobit.common.Constants.Auth.CODE;
import static com.memposit.esigning.dokobit.common.Constants.Params.RETURN_URL;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static org.apache.http.HttpHeaders.*;

@Component
@RequiredArgsConstructor
public class PreAuthZuulFilter extends ZuulFilter {
    @Value("${dokobit.api.token}")
    private String apiToken;
    @Value("${dokobit.api.redirectUrl}")
    private String redirectUrl;
    private final ObjectMapper objectMapper;


    @SneakyThrows
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        ObjectNode requestBody = objectMapper.createObjectNode().put(RETURN_URL, redirectUrl);
        ctx.setRequestQueryParams(addParams());
        addHeaders(ctx);
        ctx.setRequest(new CustomHttpServletRequest(req, objectMapper.writeValueAsBytes(requestBody)));
        return null;
    }

    private Map<String, List<String>> addParams() {
        Map<String, List<String>> params = new HashMap<>();
        params.put(ACCESS_TOKEN, Lists.newArrayList(apiToken));
        return params;
    }

    private void addHeaders(RequestContext ctx) {
        ctx.addZuulRequestHeader(CONTENT_TYPE, APPLICATION_JSON);
        ctx.addZuulRequestHeader(CONNECTION, KEEP_ALIVE);
    }


    @Override
    public boolean shouldFilter() {
        boolean shouldfilter = false;
        final RequestContext ctx = RequestContext.getCurrentContext();
        String URI = ctx.getRequest().getRequestURI();

        if (URI.contains(CODE)) {
            shouldfilter = true;
        }

        return shouldfilter;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public String filterType() {
        return "pre";
    }

}
