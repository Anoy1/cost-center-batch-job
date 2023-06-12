package com.anoy.batch.load.step;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.anoy.batch.load.model.CostCenterData;
import com.anoy.batch.load.util.CostCenterUtil;

public class CostCenterItemProcessor implements ItemProcessor<CostCenterData, CostCenterData>{
	
	@Autowired
	CostCenterUtil costCenterUtil;
	
	public CostCenterData process(CostCenterData costCenter){
		
		costCenterUtil.locationTypeCheck(costCenter);
		costCenterUtil.toUpperCase(costCenter);

		return costCenter;
		}

}

