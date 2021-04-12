/*
 * Copyright (C) 2020 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.UUID;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import io.quarkus.security.AuthenticationFailedException;
import io.smallrye.jwt.KeyUtils;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import org.eclipse.microprofile.jwt.Claims;

public class TokenUtils {

  private static final String ISSUER = "https://api.stackgres.io/auth";
  private static final String AUDIENCE = "api-websecurity";

  private TokenUtils() {
    // utility class
  }

  /**
   * Utility method to generate a JWT string from a JSON resource file that is signed by the
   * privateKey.pem test resource key, possibly with invalid fields.
   *
   * @return the JWT string
   */
  public static String generateTokenString(String k8sUsername,
      String preferredUsername, long duration, String privateKeyPath) {
    PrivateKey privateKey;
    try {
      privateKey = KeyUtils.decodePrivateKey(Files.asCharSource(
          Paths.get(privateKeyPath).toFile(), StandardCharsets.UTF_8)
          .read());
    } catch (IOException | GeneralSecurityException e) {
      throw new AuthenticationFailedException();
    }

    long currentTimeInSecs = currentTimeInSecs();
    long exp = currentTimeInSecs + duration;

    JwtClaimsBuilder claims = Jwt.claims();
    claims.issuer(ISSUER)
        .audience(AUDIENCE)
        .claim(Claims.jti.name(), UUID.randomUUID())
        .subject(k8sUsername)
        .preferredUserName(preferredUsername)
        .issuedAt(currentTimeInSecs)
        .claim(Claims.auth_time.name(), currentTimeInSecs)
        .expiresAt(exp);

    return claims.jws().sign(privateKey);
  }

  /**
   * Return SHA256 of a password.
   *
   * @param password the password
   * @return SHA256 of password
   */
  public static String sha256(String password) {
    return Hashing.sha256()
        .hashString(password, StandardCharsets.UTF_8)
        .toString();
  }

  /**
   * Get the current time in seconds.
   *
   * @return the current time in seconds since epoch
   */
  public static int currentTimeInSecs() {
    long currentTimeMS = System.currentTimeMillis();
    return (int) (currentTimeMS / 1000);
  }

}
