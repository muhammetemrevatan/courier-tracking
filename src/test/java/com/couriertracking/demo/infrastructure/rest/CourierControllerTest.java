package com.couriertracking.demo.infrastructure.rest;

import com.couriertracking.demo.application.event.LocationEventPublisher;
import com.couriertracking.demo.application.port.in.CourierCommandPort;
import com.couriertracking.demo.application.port.in.CourierQueryPort;
import com.couriertracking.demo.domain.model.CourierLog;
import com.couriertracking.demo.domain.model.Location;
import com.couriertracking.demo.infrastructure.config.i18n.MessageTranslator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourierController.class)
@Import(CourierControllerTest.I18nTestConfig.class)
class CourierControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean
    private CourierCommandPort commandPort;
    @MockitoBean
    private CourierQueryPort   queryPort;
    @MockitoBean
    private LocationEventPublisher publisher;
    @MockitoBean
    private MessageTranslator messageTranslator;

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

    @TestConfiguration
    static class I18nTestConfig {
        @Bean
        MessageSource messageSource() {
            var source = new ReloadableResourceBundleMessageSource();
            source.setBasename("classpath:messages");
            source.setDefaultEncoding("UTF-8");
            return source;
        }

        @Bean
        MessageTranslator messageTranslator(MessageSource ms) {
            return new MessageTranslator(ms);
        }
    }

    @BeforeEach
    void setUp() {
        when(messageTranslator.translate(anyString(), any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_returns201_withBody() throws Exception {
        when(commandPort.createCourier("Emre")).thenReturn(10L);

        var body = om.writeValueAsString(new CourierController.CreateCourierRequest("Emre"));

        mockMvc.perform(post("/api/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Emre")));

        verify(commandPort).createCourier("Emre");
    }

    @Test
    void sendLocation_returns202_andPublishesEvent() throws Exception {
        Long courierId = 10L;
        LocalDateTime t = LocalDateTime.of(2025, 8, 24, 12, 0, 0);
        var req = new CourierController.CourierLocationRequest(41.0, 29.0, t);

        mockMvc.perform(post("/api/couriers/{courierId}/location", courierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isAccepted());

        ArgumentCaptor<Location> locCap = ArgumentCaptor.forClass(Location.class);
        verify(publisher, times(1)).notifyObservers(eq(courierId), locCap.capture());

        Location sent = locCap.getValue();
        assert sent.lat() == 41.0;
        assert sent.lng() == 29.0;
        assert sent.time().equals(t);
    }

    @Test
    void getDistance_returns200_withDto() throws Exception {
        when(queryPort.getTotalTravelDistance(7L)).thenReturn(1234.5);

        mockMvc.perform(get("/api/couriers/{courierId}/distance", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courierId", is(7)))
                .andExpect(jsonPath("$.totalDistance", is(1234.5)));

        verify(queryPort).getTotalTravelDistance(7L);
    }

    @Test
    void getLogs_returns200_withMappedArray() throws Exception {
        LocalDateTime time = LocalDateTime.of(2025, 8, 24, 12, 30, 0);
        when(queryPort.getLogs(5L)).thenReturn(
                List.of(new CourierLog(5L, 1000L, time))
        );

        mockMvc.perform(get("/api/couriers/{courierId}/logs", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courierId", is(5)))
                .andExpect(jsonPath("$[0].storeId", is(1000)));

        verify(queryPort).getLogs(5L);
    }
}