package com.serviciosTransporte_controlDeIdentidad.Controladores;

import com.serviciosTransporte_controlDeIdentidad.Servicios.ServicioUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class ControladorGestionDeUsuarios {

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @GetMapping("/listartodo")
    public ResponseEntity<?> listarUsuarios(){
        return ResponseEntity.ok(servicioUsuarios.findAllUsers());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id){
        servicioUsuarios.deleteUser(id);
        return ResponseEntity.noContent().build() ;
    }
}
