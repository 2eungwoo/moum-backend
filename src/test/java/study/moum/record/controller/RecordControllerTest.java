package study.moum.record.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import study.moum.custom.WithAuthUser;
import study.moum.moum.moum.controller.MoumController;
import study.moum.moum.moum.service.MoumService;
import study.moum.record.service.RecordService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(RecordController.class)
class RecordControllerTest {

    @MockBean
    private RecordService recordService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }


    @Test
    @DisplayName("레코드(이력) 추가 성공")
    @WithAuthUser
    void add_record_success(){

    }

    @Test
    @DisplayName("레코드(이력) 추가 실패 - 로그인 필요")
    @WithAuthUser
    void add_record_fail_needLogin(){

    }

    @Test
    @DisplayName("레코드(이력) 삭제 성공")
    @WithAuthUser
    void remove_record_success(){

    }

    @Test
    @DisplayName("레코드(이력) 삭제 실패 - 로그인 필요")
    @WithAuthUser
    void remove_record_fail_needLogin(){

    }
}