package cat.albirar.users.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import cat.albirar.users.config.UsersRegisterConfiguration;
import cat.albirar.users.registration.IRegistrationService;
import cat.albirar.users.test.context.DefaultContextTestConfiguration;

/**
 * Base abstract test class for all test.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DefaultContextTestConfiguration.class, UsersRegisterConfiguration.class})
@WebAppConfiguration
public abstract class UsersRegisterTests extends UsersRegisterAbstractDataTest {
    
    @Autowired
    protected IRegistrationService registrationService;
    
}
