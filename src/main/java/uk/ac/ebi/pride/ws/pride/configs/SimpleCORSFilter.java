package uk.ac.ebi.pride.ws.pride.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SimpleCORSFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (((request.getHeader("Access-Control-Request-Method") != null
                || ("GET".equals(request.getMethod()))
                || ("POST".equals(request.getMethod())))
                || ("OPTIONS".equals(request.getMethod()))
                || ("DELETE".equals(request.getMethod()))
                || ("PUT".equals(request.getMethod())))) {
            // CORS "pre-flight" request
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Access-Control-Allow-Headers, Authorization");
            /*response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,accept,Origin,"
                    + "Access-Control-Request-Method,Access-Control-Request-Headers");*/
            /*response.addHeader("cors.support.credentials", "true");
            response.addHeader("cors.exposed.headers", "Access-Control-Allow-Origin,"
                    + "Access-Control-Allow-Credentials");*/
        }
        log.info("########### Simple CORS Filter: " + request.getRequestURL() + " ############");

        /*filterChain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) request) {
            @Override
            public String getRequestURI() {
                // return what you want
                String requestURL = request.getRequestURL().toString();
                if(requestURL.contains("/v2")){
                    return requestURL.replace("/v2","");
                }else{
                    return requestURL;
                }
            }
        }, response);*/

        filterChain.doFilter(request, response);
    }
}