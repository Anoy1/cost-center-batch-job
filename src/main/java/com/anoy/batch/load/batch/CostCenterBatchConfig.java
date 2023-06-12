package com.anoy.batch.load.batch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.anoy.batch.load.listener.CostCenterListener;
import com.anoy.batch.load.model.CostCenterData;
import com.anoy.batch.load.repository.CostCenterDataRepository;
import com.anoy.batch.load.step.CostCenterItemProcessor;
import com.anoy.batch.load.step.CostCenterItemWriter;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.opencsv.CSVReader;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class CostCenterBatchConfig {
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Value("${azure.storage.connectionKey}")
	private String storageConnectionKey;
	@Value("${azure.storage.containername}")
	private String storageContainerName;
	private List<String> blockBlobList;
	private CloudStorageAccount cloudStorageAccount;

	
	
	@Bean
	public FlatFileItemReader<CostCenterData> reader(){
		
		FlatFileItemReader<CostCenterData> reader = new FlatFileItemReader<CostCenterData>();

		try {
		cloudStorageAccount = CloudStorageAccount.parse(storageConnectionKey);
		blockBlobList = new ArrayList<String>();
		final CloudBlobClient cloudBlobClient = cloudStorageAccount.createCloudBlobClient();
		final CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(storageContainerName);
		CloudBlobDirectory cloudBlobDirectory = cloudBlobContainer.getDirectoryReference("INBOUND");
		CloudBlobDirectory cloudBlobArchiveDirectory = cloudBlobContainer.getDirectoryReference("INBOUND ARCHIVE");
	
		for(ListBlobItem blobItem: cloudBlobContainer.listBlobs("INBOUND",true)) {
			if(blobItem.getUri().toString().contains(".csv")) {
				blockBlobList.add(blobItem.getUri().toString().substring((blobItem.getUri().toString().indexOf("d/"+2))));
			}
		}
		
		if(blockBlobList.size()>0) {
			for(String blobName: blockBlobList) {
				try {
					CloudBlockBlob downloadBlob = cloudBlobDirectory.getBlockBlobReference(blobName);
					File sourceFile = new File(blobName);
					FileOutputStream fileOutputStream = new FileOutputStream(sourceFile);
					downloadBlob.download(fileOutputStream);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					baos.writeTo(fileOutputStream);
					//process(new FileReader(sourceFile));
					reader.setResource(new FileSystemResource(sourceFile));
					reader.setLinesToSkip(1);
					reader.setName("csvReader");
					reader.setLineMapper(lineMapper());
				
					CloudBlockBlob downloadArchiveBlob = cloudBlobArchiveDirectory.getBlockBlobReference(blobName);
					downloadArchiveBlob.uploadFromFile(sourceFile.getAbsolutePath());
					sourceFile.delete();
					downloadBlob.delete();
					
					/*CSVReader csvReader = new CSVReader(new FileReader(sourceFile));
					String[] nextRecord;
					List<List<String>> data = new ArrayList<List<String>>();
					while((nextRecord = csvReader.readNext()) != null) {
						List<String> rdata = new ArrayList<String>();
						for(String cell : nextRecord) {
							rdata.add(cell);
						}
						data.add(rdata);
					}*/
				}catch(Exception e) {
					log.error("Unable to dlownload the blob");
				}
			}
		}
	
	}catch(Exception e) {
		log.error("Unable to create the azure coonection");
	}
    return reader;	
	}
	
    private LineMapper<CostCenterData> lineMapper() {
        DefaultLineMapper<CostCenterData> lineMapper = new DefaultLineMapper<>();
 
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("userId","status","jobCode","jobName","location","locationType","fohpCode","terminatedDate");
        
        BeanWrapperFieldSetMapper<CostCenterData> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CostCenterData.class);
 
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }
 
	
	@Bean
	public ItemWriter<CostCenterData>  writer(){
		return new CostCenterItemWriter();
	}
	
	@Bean
	public ItemProcessor<CostCenterData, CostCenterData> processor(){
		return new CostCenterItemProcessor();
	}
	
	@Bean
	public JobExecutionListener jobExecutionListener() {
		return new CostCenterListener();
	}
	
	@Bean
	public Step stepCostCenter() {
		return stepBuilderFactory.get("stepCostCenter")
				.<CostCenterData,CostCenterData>chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}
	
	@Bean
	public Job jobCostCenter() {
		return jobBuilderFactory.get("jobCostCenter")
				.incrementer(new RunIdIncrementer())
				.listener(jobExecutionListener())
				.start(stepCostCenter())
				.build();
	}

}
