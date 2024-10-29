package study.moum.moum.moum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import study.moum.moum.moum.service.MoumService;

@RestController
@RequiredArgsConstructor
public class MoumController {

    private final MoumService moumService;
}
