package com.damian.photogram.customers.dto;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.profile.ProfileDTO;
import com.damian.photogram.customers.profile.ProfileDTOMapper;
import com.damian.photogram.customers.profile.exception.ProfileNotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomerDTOMapper {
    public static CustomerDTO toCustomerDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerWithProfileDTO toCustomerWithProfileDTO(Customer customer) {
        return new CustomerWithProfileDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                ProfileDTOMapper.toProfileDTO(customer.getProfile()),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerWithAllDataDTO toCustomerWithAllDataDTO(Customer customer) {
        ProfileDTO profileDTO = Optional.ofNullable(ProfileDTOMapper.toProfileDTO(customer.getProfile()))
                                        .orElseThrow(() -> new ProfileNotFoundException(
                                                Exceptions.PROFILE.NOT_FOUND));


        return new CustomerWithAllDataDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                profileDTO,
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static Set<CustomerNonFriendDTO> apply(Set<Customer> customers) {
        return customers
                .stream()
                .map((customer) ->
                        new CustomerNonFriendDTO(
                                customer.getId(),
                                customer.getProfile().getFirstName(),
                                customer.getProfile().getLastName(),
                                customer.getProfile().getImageFilename()
                        )
                ).collect(Collectors.toSet());
    }

    public static List<CustomerDTO> toCustomerDTOList(List<Customer> customers) {
        return customers
                .stream()
                .map(
                        CustomerDTOMapper::toCustomerDTO
                ).toList();
    }

    public static Page<CustomerDTO> toCustomerDTOPage(Page<Customer> customers) {
        return customers.map(
                CustomerDTOMapper::toCustomerDTO
        );
    }
}
