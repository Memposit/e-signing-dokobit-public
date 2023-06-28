package com.memposit.esigning.dokobit.dto;

import com.netflix.zuul.http.HttpServletRequestWrapper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {


    public CustomHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        if (request.getCookies() != null) {
            this.cookies = Arrays.asList(request.getCookies());
        } else cookies = new ArrayList<>();
    }

    private final Map<String, String> headerMap = new HashMap<>();

    private final List<Cookie> cookies;

    @Override
    public String getHeader(String name) {
        String headerValue = super.getHeader(name);
        if (headerMap.containsKey(name)) {
            headerValue = headerMap.get(name);
        }
        return headerValue;
    }

    /**
     * get the Header names
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        for (String name : headerMap.keySet()) {
            if(!name.equals("host")){
            names.add(name);}
        }
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
        }
        return Collections.enumeration(values);
    }


    @Override
    public Cookie[] getCookies() {
        return this.cookies.toArray(new Cookie[0]);
    }

    public void addCookie(Cookie cookie) {
        if (!this.cookies.contains(cookie)) {
            this.cookies.add(cookie);
        }
    }


}
