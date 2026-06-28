package com.sky_copilot.ai_copilot.auth.security;

import com.sky_copilot.ai_copilot.user.entity.User;
import com.sky_copilot.ai_copilot.user.repository.UserRepository;
import com.sky_copilot.ai_copilot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
        public UserDetails loadUserByUsername(String email)
                throws UsernameNotFoundException {

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new UsernameNotFoundException("User not found: " + email));

                return new UserPrincipal(user);
        }
}
