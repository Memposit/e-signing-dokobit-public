package com.memposit.esigning.dokobit.zuul;

import com.memposit.esigning.dokobit.dto.FilesDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.memposit.esigning.dokobit.common.Constants.Auth;
import com.memposit.esigning.dokobit.common.Constants.FileMetaData;
import com.memposit.esigning.dokobit.common.Constants.FileType;
import com.memposit.esigning.dokobit.common.Constants.MobileId;
import com.memposit.esigning.dokobit.common.Constants.Params;
import com.memposit.esigning.dokobit.common.Constants.UserInfo;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.IntStream;

import static com.memposit.esigning.dokobit.zuul.PostSigningZuulFilter.SESSION_SIGNING_TOKEN;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.bouncycastle.pqc.crypto.xmss.XMSSKeyParameters.SHA_256;

@Component
@RequiredArgsConstructor
public class PreSigningZuulFilter extends ZuulFilter {

    @Value("${dokobit.api.token}")
    private String apiToken;
    private final ObjectMapper objectMapper;

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        final String requestURI = req.getRequestURI();
        ctx.setRequestQueryParams(addParams());
        if (requestURI.contains(Auth.CODE)) {
            try {
                Optional<FilesDto> filesDto = Optional.of(objectMapper.readValue(req.getInputStream(), FilesDto.class));
                filesDto.ifPresent(files -> addHeaders(ctx, files, req));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (requestURI.contains(Auth.STATUS)) {
            HttpServletRequest modifiedRequest = new HttpServletRequestWrapper((req)) {
                @Override
                public String getRequestURI() {
                    return req.getRequestURI() + extractCookie(req, SESSION_SIGNING_TOKEN) + Params.JSON;
                }
            };
            ctx.setRequest(modifiedRequest);
        }
        return null;
    }

    private void addHeadersFileContent(RequestContext requestContext, FilesDto filesDto) {
        List<MultipartFile> files = filesDto.getFiles();
        IntStream.range(0, files.size())
                .filter(index -> !files.get(index).getName().isEmpty())
                .forEach(index -> {
                    try {
                        MultipartFile file = files.get(index);
                        requestContext.addZuulRequestHeader(
                            FileMetaData.PDF_FILES + index + FileMetaData.NAME, file.getOriginalFilename());
                        requestContext.addZuulRequestHeader(
                            FileMetaData.PDF_FILES + index + FileMetaData.CONTENT, Base64.getEncoder().encodeToString(file.getBytes()));
                        requestContext.addZuulRequestHeader(
                            FileMetaData.PDF_FILES + index + FileMetaData.DIGEST, Base64.getEncoder().encodeToString(MessageDigest
                                .getInstance(SHA_256)
                                .digest(file.getBytes())));
                    } catch (IOException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void addHeaders(RequestContext requestContext, FilesDto filesDto, HttpServletRequest req) {
        requestContext.addZuulRequestHeader(FileType.TYPE, FileType.PDF);
        requestContext.addZuulRequestHeader(
            MobileId.MOBILE_ID_PHONE, extractCookie(req, MobileId.MOBILE_ID_PHONE));
        requestContext.addZuulRequestHeader(Auth.CODE, extractCookie(req, MobileId.MOBILE_ID_CODE));
        requestContext.addZuulRequestHeader(UserInfo.LANGUAGE, UserInfo.DEFAULT_LANGUAGE);
        requestContext.addZuulRequestHeader(UserInfo.COUNTRY, UserInfo.DEFAULT_COUNTRY);
        requestContext.addZuulRequestHeader(FileMetaData.CONTACT, filesDto.getUserInfo().getUserName());
        requestContext.addZuulRequestHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
        addHeadersFileContent(requestContext, filesDto);
    }

    private Map<String, List<String>> addParams() {
        Map<String, List<String>> params = new HashMap<>();
        params.put(Auth.ACCESS_TOKEN, Lists.newArrayList(apiToken));
        return params;
    }

    private String extractCookie(HttpServletRequest req, String name) {
        final Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public boolean shouldFilter() {
        boolean shouldfilter = false;
        final RequestContext ctx = RequestContext.getCurrentContext();
        String URI = ctx.getRequest().getRequestURI();

        if (URI.contains(FileMetaData.SIGNING) |
                URI.contains(Auth.STATUS)) {
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
