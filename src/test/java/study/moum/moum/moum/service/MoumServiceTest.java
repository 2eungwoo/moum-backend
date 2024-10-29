package study.moum.moum.moum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import study.moum.moum.moum.domain.MoumRepository;
import study.moum.moum.moum.domain.MoumTeamRepository;

import static org.junit.jupiter.api.Assertions.*;

class MoumServiceTest {

    @Spy
    @InjectMocks
    private MoumService moumService;

    @Mock
    private MoumRepository moumRepository;

    @Mock
    private MoumTeamRepository moumTeamRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("test setup")
    void testSetup(){

    }

}