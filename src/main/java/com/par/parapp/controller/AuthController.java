package com.par.parapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.par.parapp.dto.MessageResponse;
import com.par.parapp.dto.SignInRequest;
import com.par.parapp.dto.SignUpRequest;
import com.par.parapp.dto.UserDataResponse;
import com.par.parapp.service.AuthService;

@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("sign-in")
    public ResponseEntity<Object> authUser(@RequestBody SignInRequest signInRequest) {
        UserDataResponse userDataResponse = authService.loginAsUser(signInRequest.getLogin(),
                signInRequest.getPassword());
        UserDataResponse response = new UserDataResponse(
                userDataResponse.getJwt(),
                userDataResponse.getLogin(),
                userDataResponse.getRoles(),
                userDataResponse.getIsTutorialCompleted());
        return ResponseEntity.ok(response);
    }

    @PostMapping("sign-up")
    public ResponseEntity<Object> registerUser(@RequestBody SignUpRequest signUpRequest) {
        authService.saveUser(signUpRequest.getLogin(), signUpRequest.getPassword(),
                signUpRequest.getEmail(), signUpRequest.getIsDev());
        return new ResponseEntity<>(new MessageResponse("Пользователь успешно зарегистрирован!"), HttpStatus.CREATED);
    }

}
