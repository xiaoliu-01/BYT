package com.register.byt.hosp.repository;

import com.register.model.entity.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author LLXX
 * @create 2021-08-05 10:10
 */
@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
        
     Department getDepartmentByHoscodeAndDepcode(String hosCode, String depCode);
}
