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
public class PullRequest {
    private String uuid;
    private Long id;
    private Integer number;
    private String title;
}
