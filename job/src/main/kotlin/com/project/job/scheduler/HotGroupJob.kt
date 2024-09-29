package com.project.job.scheduler

import com.project.job.service.GroupService
import org.quartz.CronScheduleBuilder
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

@Component
@DisallowConcurrentExecution
class HotGroupJob(
    private val groupService: GroupService,
) : QuartzJobBean() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun executeInternal(context: JobExecutionContext) {
        logger.info("Job ${this.javaClass.name} started")
        groupService.updateVisit()
        groupService.updateHotGroup()
        groupService.updateHotGroupCache()
    }

    @Bean
    fun hotGroupJobDetail(): JobDetail =
        JobBuilder
            .newJob()
            .ofType(this::class.java)
            .storeDurably()
            .withIdentity(this::class.simpleName)
            .build()

    @Bean
    fun hotGroupJobTrigger(hotGroupJobDetail: JobDetail): Trigger =
        TriggerBuilder
            .newTrigger()
            .forJob(hotGroupJobDetail)
            .withIdentity(this::class.simpleName)
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
            .build()
}
