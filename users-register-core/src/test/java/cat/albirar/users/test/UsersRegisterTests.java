package cat.albirar.users.test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import cat.albirar.users.config.UsersRegisterConfiguration;
import cat.albirar.users.models.tokens.AbstractTokenBean.AbstractTokenBeanBuilder;
import cat.albirar.users.models.tokens.ETokenClass;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.registration.IRegistrationService;
import cat.albirar.users.repos.IAccountRepo;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.test.context.DefaultContextTestConfiguration;
import cat.albirar.users.verification.ITokenManager;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 * Base abstract test class for all test.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UsersRegisterConfiguration.class, DefaultContextTestConfiguration.class})
@WebAppConfiguration
@DirtiesContext
public abstract class UsersRegisterTests extends UsersRegisterAbstractDataTest {

    @Autowired
    protected IUserRepo userRepo;
    @Autowired
    protected IAccountRepo accountRepo;
    @Autowired
    protected IRegistrationService registrationService;
    @Autowired
    protected ITokenManager tokenManager;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    
    protected <T extends AbstractTokenBeanBuilder<?,?>> T buildAbstractToken(T tokenBeanBuilder) {
        tokenBeanBuilder
        .tokenId(SAMPLE_ID)
        .issued(LocalDateTime.now().minusDays(1).withNano(0))
        .expire(LocalDateTime.now().plusDays(5).withNano(0))
        .idUser(SAMPLE_CREATED_USER.getId())
        .username(SAMPLE_CREATED_USER.getUsername())
        ;
        return tokenBeanBuilder;
    }
    
    
    protected <T extends AbstractTokenBeanBuilder<?,?>> T buildAbstractToken(T tokenBeanBuilder, UserBean user) {
        buildAbstractToken(tokenBeanBuilder);
        tokenBeanBuilder
        .idUser(user.getId())
        .username(user.getUsername())
        ;
        return tokenBeanBuilder;
    }

    protected JwtBuilder createJwtBuilder() {
        SecretKey jwsSecretKey;
        
        jwsSecretKey = (SecretKey) ReflectionTestUtils.getField(tokenManager, "jwsSecretKey");
        // Builder
        return Jwts.builder()
                .claim("XXX", "XXX")
                .signWith(jwsSecretKey)
                ;
    }

    protected JwtBuilder addTokenInformation(JwtBuilder builder, ETokenClass tokenClass, UserBean user) {
        builder.setId(UUID.randomUUID().toString())
        .setIssuer((String)ReflectionTestUtils.getField(tokenManager, "issuer"))
        .setSubject(user.getUsername())
        .setIssuedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
        .setExpiration(Date.from(LocalDateTime.now().plusDays((int)ReflectionTestUtils.getField(tokenManager, "daysToExpire")).atZone(ZoneId.systemDefault()).toInstant()))
        .claim(ITokenManager.CLAIM_USERID, user.getId())
        .claim(ITokenManager.CLAIM_TOKEN_CLASS, tokenClass.name())
        ;
        return builder;
    }
    /**
     * Assert that the two users are equals ensuring that password are not encoded for equals and {@link UserBean#getAuthorities()} are sorted.
     * @param user1 The first user to compare
     * @param user2 The second user to compare
     * @return true if equals and false if not
     */
    protected boolean equalsEnsurePasswordAuthorities(UserBean user1, UserBean user2) {
        return (passwordEncoder.matches(user1.getPassword(), user2.getPassword())
                && user1.toBuilder()
                            .password("XXX")
                            .authorities(user1.getAuthorities().stream().sorted().collect(Collectors.toList()))
                            .build()
                .equals(user2.toBuilder()
                            .password("XXX")
                            .authorities(user2.getAuthorities().stream().sorted().collect(Collectors.toList()))
                            .build()
                ));
    }
}
