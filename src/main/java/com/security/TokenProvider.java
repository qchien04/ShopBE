package com.security;


import com.constant.JwtConstant;
import com.service.implement.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TokenProvider {

    SecretKey key= Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

    public String genarateToken(Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return Jwts.builder().setIssuer("Achien").setIssuedAt(new Date()).setExpiration(new Date(new Date().getTime()+864000000))
                .claim("id",userDetails.getId())
                .claim("email",authentication.getName())
                .claim("authorities", authentication.getAuthorities())
                .signWith(key).compact();
    }

    public String getEmailFromToken(String jwt) {
        try {
            jwt = jwt.substring(7);
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
            return String.valueOf(claims.get("email"));
        } catch (JwtException e) {
            throw new IllegalStateException("Invalid token", e);
        }
    }

    public Long getUserIdFromToken(String jwt) {
        try {
            if (jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            return Long.parseLong(claims.get("id").toString());
        } catch (JwtException e) {
            throw new IllegalStateException("Token không hợp lệ!", e);
        }
    }

    public String getRoleFromToken(String jwt) {
        try {
            if (jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);
            }
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            List<?> authorities = claims.get("authorities", List.class);
            if (authorities.isEmpty()) {
                return "CLIENT";
            }
            Object first = authorities.getFirst();
            if (first instanceof Map<?,?>) {
                System.out.println("Tao la: "+((Map<?, ?>) first).get("authority").toString());
                return ((Map<?, ?>) first).get("authority").toString();
            }
            return first.toString();
        } catch (JwtException e) {
            throw new IllegalStateException("Token không hợp lệ!", e);
        }
    }

}
