package me.erikpelli.jdigital.shipping;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface ShippingRepository extends CrudRepository<ShippingLot, String> {
    @NonNull
    List<ShippingLot> findAll(Pageable pageable);

    @Nullable
    ShippingLot findByShippingCode(String shippingCode);
}
