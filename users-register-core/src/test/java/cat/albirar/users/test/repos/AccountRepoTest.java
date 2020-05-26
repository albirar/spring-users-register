/*
 * This file is part of "albirar users-register".
 * 
 * "albirar users-register" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar users-register" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.test.repos;

import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.repos.IAccountRepo;
import cat.albirar.users.test.UsersRegisterTests;

/**
 * The main test for {@link IAccountRepo}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class AccountRepoTest extends UsersRegisterTests {
    @Test
    public void testFindAll() {
        List<AccountBean> r;
        
        r = accountRepo.findAll();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(ACCOUNTS.length, r.size());
        Assertions.assertArrayEquals(ACCOUNTS, r.toArray());
    }
    
    @Test
    public void testFindById() {
        Optional<AccountBean> ac;
        
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.findById(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.findById(""));
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.findById("   "));
     
        ac = accountRepo.findById(DUMMY_ID);
        Assertions.assertNotNull(ac);
        Assertions.assertFalse(ac.isPresent());
        
        for(AccountBean b : ACCOUNTS) {
            ac = accountRepo.findById(b.getId());
            Assertions.assertNotNull(ac);
            Assertions.assertTrue(ac.isPresent());
            Assertions.assertEquals(b, ac.get());
        }
    }
    
    @Test
    public void testFindByName() {
        Optional<AccountBean> ac;
        
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.findByName(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.findByName(""));
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.findByName("   "));
     
        ac = accountRepo.findByName(DUMMY_USERNAME);
        Assertions.assertNotNull(ac);
        Assertions.assertFalse(ac.isPresent());
        
        for(AccountBean b : ACCOUNTS) {
            ac = accountRepo.findByName(b.getName());
            Assertions.assertNotNull(ac);
            Assertions.assertTrue(ac.isPresent());
            Assertions.assertEquals(b, ac.get());
        }
    }
    
    @Test
    public void testSave() {
        
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.save(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.save(AccountBean.builder().build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.save(AccountBean.builder().name("").build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> accountRepo.save(AccountBean.builder().name("    ").build()));
        
        // DataIntegrityViolationException for name on create
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> accountRepo.save(ACCOUNTS[0].toBuilder().id(null).build()));
        // DataIntegrityViolationException for name on update
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> accountRepo.save(ACCOUNTS[0].toBuilder().name(ACCOUNTS[1].getName()).build()));

        {
            // Creating OK
            AccountBean ac, ac1;
            
            ac = ACCOUNTS[0].toBuilder().id(null).name(DUMMY_NAME).build();
            ac1 = accountRepo.save(ac);
            Assertions.assertNotNull(ac1);
            Assertions.assertTrue(StringUtils.hasText(ac1.getId()));
            ac.setId(ac1.getId());
            Assertions.assertEquals(ac, ac1);
            
            // Creating with id not found
            ac = ACCOUNTS[0].toBuilder().id(DUMMY_ID).name(DUMMY_NAME_1).build();
            ac1 = accountRepo.save(ac);
            Assertions.assertNotNull(ac1);
            Assertions.assertEquals(ac, ac1);
            
        }
        {
            // Updating OK
            AccountBean ac, ac1;
            
            ac = ACCOUNTS[0].toBuilder().name(DUMMY_NAME_2).build();
            ac1 = accountRepo.save(ac);
            Assertions.assertNotNull(ac1);
            Assertions.assertEquals(ac, ac1);
        }
    }
}
