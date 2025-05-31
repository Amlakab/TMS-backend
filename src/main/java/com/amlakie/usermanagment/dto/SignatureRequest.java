package com.amlakie.usermanagment.dto;

import lombok.Data;

@Data
public class SignatureRequest {
    private String role;
    private String name;
    private String signature;
    private String date;
}