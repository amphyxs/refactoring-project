package com.par.parapp.controller;

import com.par.parapp.dto.*;
import com.par.parapp.model.User;
import com.par.parapp.service.AuthService;
import com.par.parapp.service.UserActivityService;
import com.par.parapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/activity")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserActivityController {

    private final AuthService authService;

    private final UserService userService;

    private final UserActivityService userActivityService;

    public UserActivityController(AuthService authService, UserService userService,
            UserActivityService userActivityService) {
        this.userService = userService;
        this.authService = authService;
        this.userActivityService = userActivityService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping()
    public ResponseEntity<Object> submitActivity(@Valid @RequestBody ActivityRequest activityRequest,
            HttpServletRequest httpServletRequest) {
        String login = authService.getLoginFromToken(httpServletRequest);
        User user = userService.getUserByLogin(login);
        userActivityService.saveUserActivity(user, activityRequest.getText());
        return new ResponseEntity<>(new MessageResponse("Запись успешно опубликована!"), HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllActivities(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String login) {

        return ResponseEntity.ok(userActivityService.getAllActivities(login, page, size));

    }

}
