package com.serviciosTransporte_controlDeIdentidad.Controladores;

import com.serviciosTransporte_controlDeIdentidad.Dtos.LoginDto;
import com.serviciosTransporte_controlDeIdentidad.Dtos.RefreshToken;
import com.serviciosTransporte_controlDeIdentidad.Dtos.TokenResponse;
import com.serviciosTransporte_controlDeIdentidad.Dtos.UserDto;
import com.serviciosTransporte_controlDeIdentidad.Servicios.ServicioUsuarios;
import com.serviciosTransporte_controlDeIdentidad.Utils.TokenClient;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class ControladorAutenticacion {

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @Autowired
    private TokenClient tokenClient;

    @PostMapping("/registrar-admin")
    public ResponseEntity<Void> registrarAdmin(@Valid @RequestBody UserDto userDto){
        servicioUsuarios.createAdmin(userDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/registrar-user")
    public ResponseEntity<Void> registrarUser(@Valid @RequestBody UserDto userDto){
        servicioUsuarios.createUser(userDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginDto loginDto){
        TokenResponse response = servicioUsuarios.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refrescar")
    public ResponseEntity<TokenResponse> refrescar(@Valid @RequestBody RefreshToken token){
        TokenResponse newTokens = tokenClient.refresh(token.refreshToken());
        return ResponseEntity.ok(newTokens);
    }
}
