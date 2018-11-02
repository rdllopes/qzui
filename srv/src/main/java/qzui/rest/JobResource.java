package qzui.rest;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import qzui.domain.JobDefinition;
import qzui.domain.JobDescriptor;
import qzui.domain.TriggerHistory;
import qzui.domain.TriggerHistoryRepository;
import restx.annotations.DELETE;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Date: 18/2/14
 * Time: 21:31
 */
@RestxResource
@Component
public class JobResource {
    private final Scheduler scheduler;
    private final Collection<JobDefinition> definitions;
    private final TriggerHistoryRepository triggerHistoryRepository;

    public JobResource(Scheduler scheduler, Collection<JobDefinition> definitions, TriggerHistoryRepository triggerHistoryRepository) {
        this.scheduler = scheduler;
        this.definitions = definitions;
        this.triggerHistoryRepository = triggerHistoryRepository;
    }

    /*
    Example:

        {"type":"log", "name":"test2", "group":"log", "triggers": [{"cron":"0/2 * * * * ?"}]}

        {"type":"http", "name":"google-humans", "method":"GET", "url":"http://127.0.0.1:8080/api/@/ui/", "triggers": [{"when":"now"}]}
     */
    @POST("/groups/{group}/jobs")
    public JobDescriptor addJob(String group, JobDescriptor jobDescriptor) {
        try {
            jobDescriptor.setGroup(group);
            Set<Trigger> triggers = jobDescriptor.buildTriggers();
            JobDetail jobDetail = jobDescriptor.buildJobDetail();
            if (triggers.isEmpty()) {
                this.scheduler.addJob(jobDetail, false);
            } else {
                this.scheduler.scheduleJob(jobDetail, triggers, false);
            }
            return jobDescriptor;
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @GET("/groups/{group}/jobs/{name}/history")
    public List<TriggerHistory> getListTriggerHistory(String group, String name) {
        return this.triggerHistoryRepository.findAll(group, name);
    }

    @GET("/jobs")
    public Set<JobKey> getJobKeys() {
        try {
            return this.scheduler.getJobKeys(GroupMatcher.anyJobGroup());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @GET("/groups/{group}/jobs")
    public Set<JobKey> getJobKeysByGroup(String group) {
        try {
            return this.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @GET("/groups/{group}/jobs/{name}")
    public Optional<JobDescriptor> getJob(String group, String name) {
        try {
            JobDetail jobDetail = this.scheduler.getJobDetail(new JobKey(name, group));

            if (jobDetail == null) {
                return Optional.empty();
            }

            for (JobDefinition definition : this.definitions) {
                if (definition.acceptJobClass(jobDetail.getJobClass())) {
                    return Optional.of(definition.buildDescriptor(
                            jobDetail, this.scheduler.getTriggersOfJob(jobDetail.getKey())));
                }
            }

            throw new IllegalStateException("can't find job definition for " + jobDetail
                    + " - available job definitions: " + this.definitions);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @DELETE("/groups/{group}/jobs/{name}")
    public void deleteJob(String group, String name) {
        try {
            this.scheduler.deleteJob(new JobKey(name, group));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
