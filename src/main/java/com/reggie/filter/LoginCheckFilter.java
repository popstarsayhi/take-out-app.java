package com.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * check if the user finished logging process
 */

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //URI matcher, support wildcard
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1. get URI
        String requestURI = request.getRequestURI();

        log.info("intercepting: {} ", requestURI);

        //URIs dont need to be checked
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
        };

        //2. checking if URI is in the above urls list
        boolean check = check(urls, requestURI);

        //3. if requestURI is in the urls, release
        if (check) {
            log.info("request can be released: {} ", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4-1.check login status of backends
        if (request.getSession().getAttribute("employee") != null) {

            log.info("user logged in, id is {}", request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            long id = Thread.currentThread().getId();
            log.info("thread id is {}", id);

            filterChain.doFilter(request, response);
            return;
        }


        //4-2.check login status of front
        if (request.getSession().getAttribute("user") != null) {

            log.info("user logged in, id is {}", request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }


        log.info("user had not logged in");

        //5.status is not logged in,respond to client page via stream
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }


    /**
     * URI matcher, to check if the URI needs to be released
     *
     * @param requestURI
     * @param urls
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match == true) {
                return true;
            }
        }
        return false;
    }
}
