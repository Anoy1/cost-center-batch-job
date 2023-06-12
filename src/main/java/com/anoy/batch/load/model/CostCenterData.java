package com.anoy.batch.load.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CostCenterData {
	
	@Id
	private String userId;
	private String status;
	private String jobCode;
	private String jobName;
	private String location;
	private String locationType;
	private String fohpCode;
	private String terminatedDate;

}
