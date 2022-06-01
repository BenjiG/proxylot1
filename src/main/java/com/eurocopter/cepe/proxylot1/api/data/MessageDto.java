package com.eurocopter.cepe.proxylot1.api.data;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MessageDto implements Serializable {

    private TypeMessageEnum type;

    private LocalDateTime date;

    private Map<String, DonneesDto> mesures;

    private String erreur;

    private int dureeAcq;

}
