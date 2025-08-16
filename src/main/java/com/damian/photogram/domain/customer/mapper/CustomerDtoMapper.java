package com.damian.photogram.domain.customer.mapper;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.customer.dto.response.CustomerDto;
import com.damian.photogram.domain.customer.dto.response.CustomerWithAllDataDto;
import com.damian.photogram.domain.customer.dto.response.CustomerWithProfileDto;
import com.damian.photogram.domain.customer.dto.response.ProfileDto;
import com.damian.photogram.domain.customer.exception.ProfileNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public class CustomerDtoMapper {
    public static CustomerDto toCustomerDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerWithProfileDto toCustomerWithProfileDto(Customer customer) {
        return new CustomerWithProfileDto(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                ProfileDtoMapper.toProfileDTO(customer.getProfile()),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerWithAllDataDto toCustomerWithAllDataDto(Customer customer) {
        ProfileDto profileDTO = Optional.of(ProfileDtoMapper.toProfileDTO(customer.getProfile()))
                                        .orElseThrow(() -> new ProfileNotFoundException(
                                                Exceptions.PROFILE.NOT_FOUND));

        return new CustomerWithAllDataDto(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                profileDTO,
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static List<CustomerDto> toCustomerDtoList(List<Customer> customers) {
        return customers
                .stream()
                .map(
                        CustomerDtoMapper::toCustomerDto
                ).toList();
    }

    public static Page<CustomerDto> toCustomerDtoWithPagination(Page<Customer> customers) {
        return customers.map(
                CustomerDtoMapper::toCustomerDto
        );
    }
}
