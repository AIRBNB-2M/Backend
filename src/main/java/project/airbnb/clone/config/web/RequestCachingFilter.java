package project.airbnb.clone.config.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.support.MultipartResolutionDelegate;

import java.io.IOException;

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Component
public class RequestCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!MultipartResolutionDelegate.isMultipartRequest(request)) {
            request = new CachedHttpServletRequest(request);
        }
        filterChain.doFilter(request, response);
    }
}
