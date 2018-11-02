package qzui;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import restx.factory.AutoStartable;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Date: 18/2/14
 * Time: 21:14
 */
@Module
public class QuartzModule {


    @Provides
    @Named("qzui.properties")
    public Properties quartzProperties() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("qzui.properties");
        Properties prop = new Properties();
        try {
            prop.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return prop;
    }

    @Provides
    @Named("qzui.dataSource")
    public String dataSource(@Named("qzui.properties") Properties quartzProperties) {
        return quartzProperties.getProperty("org.quartz.jobStore.dataSource");
    }

    @Provides
    public Scheduler scheduler(@Named("qzui.properties") Properties quartzProperties) throws SchedulerException {
        StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize(quartzProperties);
        return sf.getScheduler();

    }

    @Provides
    public AutoStartable schedulerStarter(Scheduler scheduler) {
        return new AutoStartable() {
            @Override
            public void start() {
                try {
                    scheduler.start();
                } catch (SchedulerException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Provides
    public AutoCloseable schedulerCloser(Scheduler scheduler) {
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                scheduler.shutdown();
            }
        };
    }
}
