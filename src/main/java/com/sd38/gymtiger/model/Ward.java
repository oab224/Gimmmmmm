package com.sd38.gymtiger.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ward {

    private String WardCode;

    private String WardName;

    private List<String> NameExtension;
}
