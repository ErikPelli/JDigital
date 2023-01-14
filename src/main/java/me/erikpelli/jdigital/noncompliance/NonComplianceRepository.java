package me.erikpelli.jdigital.noncompliance;

import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface NonComplianceRepository extends CrudRepository<NonCompliance, Integer> {
    @Nullable
    NonCompliance findByCode(int code);

    @NonNull
    List<NonCompliance> findAll(Pageable pageable);

    @NonNull
    List<NonCompliance> findByCommentContaining(String comment, Pageable pageable);

    @NonNull
    List<NonCompliance> findByOriginAndCode(NonComplianceOrigin origin, Integer code, Pageable pageable);

    @NonNull
    @Query("""
            select n.nonComplianceState.status as status , count(*) as counter
            from NonCompliance n
            group by n.nonComplianceState.status""")
    List<TotalStatsType> totalStatsByStatus_rows();

    interface TotalStatsType {
        NonComplianceStatus getStatus();

        Integer getCounter();
    }

    default Map<NonComplianceStatus, Integer> totalStatsByStatus() {
        return totalStatsByStatus_rows().stream()
                .collect(Collectors.toMap(TotalStatsType::getStatus, TotalStatsType::getCounter));
    }

    @NonNull
    @Query("""
            select n.date as date, n.nonComplianceState.status as status, count(*) as counter
            from NonCompliance n
            where n.date > :oneMonthAgoDate and n.date <= :todayDate
            group by n.date, n.nonComplianceState.status
            order by n.date desc""")
    List<TotalStatsTypeWithDate> last30DaysStats_rows(@Param("oneMonthAgoDate") LocalDate oneMonthAgoDate,
                                                      @Param("todayDate") LocalDate todayDate);

    interface TotalStatsTypeWithDate extends TotalStatsType {
        Date getDate();
    }

    default Map<Date, Map<NonComplianceStatus, Integer>> last30DaysStats(LocalDate oneMonthAgoDate, LocalDate todayDate) {
        return last30DaysStats_rows(oneMonthAgoDate, todayDate).stream().collect(Collectors.toMap(
                TotalStatsTypeWithDate::getDate,
                (TotalStatsTypeWithDate) -> new HashMap<>(Map.of(
                        TotalStatsTypeWithDate.getStatus(),
                        TotalStatsTypeWithDate.getCounter()
                )),
                (alreadyPresent, otherMap) -> {
                    alreadyPresent.putAll(otherMap);
                    return alreadyPresent;
                }));
    }
}
