package study.moum.community.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.moum.community.record.domain.RecordEntity;

public interface RecordRepository extends JpaRepository<RecordEntity, Integer> {
}
