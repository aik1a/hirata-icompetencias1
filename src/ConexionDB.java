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

    // --- Parámetros de conexión ---
    private static final String URL      = "jdbc:mysql://localhost:3306/hirata_flota";
    private static final String USUARIO  = "root";
    private static final String CLAVE    = "";

    /**
     * Abre y retorna una conexión activa a la base de datos MySQL.
     *
     * @return Connection objeto de conexión listo para usar
     * @throws SQLException si no es posible establecer la conexión
     */
    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    // ======================== KILOMETRAJE ========================

    /**
     * Registra el kilometraje de un camión en la base de datos.
     * Inserta una nueva fila en la tabla registro_kilometraje con la patente,
     * el kilometraje recibido y la fecha/hora actual (asignada por el servidor).
     *
     * @param patente     Patente del camión (ej: "ABCD12")
     * @param kilometraje Kilómetros recorridos en el trayecto (mayor a 0)
     */
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

    /**
     * Obtiene el kilometraje total acumulado para una patente específica.
     * Calcula la suma de todos los registros de kilometraje de esa patente.
     *
     * @param patente Patente del camión a consultar
     * @return Suma total de kilometraje acumulado, 0 si no hay registros
     */
    public int obtenerKilometrajeTotalPorPatente(String patente) {
        String sql = "SELECT COALESCE(SUM(kilometraje), 0) FROM registro_kilometraje WHERE patente = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ======================== CONDUCTORES ========================

    /**
     * Agrega un nuevo conductor a la base de datos.
     *
     * @param nombre   Nombre completo del conductor
     * @param rut      RUT del conductor
     * @param telefono Teléfono de contacto
     * @param email    Correo electrónico
     */
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

    /**
     * Obtiene la lista de todos los conductores registrados, ordenados por nombre.
     *
     * @return Lista de arreglos con {id, nombre, rut, telefono, email}
     */
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

    /**
     * Actualiza los datos de un conductor existente.
     *
     * @param id       ID del conductor a actualizar
     * @param nombre   Nuevo nombre
     * @param rut      Nuevo RUT
     * @param telefono Nuevo teléfono
     * @param email    Nuevo email
     */
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

    /**
     * Elimina un conductor por su ID.
     * Puede fallar si el conductor está asignado a un camión (FK constraint).
     *
     * @param id ID del conductor a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
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

    /**
     * Agrega un nuevo camión a la base de datos.
     *
     * @param patente     Patente del camión
     * @param marca       Marca del camión
     * @param modelo      Modelo del camión
     * @param anio        Año de fabricación
     * @param conductorId ID del conductor asignado, null si no tiene
     */
    public void agregarCamion(String patente, String marca, String modelo, int anio, Integer conductorId) {
        String sql = "INSERT INTO camiones (patente, marca, modelo, anio, conductor_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            ps.setString(2, marca);
            ps.setString(3, modelo);
            ps.setInt(4, anio);
            if (conductorId != null) {
                ps.setInt(5, conductorId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la lista de todos los camiones con el nombre del conductor asignado.
     * Realiza un LEFT JOIN con la tabla conductores para traer el nombre.
     *
     * @return Lista de arreglos con {id, patente, marca, modelo, anio, conductorNombre, conductorId}
     */
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

    /**
     * Actualiza los datos de un camión existente.
     *
     * @param id          ID del camión a actualizar
     * @param patente     Nueva patente
     * @param marca       Nueva marca
     * @param modelo      Nuevo modelo
     * @param anio        Nuevo año de fabricación
     * @param conductorId Nuevo ID del conductor asignado, null si no tiene
     */
    public void actualizarCamion(int id, String patente, String marca, String modelo, int anio, Integer conductorId) {
        String sql = "UPDATE camiones SET patente = ?, marca = ?, modelo = ?, anio = ?, conductor_id = ? WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patente);
            ps.setString(2, marca);
            ps.setString(3, modelo);
            ps.setInt(4, anio);
            if (conductorId != null) {
                ps.setInt(5, conductorId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina un camión por su ID.
     *
     * @param id ID del camión a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
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

    /**
     * Agrega un nuevo registro de mantenimiento para un camión.
     *
     * @param patente     Patente del camión
     * @param tipo        Tipo de mantenimiento (Preventivo, Correctivo, Revisión)
     * @param descripcion Descripción detallada del mantenimiento
     * @param fecha       Fecha del mantenimiento en formato AAAA-MM-DD
     */
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

    /**
     * Obtiene la lista de todos los registros de mantenimiento,
     * ordenados por fecha descendente (más recientes primero).
     *
     * @return Lista de arreglos con {id, patente, tipo, descripcion, fecha}
     */
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

    /**
     * Actualiza un registro de mantenimiento existente.
     *
     * @param id          ID del registro a actualizar
     * @param patente     Nueva patente
     * @param tipo        Nuevo tipo de mantenimiento
     * @param descripcion Nueva descripción
     * @param fecha       Nueva fecha en formato AAAA-MM-DD
     */
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

    /**
     * Elimina un registro de mantenimiento por su ID.
     *
     * @param id ID del registro a eliminar
     * @return true si se eliminó correctamente, false si falló
     */
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
