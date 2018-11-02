package qzui.plugin;

import org.quartz.*;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qzui.database.QuartzConnection;

import java.sql.*;
import java.text.MessageFormat;

public class JdbcTriggerHistoryPlugin implements SchedulerPlugin, TriggerListener {

    private String dataSource;

    private String getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    private String name;

    private final String triggerFiredMessage = "Trigger {1}.{0} fired job {6}.{5} at: {4, date, HH:mm:ss MM/dd/yyyy}";

    private final String triggerMisfiredMessage = "Trigger {1}.{0} misfired job {6}.{5}  at: {4, date, HH:mm:ss MM/dd/yyyy}.  Should have fired at: {3, date, HH:mm:ss MM/dd/yyyy}";

    private final String triggerCompleteMessage = "Trigger {1}.{0} completed firing job {6}.{5} at {4, date, HH:mm:ss MM/dd/yyyy} with resulting trigger instruction code: {9}";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public String getTriggerCompleteMessage() {
        return this.triggerCompleteMessage;
    }

    public String getTriggerFiredMessage() {
        return this.triggerFiredMessage;
    }

    public String getTriggerMisfiredMessage() {
        return this.triggerMisfiredMessage;
    }

    public JdbcTriggerHistoryPlugin() {
    }

    protected Logger getLog() {
        return this.log;
    }


    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give
     * the <code>SchedulerPlugin</code> a chance to initialize.
     * </p>
     *
     * @throws SchedulerConfigException if there is an error initializing.
     */
    @Override
    public void initialize(String pname, Scheduler scheduler, ClassLoadHelper classLoadHelper)
            throws SchedulerException {
        this.name = pname;

        scheduler.getListenerManager().addTriggerListener(this, EverythingMatcher.allTriggers());
    }

    @Override
    public void start() {
        // do nothing...
    }

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it
     * should free up all of it's resources because the scheduler is shutting
     * down.
     * </p>
     */
    @Override
    public void shutdown() {
        // nothing to do...
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * TriggerListener Interface.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /*
     * Object[] arguments = { new Integer(7), new
     * Date(System.currentTimeMillis()), "a disturbance in the Force" };
     *
     * String result = MessageFormat.format( "At {1,time} on {1,date}, there
     * was {2} on planet {0,number,integer}.", arguments);
     */

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        if (!getLog().isInfoEnabled()) {
            return;
        }

        Object[] args = {
                trigger.getKey().getName(), trigger.getKey().getGroup(),
                trigger.getPreviousFireTime(), trigger.getNextFireTime(),
                new java.util.Date(), context.getJobDetail().getKey().getName(),
                context.getJobDetail().getKey().getGroup(),
                context.getRefireCount()
        };

        getLog().info(MessageFormat.format(getTriggerFiredMessage(), args));
    }


    @Override
    public void triggerMisfired(Trigger trigger) {
        if (!getLog().isInfoEnabled()) {
            return;
        }

        Object[] args = {
                trigger.getKey().getName(), trigger.getKey().getGroup(),
                trigger.getPreviousFireTime(), trigger.getNextFireTime(),
                new java.util.Date(), trigger.getJobKey().getName(),
                trigger.getJobKey().getGroup()
        };

        getLog().info(MessageFormat.format(getTriggerMisfiredMessage(), args));
    }


    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
                                Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        if (!getLog().isInfoEnabled()) {
            return;
        }

        String instrCode = "UNKNOWN";
        if (triggerInstructionCode == Trigger.CompletedExecutionInstruction.DELETE_TRIGGER) {
            instrCode = "DELETE TRIGGER";
        } else if (triggerInstructionCode == Trigger.CompletedExecutionInstruction.NOOP) {
            instrCode = "DO NOTHING";
        } else if (triggerInstructionCode == Trigger.CompletedExecutionInstruction.RE_EXECUTE_JOB) {
            instrCode = "RE-EXECUTE JOB";
        } else if (triggerInstructionCode == Trigger.CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_COMPLETE) {
            instrCode = "SET ALL OF JOB'S TRIGGERS COMPLETE";
        } else if (triggerInstructionCode == Trigger.CompletedExecutionInstruction.SET_TRIGGER_COMPLETE) {
            instrCode = "SET THIS TRIGGER COMPLETE";
        }

        Object[] args = {
                trigger.getKey().getName(),
                trigger.getKey().getGroup(),

                trigger.getPreviousFireTime(),
                trigger.getNextFireTime(),
                new java.util.Date(),

                context.getJobDetail().getKey().getName(),
                context.getJobDetail().getKey().getGroup(),

                context.getRefireCount(),
                triggerInstructionCode.toString(),
                trigger.getJobKey().getName(),
                trigger.getJobKey().getGroup()
        };

        getLog().info(MessageFormat.format(getTriggerCompleteMessage(), args));
        try {
            saveTriggerComplete(args);
        } catch (JobPersistenceException e) {
            throw new RuntimeException(e);
        }

    }


    private static final String triggerCompleteQuery =
            "INSERT INTO QRTZ_TRIGGER_RUN " +
                    "(" +
                    "TRIGGER_NAME, " +
                    "TRIGGER_GROUP, " +
                    "PREV_FIRED_DATE, " +
                    "PREV_FIRED_TIME, " +
                    "NEXT_FIRED_DATE, " +
                    "NEXT_FIRED_TIME, " +
                    "FIRED_DATE, " +
                    "FIRED_TIME, " +
                    "CTX_TRIGGER_NAME, " +
                    "CTX_TRIGGER_GROUP, " +
                    "REFIRE_COUNT, " +
                    "INSTRUCTION_CODE," +
                    "JOB_NAME, " +
                    "JOB_GROUP " +
                    ") values (?, ?,   ?,?,  ?,?,  ?, ?,   ?, ?, ?, ?, ?, ?)";

    private void saveTriggerComplete(Object[] args) throws JobPersistenceException {
        Connection con = QuartzConnection.getConnection(getDataSource());
        PreparedStatement ps = null;
        try {

            ps = con.prepareStatement(JdbcTriggerHistoryPlugin.triggerCompleteQuery);
            ps.setString(1, (String) args[0]); // name
            ps.setString(2, (String) args[1]); // group


            if (args[2] == null) {
                ps.setNull(3, Types.DATE);
                ps.setNull(4, Types.TIME);
            } else {
                ps.setDate(3, getSqlDate((java.util.Date) args[2])); // date
                ps.setTime(4, getSqlTime((java.util.Date) args[2])); // time
            }

            if (args[3] == null) {
                ps.setNull(5, Types.DATE);
                ps.setNull(6, Types.TIME);
            } else {
                ps.setDate(5, getSqlDate((java.util.Date) args[3])); // date
                ps.setTime(6, getSqlTime((java.util.Date) args[3])); // time
            }

            ps.setDate(7, getSqlDate((java.util.Date) args[4])); // date
            ps.setTime(8, getSqlTime((java.util.Date) args[4])); // time


            ps.setString(9, (String) args[5]);
            ps.setString(10, (String) args[6]);
            ps.setInt(11, (Integer) args[7]); // refire count
            ps.setString(12, (String) args[8]); // instruction code

            ps.setString(13, (String) args[9]); // instruction code
            ps.setString(14, (String) args[10]); // instruction code

            ps.execute();
        } catch (SQLException e) {
            throw new JobPersistenceException(e.getLocalizedMessage());
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                throw new JobPersistenceException(e.getLocalizedMessage());
            }
        }
    }

    private Time getSqlTime(java.util.Date javaDate) {
        return new Time(javaDate.getTime());
    }

    private Date getSqlDate(java.util.Date javaDate) {
        return new Date(javaDate.getTime());
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

}