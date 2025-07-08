package com.killrvideo.security;

import java.util.Optional;

import com.killrvideo.dao.UserDao;
import com.killrvideo.dto.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userDao.findByEmail(username);

        if (!userOpt.isPresent()) {
            //System.out.println("User Not Found with email: " + username);
            throw new UsernameNotFoundException("User Not Found with email: " + username);
        }

        User user = userOpt.get();

        //System.out.println("User found: " + user.getEmail());
        
        return UserDetailsImpl.build(user);
    }
}