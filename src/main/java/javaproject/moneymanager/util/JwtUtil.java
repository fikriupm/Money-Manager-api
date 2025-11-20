package javaproject.moneymanager.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
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
	 * Generate a signed JWT with subject (email or user id)
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
	 * Backwards-compatible method expected by JwtRequestFilter
	 */
	public String extractUsername(String token) {
		return getSubjectFromToken(token);
	}

	/**
	 * Parse token and return subject (email/user id)
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

	private boolean isTokenExpired(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token)
					.getBody();

			Date expiration = claims.getExpiration();
			return expiration.before(new Date());
		} catch (io.jsonwebtoken.ExpiredJwtException ex) {
			return true;
		}
	}

	/**
	 * Validate token — checks signature, expiration, and username match
	 */
	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			final String username = getSubjectFromToken(token);
			return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Simplified validation (just checks token integrity & expiration)
	 */
	public boolean validateToken(String token) {
		try {
			getSubjectFromToken(token); // will throw if invalid
			return !isTokenExpired(token);
		} catch (Exception ex) {
			return false;
		}
	}
}
