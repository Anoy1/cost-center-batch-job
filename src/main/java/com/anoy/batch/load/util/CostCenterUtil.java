package com.anoy.batch.load.util;

import org.springframework.stereotype.Component;

import com.anoy.batch.load.model.CostCenterData;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CostCenterUtil {
	
	public void toUpperCase(CostCenterData costCenter) {
		costCenter.setStatus(costCenter.getStatus().toUpperCase());
		costCenter.setJobName(costCenter.getJobName().toUpperCase());
		costCenter.setLocationType(costCenter.getLocationType().toUpperCase());
		costCenter.setLocation(costCenter.getLocation().toUpperCase());
	}

	public void locationTypeCheck(CostCenterData costCenter) {
		costCenter.setLocationType(costCenter.getLocationType().substring(0, (costCenter.getLocationType().indexOf("-")+1)) 
				                   + costCenter.getLocation());
		
	}

}
