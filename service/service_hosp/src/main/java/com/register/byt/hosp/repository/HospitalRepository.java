package com.register.byt.hosp.repository;

import com.register.model.entity.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-04 15:17
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {

    Hospital getHospitalByHoscode(String hosCode);

    List<Hospital> findHospitalByHosnameLike(String hosName);
}
