package study.moum.moum.moum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.moum.moum.moum.domain.MoumRepository;

@Service
@RequiredArgsConstructor
public class MoumService {

    private final MoumRepository moumRepository;
}
