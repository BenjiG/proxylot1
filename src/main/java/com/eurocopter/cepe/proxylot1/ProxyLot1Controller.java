package com.eurocopter.cepe.proxylot1;

import com.eurocopter.cepe.proxylot1.api.common.StatusDto;
import com.eurocopter.cepe.proxylot1.api.common.StatusEnum;
import com.eurocopter.cepe.proxylot1.api.data.MessageDto;
import com.eurocopter.cepe.proxylot1.parser.TrameParser;
import com.eurocopter.cepe.proxylot1.tcp.TCPClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/lot1")
public class ProxyLot1Controller {

    private final TCPClient client;
    private final TrameParser parser;

    @GetMapping
    public List<MessageDto> send(@RequestParam final String message) {
        final List<String> response = client.connectLot1AndSendReceiveMessage(message);
        return response.stream().map(parser::parse).collect(Collectors.toList());
    }

    @GetMapping("/test")
    public StatusDto test() {
        final var isOk = client.testConnectionLot1();
        return new StatusDto(isOk ? StatusEnum.OK : StatusEnum.KO);
    }

}
