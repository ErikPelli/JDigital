package me.erikpelli.jdigital.company;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

public interface CompanyRepository extends CrudRepository<Company, String> {
    @Nullable
    Company findFirstByVatNum(String vatNum);
}
