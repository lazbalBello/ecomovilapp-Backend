package com.ServiciosTransporte.ControlDeIdentidad.Utils;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.ConductorUsuarioDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.LoginDto;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.TokenResponse;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.UserDto;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IKeycloakService {

    List<UserRepresentation> findAllUsers();
    List<UserRepresentation> searchUserByUsername(String username);
    void createAdmin(UserDto userDto);
    void createUser(UserDto userDto);
    void createConductor(ConductorUsuarioDto userDto);
    TokenResponse login(LoginDto loginDto);
    void deleteUser(String userId);
}
