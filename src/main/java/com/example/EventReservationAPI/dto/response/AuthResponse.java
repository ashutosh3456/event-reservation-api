package com.example.EventReservationAPI.dto.response;

import com.example.EventReservationAPI.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private String token;
    private String email;
    private Role role;
}
