package com.xietg.kc.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.xietg.kc.config.AppProperties;
import com.xietg.kc.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(AppProperties props) {
        this.algorithm = Algorithm.HMAC256(props.getJwtSecret());
        this.verifier = JWT.require(this.algorithm).build();
    }

    public String createAccessToken(String subjectEmail, String role, long ttlSeconds) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(subjectEmail)
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
                .sign(algorithm);
    }

    public JwtPayload decodeToken(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            String email = jwt.getSubject();
            String role = jwt.getClaim("role").asString();
            return new JwtPayload(email, role);
        } catch (TokenExpiredException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token expired");
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    public record JwtPayload(String subject, String role) {}
}
