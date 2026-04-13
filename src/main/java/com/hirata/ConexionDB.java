package com.hirata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * ConexionDB
 * Maneja la conexión y operaciones CRUD con la base de datos MySQL.
 * Empresa de Transporte Hirata - Sistema de Gestión de Flota
 */
public class ConexionDB {

    private static final String URL     = "jdbc:mysql://localhost:3306/hirata_flota";
    private static final String USUARIO = "root";
    private static final String CLAVE   = "";

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    // ======================== KILOMETRAJE ========================

    public void registrarKilometraje(String patente, int kilometraje) {
        String sql = "INSERT INTO registro_kilometraje (patente, kilometraje) VALUES (?, ?)";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            ps.setInt(2, kilometraje);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int obtenerKilometrajeTotalPorPatente(String patente) {
        String sql = "SELECT COALESCE(SUM(kilometraje), 0) FROM registro_kilometraje WHERE patente = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lista todos los registros de kilometraje ordenados por fecha descendente.
     * @return Lista de arreglos {id, patente, kilometraje, fecha}
     */
    public List<String[]> listarKilometrajes() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, patente, kilometraje, fecha FROM registro_kilometraje ORDER BY fecha DESC";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("patente"),
                    String.valueOf(rs.getInt("kilometraje")),
                    rs.getString("fecha")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene el kilometraje acumulado agrupado por patente (para gráfico).
     * @return Lista de arreglos {patente, totalKm} ordenado por total descendente
     */
    public List<String[]> obtenerKilometrajePorCamion() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT patente, COALESCE(SUM(kilometraje), 0) AS total " +
                     "FROM registro_kilometraje GROUP BY patente ORDER BY total DESC";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("patente"),
                    String.valueOf(rs.getInt("total"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ======================== CONDUCTORES ========================

    public void agregarConductor(String nombre, String rut, String telefono, String email) {
        String sql = "INSERT INTO conductores (nombre, rut, telefono, email) VALUES (?, ?, ?, ?)";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, rut);
            ps.setString(3, telefono);
            ps.setString(4, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> listarConductores() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, rut, telefono, email FROM conductores ORDER BY nombre";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("nombre"),
                    rs.getString("rut"),
                    rs.getString("telefono") != null ? rs.getString("telefono") : "",
                    rs.getString("email") != null ? rs.getString("email") : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void actualizarConductor(int id, String nombre, String rut, String telefono, String email) {
        String sql = "UPDATE conductores SET nombre = ?, rut = ?, telefono = ?, email = ? WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, rut);
            ps.setString(3, telefono);
            ps.setString(4, email);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean eliminarConductor(int id) {
        String sql = "DELETE FROM conductores WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ======================== CAMIONES ========================

    public void agregarCamion(String patente, String marca, String modelo, int anio, Integer conductorId) {
        String sql = "INSERT INTO camiones (patente, marca, modelo, anio, conductor_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            ps.setString(2, marca);
            ps.setString(3, modelo);
            ps.setInt(4, anio);
            if (conductorId != null) ps.setInt(5, conductorId);
            else ps.setNull(5, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> listarCamiones() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.patente, c.marca, c.modelo, c.anio, " +
                     "COALESCE(d.nombre, 'Sin asignar') AS conductor_nombre, c.conductor_id " +
                     "FROM camiones c LEFT JOIN conductores d ON c.conductor_id = d.id " +
                     "ORDER BY c.patente";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String conductorId = rs.getString("conductor_id");
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("patente"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    String.valueOf(rs.getInt("anio")),
                    rs.getString("conductor_nombre"),
                    conductorId != null ? conductorId : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void actualizarCamion(int id, String patente, String marca, String modelo, int anio, Integer conductorId) {
        String sql = "UPDATE camiones SET patente = ?, marca = ?, modelo = ?, anio = ?, conductor_id = ? WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            ps.setString(2, marca);
            ps.setString(3, modelo);
            ps.setInt(4, anio);
            if (conductorId != null) ps.setInt(5, conductorId);
            else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean eliminarCamion(int id) {
        String sql = "DELETE FROM camiones WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ======================== MANTENIMIENTO ========================

    public void agregarMantenimiento(String patente, String tipo, String descripcion, String fecha) {
        String sql = "INSERT INTO mantenimiento (patente, tipo, descripcion, fecha) VALUES (?, ?, ?, ?)";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            ps.setString(2, tipo);
            ps.setString(3, descripcion);
            ps.setString(4, fecha);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> listarMantenimientos() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, patente, tipo, descripcion, fecha FROM mantenimiento ORDER BY fecha DESC";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("patente"),
                    rs.getString("tipo"),
                    rs.getString("descripcion") != null ? rs.getString("descripcion") : "",
                    rs.getString("fecha")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void actualizarMantenimiento(int id, String patente, String tipo, String descripcion, String fecha) {
        String sql = "UPDATE mantenimiento SET patente = ?, tipo = ?, descripcion = ?, fecha = ? WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            ps.setString(2, tipo);
            ps.setString(3, descripcion);
            ps.setString(4, fecha);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean eliminarMantenimiento(int id) {
        String sql = "DELETE FROM mantenimiento WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
