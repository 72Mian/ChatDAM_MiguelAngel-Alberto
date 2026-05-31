package es.damdi.alberto.chatdam_cliente.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Mensaje {
    private Long id;
    private String contenido;
    private LocalDate fecha;
    private LocalTime hora;
    private Usuario autor;

    public Mensaje() {}

    public Mensaje(String contenido, Usuario autor) {
        this.contenido = contenido;
        this.autor = autor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
}