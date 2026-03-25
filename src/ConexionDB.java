import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ConexionDB
 * Maneja la conexión y operaciones con la base de datos MySQL.
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
    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /**
     * Registra el kilometraje de un camión en la base de datos.
     * Inserta una nueva fila en la tabla registro_kilometraje con la patente,
     * el kilometraje recibido y la fecha/hora actual (asignada por el servidor).
     *
     * @param patente     Patente del camión (ej: "ABCD12")
     * @param kilometraje Kilómetros recorridos en el trayecto (mayor a 0)
     */
    public void registrarKilometraje(String patente, int kilometraje) {
        // Sentencia SQL parametrizada para evitar inyección SQL
        String sql = "INSERT INTO registro_kilometraje (patente, kilometraje) VALUES (?, ?)";

        Connection conexion = null;
        PreparedStatement sentencia = null;

        try {
            // Establecer conexión con la base de datos
            conexion = conectar();

            // Preparar la sentencia e inyectar los parámetros
            sentencia = conexion.prepareStatement(sql);
            sentencia.setString(1, patente);
            sentencia.setInt(2, kilometraje);

            // Ejecutar el INSERT
            sentencia.executeUpdate();

        } catch (SQLException e) {
            // Imprimir el detalle del error para diagnóstico
            e.printStackTrace();
        } finally {
            // Cerrar recursos en orden inverso para liberar la conexión
            try {
                if (sentencia != null) sentencia.close();
                if (conexion  != null) conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
