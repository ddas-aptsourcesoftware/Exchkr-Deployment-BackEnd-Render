package com.exchkr.club.management.config;

import com.exchkr.club.management.security.CustomAuthenticationEntryPoint;
import com.exchkr.club.management.security.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, CustomAuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CsrfTokenRequestAttributeHandler csrfTokenRequestHandler() {
		CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
		handler.setCsrfRequestAttributeName(null);
		return handler;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				// 1. CORS Configuration
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				// 2. CSRF Configuration
				.csrf(csrf -> csrf
						// UPDATED: Now uses the helper method to set the path to "/"
						.csrfTokenRepository(cookieCsrfTokenRepository())
						.csrfTokenRequestHandler(csrfTokenRequestHandler())
						// Ignore CSRF for login/refresh so they work without a token initially
						.ignoringRequestMatchers("/auth/**", "/auth/refresh-token", "/auth/logout",
								"/api/admin/onboarding/**", "/api/finance/stripe-webhook", "/webhook/stripe/account/**",
								"/webhook/stripe/payment/**", "/api/webhook/plaid/**", "/api/donation/**", "/api/password/**"))

				// 3. Stateless Session
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 4. Exception Handling
				.exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint))

				// 5. Authorization Rules
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/**", "/auth/refresh-token", "/api/password/**", "/api/admin/onboarding/**",
								"/api/finance/stripe-webhook", "/error", "/webhook/stripe/account/**",
								"/webhook/stripe/payment/**", "/api/webhook/plaid/**", "/api/donation/**")
						.permitAll().requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/plaid/**").hasAuthority("ROLE_Officer").anyRequest().authenticated())

				// 6. Filter Chain Integration
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				// Filter to force the creation of the XSRF-TOKEN cookie
				.addFilterAfter(new CsrfCookieFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// Helper method to configure the CSRF Cookie Path
	private CookieCsrfTokenRepository cookieCsrfTokenRepository() {
		CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		repository.setCookiePath("/");
		return repository;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:3000", "https://www.aptcarep.com", "https://aptcarep.com"));
		config.setAllowCredentials(true);
		config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "Authorization"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	/**
	 * INNER CLASS: CsrfCookieFilter
	 */
	private static final class CsrfCookieFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
				FilterChain filterChain) throws ServletException, IOException {

			CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

			if (csrfToken != null) {
				csrfToken.getToken();
			}

			filterChain.doFilter(request, response);
		}
	}
}