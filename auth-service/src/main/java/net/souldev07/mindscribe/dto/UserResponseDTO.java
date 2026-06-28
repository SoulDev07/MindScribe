package net.souldev07.mindscribe.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private List<String> roles;
    private boolean sentimentAnalysis;
}
