package study.moum.record.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import study.moum.record.service.RecordService;

@RestController
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;


}
