package com.fileprocessingservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class UserRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    public boolean isValid() {
        return username != null && !username.isEmpty() &&
                email != null && !email.isEmpty() &&
                firstName != null && !firstName.isEmpty() &&
                lastName != null && !lastName.isEmpty();
    }
}
