package com.amlakie.usermanagment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureResponse {
    private String role;
    private String name;
    private String signature;
    private String date;
}