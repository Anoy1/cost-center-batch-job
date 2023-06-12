package com.anoy.batch.load.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anoy.batch.load.model.CostCenterData;

public interface CostCenterDataRepository extends JpaRepository<CostCenterData, String>{

}
