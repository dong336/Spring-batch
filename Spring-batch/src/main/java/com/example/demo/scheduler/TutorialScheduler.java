package com.example.demo.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TutorialScheduler {
	
	private final Job job;
	private final JobLauncher jobLauncher;
	
	@Scheduled(fixedDelay = 10 * 1000L)
	public void scheduleJob() {
		try {
			Map<String, JobParameter> params = new HashMap<>();
			params.put("timestamp", new JobParameter(System.currentTimeMillis()));
			
			jobLauncher.run(job, new JobParameters(params));
		} catch (JobExecutionException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
