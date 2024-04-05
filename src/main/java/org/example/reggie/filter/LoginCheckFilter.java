package org.example.reggie.filter;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContext;
import org.example.reggie.common.R;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Check if users login or not
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // Path matcher
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. Get URI of the current request
        String requestURI = request.getRequestURI();

        log.info("Block the request: {}", requestURI);

        // Define some urls that needn't be filtered
        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg", // 移动端发送短信
                "/user/login" // 移动端登陆
        };

        // 2. Check if the current request should be handled
        boolean check = check(urls, requestURI);

        // 3. If not, then pass it
        if (check) {
            log.info("The current request {} needn't be handled.", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("The user has not logged.");
        // 4.1 Check the status, if already logging, pass it
        if (request.getSession().getAttribute("employee") != null) {
            log.info("The user has already logged and the user's ID is: {}", request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        // 4.2 Check the status, if already logging, pass it -- mobile-side users
        if (request.getSession().getAttribute("user") != null) {
            log.info("The user has already logged and the user's ID is: {}", request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        // 5. If not, redirect to the login page
        // 判断逻辑根据前端页面的相应拦截器里规定的内容
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * Check if the request should be filtered
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
