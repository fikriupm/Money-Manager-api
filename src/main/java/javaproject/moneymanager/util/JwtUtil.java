package javaproject.moneymanager.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

	@Value("${jwt.secret:changeit_changeit_changeit_changeit}")
	private String jwtSecret;

	@Value("${jwt.expirationMinutes:60}")
	private long expirationMinutes;

	/**
	 * Generate a signed JWT containing the given subject (typically the user's email or id).
	 * @param subject subject to place in the token (e.g. user email)
	 * @return compact JWT string
	 */
	public String generateToken(String subject) {
		if (subject == null) {
			throw new IllegalArgumentException("JWT subject cannot be null");
		}

		SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		Instant now = Instant.now();

		return Jwts.builder()
				.setSubject(subject)
				.setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plus(Duration.ofMinutes(expirationMinutes))))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	/**
	 * Parse token and return subject (email/user id). Throws runtime exception if token is invalid/expired.
	 */
	public String getSubjectFromToken(String token) {
		SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.getSubject();
	}

	/**
	 * Validate token (returns true when token can be parsed and not expired).
	 */
	public boolean validateToken(String token) {
		try {
			getSubjectFromToken(token);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
