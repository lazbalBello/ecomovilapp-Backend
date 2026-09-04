package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Dto.AuditLogRegistroDto;
import com.ServiciosTransporte.Gestion.DtoResponse.AuditLogDto;
import com.ServiciosTransporte.Gestion.Modelos.AuditLog;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioAuditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioAuditoria {

    private final IRepositorioAuditoria repositorioAuditoria;

    public AuditLogDto registrar(AuditLogRegistroDto dto) {
        AuditLog log = new AuditLog();
        log.setTimestamp(LocalDateTime.now());
        log.setAccion(dto.getAccion());
        log.setCategoria(dto.getCategoria());
        log.setDetalles(dto.getDetalles());
        log.setUsuario(dto.getUsuario());
        AuditLog guardado = repositorioAuditoria.save(log);
        return toDto(guardado);
    }

    public List<AuditLogDto> listarTodo() {
        return repositorioAuditoria.findAllByOrderByTimestampDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void limpiar() {
        repositorioAuditoria.deleteAll();
    }

    private AuditLogDto toDto(AuditLog log) {
        return new AuditLogDto(
                String.valueOf(log.getId()),
                log.getTimestamp(),
                log.getAccion(),
                log.getCategoria(),
                log.getDetalles(),
                log.getUsuario()
        );
    }
}
