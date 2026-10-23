package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataOverViewQueryDTO implements Serializable {

    //开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate begin;

    //结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;

}
