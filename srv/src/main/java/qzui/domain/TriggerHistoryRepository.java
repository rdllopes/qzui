package qzui.domain;

import org.quartz.JobPersistenceException;
import qzui.database.QuartzConnection;
import restx.factory.Component;

import javax.inject.Named;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TriggerHistoryRepository {

    private final String dataSource;

    public TriggerHistoryRepository(@Named("qzui.dataSource") String dataSource) {
        this.dataSource = dataSource;
    }

    private final static String query =
            "select    TRIGGER_NAME,\n" +
                    "  TRIGGER_GROUP,\n" +
                    "  PREV_FIRED_DATE,\n" +
                    "  PREV_FIRED_TIME,\n" +
                    "  NEXT_FIRED_DATE,\n" +
                    "  NEXT_FIRED_TIME,\n" +
                    "  FIRED_DATE,\n" +
                    "  FIRED_TIME,\n" +
                    "  CTX_TRIGGER_NAME,\n" +
                    "  CTX_TRIGGER_GROUP,\n" +
                    "  REFIRE_COUNT,\n" +
                    "  INSTRUCTION_CODE from QRTZ_TRIGGER_RUN where" +
                    "  JOB_NAME = ? AND JOB_GROUP = ?";

    public List<TriggerHistory> findAll(String group, String name) {
        List<TriggerHistory> reslt = new ArrayList<>();
        Connection con = null;
        try {
            con = QuartzConnection.getConnection(this.dataSource);
            PreparedStatement ps = con.prepareStatement(TriggerHistoryRepository.query);
            ps.setString(1, name);
            ps.setString(2, group);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TriggerHistory triggerHistory = new TriggerHistory();
                triggerHistory.setTriggerName(rs.getString(1));
                triggerHistory.setTriggerGroup(rs.getString(2));
                triggerHistory.setPrevFiredDate(rs.getString(3));
                triggerHistory.setPrevFiredTime(rs.getString(4));
                triggerHistory.setNextFiredDate(rs.getString(5));
                triggerHistory.setNextFiredTime(rs.getString(6));
                triggerHistory.setFiredDate(rs.getString(7));
                triggerHistory.setFiredTime(rs.getString(8));
                triggerHistory.setCtxTriggerName(rs.getString(9));
                triggerHistory.setCtxTriggerGroup(rs.getString(10));
                triggerHistory.setRefireCount(rs.getString(11));
                triggerHistory.setInstructionCode(rs.getString(12));
                reslt.add(triggerHistory);
            }

        } catch (JobPersistenceException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return reslt;
    }

}
