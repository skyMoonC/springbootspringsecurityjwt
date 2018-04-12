package com.ifox.springbootspringsecurityjwt.web.filter;

import com.ifox.springbootspringsecurityjwt.exception.TokenException;
import com.ifox.springbootspringsecurityjwt.web.constant.ConstantKey;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author:zhongchao
 * @Organization: ifox
 * @Description:
 * @Date:Created in16:45 2018/4/12
 * @Modified By:
 */
public class JWTAuthenticationFilter extends BasicAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            String user = null;
            try {
                user = Jwts.parser()
                        .setSigningKey(ConstantKey.SIGNING_KEY)
                        .parseClaimsJws(token.replace("Bearer ", ""))
                        .getBody()
                        .getSubject();
                if (user != null) {
                    return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                }
            } catch (ExpiredJwtException e) {
                logger.error("Token已过期: {} " + e);
                throw new TokenException("Token已过期");
            } catch (UnsupportedJwtException e) {
                logger.error("Token格式错误: {} " + e);
                throw new TokenException("Token格式错误");
            } catch (MalformedJwtException e) {
                logger.error("Token没有被正确构造: {} " + e);
                throw new TokenException("Token没有被正确构造");
            } catch (SignatureException e) {
                logger.error("签名失败: {} " + e);
                throw new TokenException("签名失败");
            } catch (IllegalArgumentException e) {
                logger.error("非法参数异常: {} " + e);
                throw new TokenException("非法参数异常");
            }
        }
        return null;
    }
}
