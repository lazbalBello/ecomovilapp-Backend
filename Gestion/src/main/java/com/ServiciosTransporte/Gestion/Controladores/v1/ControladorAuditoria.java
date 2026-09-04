package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Dto.AuditLogRegistroDto;
import com.ServiciosTransporte.Gestion.DtoResponse.AuditLogDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioAuditoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/Auditoria/v1")
public class ControladorAuditoria {

    @Autowired
    private ServicioAuditoria servicioAuditoria;

    @PostMapping("/registrar")
    public ResponseEntity<AuditLogDto> registrar(@RequestBody AuditLogRegistroDto dto) {
        return new ResponseEntity<>(servicioAuditoria.registrar(dto), HttpStatus.CREATED);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<AuditLogDto>> listarTodo() {
        return ResponseEntity.ok(servicioAuditoria.listarTodo());
    }

    @DeleteMapping("/limpiar")
    public ResponseEntity<Void> limpiar() {
        servicioAuditoria.limpiar();
        return ResponseEntity.noContent().build();
    }
}
