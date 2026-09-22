package com.ServiciosTransporte.Gestion.Controladores.v1;

import com.ServiciosTransporte.Gestion.Auditoria.Dto.AuditoriaFilterDto;
import com.ServiciosTransporte.Gestion.Auditoria.Dto.AuditoriaResponseDto;
import com.ServiciosTransporte.Gestion.Auditoria.Servicios.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/Auditoria/v1")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin') or hasRole('ADMIN')")
public class ControladorAuditoria {

    private final AuditoriaService auditoriaService;

    /**
     * Consulta paginada y filtrada del histórico de auditorías del sistema.
     * Accessible solo por usuarios con rol de administrador.
     */
    @GetMapping("/listar")
    public ResponseEntity<Page<AuditoriaResponseDto>> obtenerAuditorias(
            AuditoriaFilterDto filtro,
            @PageableDefault(size = 20, sort = "fechaOperacion", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AuditoriaResponseDto> resultado = auditoriaService.obtenerAuditorias(filtro, pageable);
        return ResponseEntity.ok(resultado);
    }
}
