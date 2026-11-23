package io.github.giorgionalba.transactions.api;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerIntegrationTest {

    private static final String VALID_BODY = """
            { "amount": 5000, "type": "cars" }
            """;

    private final MockMvc mockMvc;

    TransactionControllerIntegrationTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldReturnStatusOkWhenTransactionIsCreated() throws Exception {
        putTransaction("10", VALID_BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    void shouldReturnBadRequestWhenBodyIsInvalid(String body) throws Exception {
        putTransaction("10", body)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenIdIsNotPositive() throws Exception {
        putTransaction("0", VALID_BODY)
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidBodies() {
        return Stream.of(
                argumentSet("missing amount", """
                        { "type": "cars" }
                        """),
                argumentSet("missing type", """
                        { "amount": 5000 }
                        """),
                argumentSet("blank type", """
                        { "amount": 5000, "type": "  " }
                        """),
                argumentSet("zero parent_id", """
                        { "amount": 5000, "type": "cars", "parent_id": 0 }
                        """));
    }

    private ResultActions putTransaction(String id, String body) throws Exception {
        return mockMvc.perform(put("/transactions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
