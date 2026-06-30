package com.example.demorestdocs.controller;

import com.example.demorestdocs.dto.UserCreateRequest;
import com.example.demorestdocs.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
class UserControllerDocumentationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(WebApplicationContext context,
               RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    void createUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "zhangsan", "zhangsan@example.com", LocalDate.of(1990, 1, 1));

        this.mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("zhangsan"))
                .andExpect(jsonPath("$.email").value("zhangsan@example.com"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"))
                .andDo(document("user/create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").description("使用者名稱，長度 2~50 字元"),
                                fieldWithPath("email").description("電子郵件，必須符合 Email 格式"),
                                fieldWithPath("birthday").description("生日（選填），格式 yyyy-MM-dd，必須是過去日期")
                        ),
                        responseFields(
                                fieldWithPath("id").description("系統自動產生的使用者 ID"),
                                fieldWithPath("username").description("使用者名稱"),
                                fieldWithPath("email").description("電子郵件"),
                                fieldWithPath("birthday").description("生日")
                        )
                ));
    }

    @Test
    void createUserWithValidationErrors() throws Exception {
        String invalidBody = """
                {"username":"","email":"invalid-email","birthday":"2099-12-31"}
                """;

        this.mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors.username").isString())
                .andExpect(jsonPath("$.fieldErrors.email").isString())
                .andDo(document("user/create-validation-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("timestamp").description("錯誤發生的時間戳記"),
                                fieldWithPath("status").description("HTTP 狀態碼（400）"),
                                fieldWithPath("error").description("錯誤訊息標題"),
                                fieldWithPath("fieldErrors").description("各欄位的驗證錯誤明細"),
                                fieldWithPath("fieldErrors.username").description("使用者名稱的驗證失敗原因"),
                                fieldWithPath("fieldErrors.email").description("電子郵件的驗證失敗原因"),
                                fieldWithPath("fieldErrors.birthday").description("生日的驗證失敗原因")
                        )
                ));
    }

    @Test
    void getUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "lisi", "lisi@example.com", LocalDate.of(1995, 6, 15));
        String createJson = objectMapper.writeValueAsString(request);

        String responseBody = this.mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserResponse created = objectMapper.readValue(responseBody, UserResponse.class);

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/users/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("lisi"))
                .andExpect(jsonPath("$.email").value("lisi@example.com"))
                .andDo(document("user/get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("使用者 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("使用者 ID"),
                                fieldWithPath("username").description("使用者名稱"),
                                fieldWithPath("email").description("電子郵件"),
                                fieldWithPath("birthday").description("生日")
                        )
                ));
    }

    @Test
    void getUserNotFound() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/users/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andDo(document("user/get-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("不存在的使用者 ID")
                        )
                ));
    }
}
