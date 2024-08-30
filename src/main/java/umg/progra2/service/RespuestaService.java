package umg.progra2.service;

import umg.progra2.dao.RespuestaDao;
import umg.progra2.db.DataBaseConnection;
import umg.progra2.db.TransactionManager;
import umg.progra2.model.Respuesta;

import java.sql.Connection;
import java.sql.SQLException;

public class RespuestaService {
    private RespuestaDao respuestaDao = new RespuestaDao();

    public void createRespuesta(Respuesta respuesta) throws SQLException {
        try (Connection connection = DataBaseConnection.getConnection()) {
            TransactionManager tm = new TransactionManager(connection);
            tm.beginTransaction();
            try {
                respuestaDao.insertRespuesta(respuesta);
                tm.commit();
            } catch (SQLException e) {
                tm.rollback();
                throw e;
            }
        }
    }
}
