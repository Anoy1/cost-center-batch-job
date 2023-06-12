package com.anoy.batch.load.step;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.anoy.batch.load.model.CostCenterData;
import com.anoy.batch.load.repository.CostCenterDataRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CostCenterItemWriter implements ItemWriter<CostCenterData>{
	
	@Autowired
	CostCenterDataRepository costCenterDataRepository;
	

	@Override
	public void write(List<? extends CostCenterData> cosCenData) throws Exception {
		log.info("Cost Ceneter Data :"+ cosCenData);
		costCenterDataRepository.saveAll(cosCenData);
		log.info("The data pushed to db successfully");
	}	

}
