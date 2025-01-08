package com.par.parapp.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.par.parapp.config.jwt.AuthTokenFilter;
import com.par.parapp.config.jwt.JwtUtils;
import com.par.parapp.dto.UserDataResponse;
import com.par.parapp.exception.ResourceAlreadyExist;
import com.par.parapp.exception.ResourceNotFoundException;
import com.par.parapp.model.ERole;
import com.par.parapp.model.Role;
import com.par.parapp.model.User;
import com.par.parapp.model.Wallet;
import com.par.parapp.repository.RoleRepository;
import com.par.parapp.repository.UserRepository;
import com.par.parapp.repository.WalletRepository;
import com.par.parapp.security.UserDetailsImpl;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final WalletRepository walletRepository;

    private final AuthTokenFilter authTokenFilter;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, WalletRepository walletRepository,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils, AuthTokenFilter authTokenFilter) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletRepository = walletRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.authTokenFilter = authTokenFilter;
    }

    public String getLoginFromToken(HttpServletRequest httpServletRequest) {
        return jwtUtils.getLoginFromJwtToken(authTokenFilter.parseJwt(httpServletRequest));
    }

    public User saveUser(String login, String password, String email, Boolean isDev) {

        if (Boolean.TRUE.equals(userRepository.existsByLogin(login)))
            throw new ResourceAlreadyExist("Этот логин уже занят. Попробуйте другой");

        if (Boolean.TRUE.equals(userRepository.existsByEmail(email)))
            throw new ResourceAlreadyExist("Эта электронная почта уже занята. Попробуйте другую");

        User user = new User(login, "Не в сети",
                passwordEncoder.encode(password),
                email, LocalDate.now());

        Set<Role> roles = new HashSet<>();

        if (Boolean.TRUE.equals(isDev)) {
            Role devRole = roleRepository
                    .findByName(ERole.ROLE_DEV)
                    .orElseThrow(ResourceNotFoundException::new);

            roles.add(devRole);
        }

        Role userRole = roleRepository
                .findByName(ERole.ROLE_USER)
                .orElseThrow(ResourceNotFoundException::new);

        roles.add(userRole);

        Wallet wallet = new Wallet(0.0);
        walletRepository.save(wallet);

        user.setRoles(roles);
        user.setWallet(wallet);
        userRepository.save(user);
        return user;
    }

    public UserDataResponse loginAsUser(String login, String password) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        login,
                        password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        userRepository.loginAsUser(login);

        return new UserDataResponse(jwt, userDetails.getLogin(), roles, userDetails.getIsTutorialCompleted());
    }

}
