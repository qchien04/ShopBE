package com.mapper;

import com.DTO.UserAccountDTO;
import com.entity.User;
import com.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {

    @Mapping(
            target = "roles",
            expression = "java(mapRoles(userAccount.getUserRoles()))"
    )
    UserAccountDTO toDto(User userAccount);

    List<UserAccountDTO> toDtos(List<User> users);


    default List<String> mapRoles(Set<UserRole> userRoles) {
        if (userRoles == null) return List.of();

        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .toList();
    }
}
