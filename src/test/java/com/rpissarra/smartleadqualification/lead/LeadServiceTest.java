package com.rpissarra.smartleadqualification.lead;

import com.rpissarra.smartleadqualification.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private LeadService underTest;


    @DisplayName("Find all should return leads")
    @Test
    void findAllShouldReturnResult() {
        // given
        Pageable pageable = PageRequest.of(0, 3);
        List<Lead> leads = List.of(
                 Lead.builder().id(1L).title("Support").type(Type.SUPPORT).urgencyLevel(UrgencyLevel.HIGH).description("user needs support asap").build(),
                 Lead.builder().id(2L).title("Demo").type(Type.DEMO_REQUEST).urgencyLevel(UrgencyLevel.MEDIUM).description("user requested a demo").build(),
                 Lead.builder().id(3L).title("Pricing").type(Type.PRICING_INQUIRY).urgencyLevel(UrgencyLevel.LOW).description("user wants to upgrade to pro plan").build()
        );
        Page<Lead> leadsPage = new PageImpl<>(leads, pageable, leads.size());
        given(leadRepository.findAll(pageable)).willReturn(leadsPage);
        // when
        List<LeadResponse> actual = underTest.getAllLeads(pageable);
        // then
        List<LeadResponse> expected = leads.stream().map(LeadResponse::toLeadResponse).toList();
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @DisplayName("Find all return empty list")
    @Test
    void findAllShouldReturnEmpty() {
        // given
        Pageable pageable = PageRequest.of(0, 3);
        List<Lead> leads = List.of();
        Page<Lead> leadsPage = new PageImpl<>(Collections.EMPTY_LIST, pageable, leads.size());
        given(leadRepository.findAll(pageable)).willReturn(leadsPage);
        // when
        List<LeadResponse> actual = underTest.getAllLeads(pageable);
        // then
        assertEquals(Collections.EMPTY_LIST.size(), actual.size());
        assertEquals(Collections.EMPTY_LIST, actual);
    }

    @DisplayName("Create a new lead")
    @Test
    void createNewLead() {
            // given
            NewLeadRequest leadRequest = new NewLeadRequest("other", Type.OTHER, UrgencyLevel.LOW, "other type of lead", null);
            Lead lead = NewLeadRequest.toLead(leadRequest);
            given(leadRepository.save(lead)).willReturn(lead);

            // when
            Lead actual = underTest.createNewLead(leadRequest);

            // then
            verify(leadRepository, times(1)).save(lead);
            assertEquals(leadRequest.title(), actual.getTitle());
            assertEquals(leadRequest.type(), actual.getType());
            assertEquals(leadRequest.urgencyLevel(), actual.getUrgencyLevel());
            assertEquals(leadRequest.description(), actual.getDescription());
    }

    @DisplayName("Find by id should return lead")
    @Test
    void findLeadByIdShouldReturnResult() {
        // given
        Lead lead = Lead.builder()
                        .id(1L)
                        .title("support")
                        .type(Type.SUPPORT)
                        .urgencyLevel(UrgencyLevel.LOW)
                        .description("support")
                        .build();
        given(leadRepository.findById(1L)).willReturn(Optional.of(lead));
        // when
        LeadResponse actual = underTest.getLeadById(1L);
        // then
        LeadResponse expected = LeadResponse.toLeadResponse(lead);
        assertEquals(expected, actual);
    }

    @DisplayName("Find by id should return exception")
    @Test
    void findLeadByIdShouldThrowException() {
        // given
        given(leadRepository.findById(2L)).willReturn(Optional.empty());
        // when
        assertThrows(ResourceNotFoundException.class, () -> underTest.getLeadById(2L));
    }
}