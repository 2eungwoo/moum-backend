package study.moum.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.moum.record.repository.MemberRecordRepository;
import study.moum.record.repository.RecordRepository;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final MemberRecordRepository memberRecordRepository;
}
