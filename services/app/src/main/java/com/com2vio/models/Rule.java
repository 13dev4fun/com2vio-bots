package com.com2vio.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rule {
    private String type;
    private String category;
    private int order;
    private Integer tc;
    private Integer ta;
    private Double tr;
}
