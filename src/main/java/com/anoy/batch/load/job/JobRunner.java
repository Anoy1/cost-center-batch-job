package com.anoy.batch.load.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobRunner implements CommandLineRunner{

	@Autowired
	private JobLauncher jobLauncher;
	@Autowired
	private Job jobCostCenter; 
	
	@Override
	public void run(String... args) throws Exception {
		JobParameters jobParameter = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();
		
		jobLauncher.run(jobCostCenter, jobParameter);
		System.out.println("EXECUTED");
	}

}
