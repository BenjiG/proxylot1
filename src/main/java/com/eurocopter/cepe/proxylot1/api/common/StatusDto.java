package com.eurocopter.cepe.proxylot1.api.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class StatusDto implements Serializable {

    private final StatusEnum status;

}
