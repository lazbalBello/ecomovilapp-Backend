package com.serviciosTransporte_controlDeIdentidad.Utils;

import com.serviciosTransporte_controlDeIdentidad.Dtos.LoginDto;
import com.serviciosTransporte_controlDeIdentidad.Dtos.TokenResponse;
import com.serviciosTransporte_controlDeIdentidad.Dtos.UserDto;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IKeycloakService {

    List<UserRepresentation> findAllUsers();
    List<UserRepresentation> searchUserByUsername(String username);
    void createAdmin(UserDto userDto);
    void createUser(UserDto userDto);
    TokenResponse login(LoginDto loginDto);
    void deleteUser(String userId);
}
