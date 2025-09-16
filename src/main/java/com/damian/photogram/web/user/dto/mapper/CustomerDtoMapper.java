package com.damian.photogram.web.user.dto.mapper;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.web.user.dto.response.CustomerDto;
import com.damian.photogram.web.user.dto.response.CustomerWithAllDataDto;
import com.damian.photogram.web.user.dto.response.CustomerWithProfileDto;
import com.damian.photogram.web.user.dto.response.ProfileDto;
import com.damian.photogram.domain.user.exception.ProfileNotFoundException;
import com.damian.photogram.domain.user.model.Customer;
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
                ProfileDtoMapper.toProfileDto(customer.getProfile()),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerWithAllDataDto toCustomerWithAllDataDto(Customer customer) {
        ProfileDto profileDTO = Optional.of(ProfileDtoMapper.toProfileDto(customer.getProfile()))
                                        .orElseThrow(
                                                () -> new ProfileNotFoundException(
                                                        Exceptions.CUSTOMER.PROFILE.NOT_FOUND,
                                                        customer.getProfile().getId(),
                                                        customer.getId()
                                                )
                                        );

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
