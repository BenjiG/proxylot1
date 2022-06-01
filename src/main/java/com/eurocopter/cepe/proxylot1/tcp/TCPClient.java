package com.eurocopter.cepe.proxylot1.tcp;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
@Component
public class TCPClient {

    @Value("${cepe.ipLot1}")
    String hostname;

    @Value("${cepe.portLot1}")
    int port;

    public List<String> connectLot1AndSendReceiveMessage(final String message) {
        try (final Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(message);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;

            final List<String> response = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                response.add(line);
                if (line.contains("FINISHED") || line.contains("ERROR") || line.contains("PAS COMPRIS")) {
                    return response;
                }
            }
            return response;
        } catch (final UnknownHostException ex) {
            log.error("Server not found: " + ex.getMessage());
        } catch (final IOException ex) {
            log.error("I/O error: " + ex.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean testConnectionLot1() {
        try (final Socket socket = new Socket(hostname, port)) {
            log.info("Lot 1 connection : success / " + socket);
            return true;
        } catch (final UnknownHostException ex) {
            log.error("Server not found: " + ex.getMessage());
        } catch (final IOException ex) {
            log.error("I/O error: " + ex.getMessage());
        }
        return false;
    }

}
