package qzui.database;

import org.quartz.JobPersistenceException;
import org.quartz.utils.DBConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class QuartzConnection {

    public static Connection getConnection(String dataSource) throws JobPersistenceException {
        Connection conn;
        try {
            conn = DBConnectionManager.getInstance().getConnection(dataSource);
        } catch (SQLException sqle) {
            throw new JobPersistenceException(
                    "Failed to obtain DB connection from data source '"
                            + dataSource + "': " + sqle.toString(), sqle);
        } catch (Throwable e) {
            throw new JobPersistenceException(
                    "Failed to obtain DB connection from data source '"
                            + dataSource + "': " + e.toString(), e);
        }

        if (conn == null) {
            throw new JobPersistenceException(
                    "Could not get connection from DataSource '"
                            + dataSource + "'");
        }

        return conn;
    }
}
