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

        // 2. If no need login url, then pass it
        if (isNoLoginNeeded(requestURI)) {
            log.info("The current request {} needn't be handled.", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        log.info("The user has not logged.");

        // 3. check the login status from desktop & mobile
        String[] userIDs = new String[] {"employee", "user"};
        for (String userID : userIDs) {
            if (request.getSession().getAttribute(userID) != null) {
                log.info("The user has already logged and the user's ID is: {}", request.getSession().getAttribute(userID));

                Long id = (Long) request.getSession().getAttribute(userID);
                BaseContext.setCurrentId(id);

                filterChain.doFilter(request, response);
                return;
            }
        }

        // 4. If not, redirect to the login page
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
    private boolean isNoLoginNeeded(String requestURI) {
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
        
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
