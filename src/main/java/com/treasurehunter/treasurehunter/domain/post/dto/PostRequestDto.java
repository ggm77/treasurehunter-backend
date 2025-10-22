package com.treasurehunter.treasurehunter.domain.post.dto;

import com.treasurehunter.treasurehunter.global.validation.Create;
import com.treasurehunter.treasurehunter.global.validation.Update;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostRequestDto {

    @NotNull(groups = Create.class)
    private String title;

    @NotNull(groups = Create.class)
    private String content;

    @NotNull(groups = Create.class)
    private String type;

    //비어도 됨
    private List<String> images;

    @NotNull(groups = Create.class)
    @Min(value = 0, groups = { Create.class, Update.class })
    private Integer setPoint;

    @NotNull(groups = Create.class)
    private String itemCategory;

    @NotNull(groups = Create.class)
    private BigDecimal lat;

    @NotNull(groups = Create.class)
    private BigDecimal lon;

    @NotNull(groups = Create.class)
    private LocalDateTime lostAt;

    @NotNull(groups = Create.class)
    private Boolean isAnonymous;
}
