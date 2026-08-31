package com.acme.hrms.common.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final long expirationMs;

    public JwtTokenProvider(JwtEncoder jwtEncoder,
                            @Value("${hrms.security.jwt.expiration-ms:3600000}") long expirationMs) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMs = expirationMs;
    }

    public String generateToken(UUID employeeId, String email, List<String> roles, UUID tenantId) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("hrms-backend")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(employeeId.toString())
                .claim("email", email)
                .claim("preferred_username", email)
                .claim("roles", roles);

        if (tenantId != null) {
            claimsBuilder.claim("tenant_id", tenantId.toString());
        }

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsBuilder.build())).getTokenValue();
    }
}
