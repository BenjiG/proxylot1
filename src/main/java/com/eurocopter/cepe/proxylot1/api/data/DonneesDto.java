package com.eurocopter.cepe.proxylot1.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class DonneesDto implements Serializable {

    private String name;

    private double mesure;

    private String unit;

}
