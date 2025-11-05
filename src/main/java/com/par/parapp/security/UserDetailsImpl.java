package com.par.parapp.security;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.par.parapp.model.User;

public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private String login;

    @JsonIgnore
    private String password;

    private String status;
    private String email;

    private LocalDate lastLoginDate;

    private LocalDate registrationDate;

    private Collection<? extends GrantedAuthority> authorities;

    private Boolean isTutorialCompleted;

    public static class UserDetailsParams {
        private String login;
        private String password;
        private String status;
        private String email;
        private LocalDate lastLoginDate;
        private LocalDate registrationDate;
        private Collection<? extends GrantedAuthority> authorities;
        private Boolean isTutorialCompleted;

        public void setLogin(String login) {
            this.login = login;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setLastLoginDate(LocalDate lastLoginDate) {
            this.lastLoginDate = lastLoginDate;
        }

        public void setRegistrationDate(LocalDate registrationDate) {
            this.registrationDate = registrationDate;
        }

        public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
        }

        public void setIsTutorialCompleted(Boolean isTutorialCompleted) {
            this.isTutorialCompleted = isTutorialCompleted;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getStatus() {
            return status;
        }

        public String getEmail() {
            return email;
        }

        public LocalDate getLastLoginDate() {
            return lastLoginDate;
        }

        public LocalDate getRegistrationDate() {
            return registrationDate;
        }

        public Collection<GrantedAuthority> getAuthorities() {
            return (Collection<GrantedAuthority>) authorities;
        }

        public Boolean getIsTutorialCompleted() {
            return isTutorialCompleted;
        }
    }

    public UserDetailsImpl(UserDetailsParams params) {
        this.login = params.getLogin();
        this.password = params.getPassword();
        this.status = params.getStatus();
        this.email = params.getEmail();
        this.lastLoginDate = params.getLastLoginDate();
        this.registrationDate = params.getRegistrationDate();
        this.authorities = params.getAuthorities();
        this.isTutorialCompleted = params.getIsTutorialCompleted();
    }

    public static UserDetailsImpl build(User user) {

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(roles -> new SimpleGrantedAuthority(roles.getName().name()))
                .collect(Collectors.toList());

        UserDetailsParams params = new UserDetailsParams();
        params.setLogin(user.getLogin());
        params.setPassword(user.getPassword());
        params.setStatus(user.getStatus());
        params.setEmail(user.getEmail());
        params.setLastLoginDate(user.getLastLoginDate());
        params.setRegistrationDate(user.getRegistrationDate());
        params.setAuthorities(authorities);
        params.setIsTutorialCompleted(user.getIsTutorialCompleted());
        return new UserDetailsImpl(params);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return null;
    }

    public String getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getLastLoginDate() {
        return lastLoginDate;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLastLoginDate(LocalDate lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Boolean getIsTutorialCompleted() {
        return isTutorialCompleted;
    }

    public void setIsTutorialCompleted(Boolean isTutorialCompleted) {
        this.isTutorialCompleted = isTutorialCompleted;
    }
}
