package com.servicehub.servicehubapi.controller;

import com.servicehub.servicehubapi.dto.service.CreateServiceDTO;
import com.servicehub.servicehubapi.dto.service.ResponseServiceDTO;
import com.servicehub.servicehubapi.dto.user.CreateUserDTO;
import com.servicehub.servicehubapi.dto.user.ResponseUserDTO;
import com.servicehub.servicehubapi.model.Role;
import com.servicehub.servicehubapi.model.Service_;
import com.servicehub.servicehubapi.security.UserPrincipal;
import com.servicehub.servicehubapi.service.ServiceService;
import com.servicehub.servicehubapi.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class GraphQLController {

    private final UserService userService;
    private final ServiceService serviceService;

    public GraphQLController(UserService userService, ServiceService serviceService) {
        this.userService = userService;
        this.serviceService = serviceService;
    }

    @QueryMapping
    public List<ResponseUserDTO> users(@Argument int page, @Argument int size) {
        return userService.findAll(PageRequest.of(page, Math.min(size, 50))).getContent();
    }

    @QueryMapping
    public ResponseUserDTO userById(@Argument Long id) {
        return userService.findById(id);
    }

    @QueryMapping
    public List<ResponseServiceDTO> services(@Argument int page, @Argument int size) {
        return serviceService.findAll(PageRequest.of(page, Math.min(size, 50))).getContent();
    }

    @QueryMapping
    public ResponseServiceDTO serviceById(@Argument Long id) {
        return serviceService.findById(id);
    }

    @MutationMapping
    public ResponseUserDTO createUser(@Argument("input") Map<String, Object> input) {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setFullName((String) input.get("fullName"));
        dto.setEmail((String) input.get("email"));
        dto.setPassword((String) input.get("password"));
        dto.setPhone((String) input.get("phone"));
        dto.setBio((String) input.get("bio"));
        dto.setAvatarUrl((String) input.get("avatarUrl"));
        if (input.get("role") != null) {
            dto.setRole(Role.valueOf((String) input.get("role")));
        }
        return userService.createUser(dto);
    }

    @MutationMapping
    public ResponseServiceDTO createService(@Argument("input") Map<String, Object> input,
                                            @AuthenticationPrincipal UserPrincipal actor) {
        CreateServiceDTO dto = new CreateServiceDTO();
        dto.setTitle((String) input.get("title"));
        dto.setDescription((String) input.get("description"));
        Object price = input.get("price");
        if (price instanceof Number n) {
            dto.setPrice(BigDecimal.valueOf(n.doubleValue()));
        }
        dto.setCategory(Service_.ServiceCategory.valueOf((String) input.get("category")));
        if (input.get("provider") != null) {
            dto.setProvider(Long.valueOf(input.get("provider").toString()));
        }
        return serviceService.createService(dto, actor);
    }
}
