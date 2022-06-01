package com.eurocopter.cepe.proxylot1.parser;

import com.eurocopter.cepe.proxylot1.api.data.DonneesDto;
import com.eurocopter.cepe.proxylot1.api.data.MessageDto;
import com.eurocopter.cepe.proxylot1.api.data.TypeMessageEnum;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

@Log4j2
@Component
public class TrameParser {

    private static final String REGEXP_NB_VIRGULE_OU_POINT = "[\\+\\-]?[\\d]*(?:[\\.\\,]+[\\d]*)?";
    private static final String REGEXP_SPACES = "[\\s]*-[\\s]*";
    private static final String REGEXP_NB_VIRGULE = "[\\+\\-]?[\\d]*(?:\\,[\\d]*)";
    private static final String REGEXP_NB_POINT = "[\\+\\-]?[\\d]*(?:\\.[\\d]*)";
    private static final String REGEXP_NB_SANS_DECIMAL = "[\\+\\-]?[\\d]*";

    @SneakyThrows
    public MessageDto parse(String trame) {
        final var message = new MessageDto();

        // Vérification que le message est bien formé
        if (!trame.matches("BEGIN-.*-END[\n\r]*")) {
            log.error("Trame mal formée (begin/end):" + trame);
            throw new ParseException(trame, 0);
        }

        trame = trame.replaceFirst("BEGIN-", "").replaceFirst("-END", "");

        log.debug("Message sans balises: <DEBUT>" + trame + "<FIN>\n");

        try (var scanner = new Scanner(trame)) {
            if (trame.contains("FINISHED_MEASURES")) {
                // Message de type S3 (mesures renvoyees)
                message.setType(TypeMessageEnum.DONNEES_MESURES);
                message.setDate(getDateTime(scanner));

                scanner.skip(REGEXP_SPACES);

                message.setMesures(getMesures(scanner));
            } else if (trame.contains("FINISHED_DELTAI")) {
                // Message de type S6 (mesures de delta I renvoyees)
                message.setType(TypeMessageEnum.DONNEES_DELTA_I);
                message.setDate(getDateTime(scanner));

                scanner.skip(REGEXP_SPACES);

                message.setMesures(getMesures(scanner));
            } else if (trame.contains("FINISHED_UNBALANCE")) {
                // Message de type S6 (mesures de delta I renvoyees)
                message.setType(TypeMessageEnum.DONNEES_BALOURD);
                message.setDate(getDateTime(scanner));

                scanner.skip(REGEXP_SPACES);

                message.setMesures(getMesures(scanner));
            } else if (trame.contains("OK_STARTED")) {
                // Message de type S2 (Confirmation demande de mesures reaues)
                message.setType(TypeMessageEnum.CONFIRMATION_MESURES);
                message.setDate(getDateTime(scanner));

                scanner.skip("[\\s]*WILL_TAKE[\\s]*");

                final var chaineDureeAcq = scanner.findInLine("[\\d]*[sS]");
                final var sDuree = new Scanner(chaineDureeAcq);
                sDuree.useDelimiter("[sS]");
                final var dureeAcq = sDuree.nextInt();
                sDuree.close();

                log.info("Durae acq:" + dureeAcq + " seconde(s)");
                message.setDureeAcq(dureeAcq);
            } else if (trame.contains(".*PAS COMPRIS.*")) {
                log.info("Serveur n'a pas compris le message.");
            } else if (trame.contains("ERROR:")) {
                message.setType(TypeMessageEnum.MSG_ERREUR);

                final var infoErreur = scanner.findInLine(".*");
                message.setErreur(infoErreur);
                log.info("Message d'erreur:" + infoErreur);
            } else {
                message.setType(TypeMessageEnum.MSG_ERREUR);
                message.setErreur(trame);
            }
        }
        return message;
    }

    private LocalDateTime getDateTime(final Scanner scanner) {
        final var date = scanner.findInLine("[\\d]*/[\\d]*/[\\d]*");
        final var scanDate = new Scanner(date);
        scanDate.useDelimiter("/");
        final int jour = scanDate.nextInt();
        final int mois = scanDate.nextInt();
        final int annee = scanDate.nextInt();
        log.debug("Jour:" + jour + "Mois:" + mois + "Annee:" + annee);
        scanDate.close();

        scanner.skip("[\\s]*AT[\\s]*");

        final var time = scanner.findInLine("[\\d]*[hH][\\d]*[mM][nN][\\d]*[sS]");
        final var scanTime = new Scanner(time);
        scanTime.useDelimiter("[hH]|([mM][nN])|[sS]");
        final int heures = scanTime.nextInt();
        final int minutes = scanTime.nextInt();
        final int secondes = scanTime.nextInt();
        log.debug("Heures:" + heures + "Minutes:" + minutes + "Secondes:" + secondes);
        scanTime.close();

        final var localDateTime = LocalDateTime.of(annee, mois, jour, heures, minutes, secondes);
        log.debug("Date:" + localDateTime);
        return localDateTime;
    }

    @SneakyThrows
    private Map<String, DonneesDto> getMesures(final Scanner scanner) {
        final var mesurePattern = Pattern.compile("([^=]*)=(" + REGEXP_NB_VIRGULE_OU_POINT + ")([\\D]*)");
        scanner.useDelimiter(";");

        final Map<String, DonneesDto> jeuMesures = new HashMap<>();
        while (scanner.hasNext()) {
            final var chaineMesure = scanner.next();
            log.debug("Chaine Donnee:" + chaineMesure);
            final var m = mesurePattern.matcher(chaineMesure);
            final boolean matches = m.matches();
            if (!matches) {
                log.error("Donnee incorrecte");
                continue;
            }
            final var mR = m.toMatchResult();

            final var mesure = mR.group(2);
            log.debug("[VALEUR]" + mesure);

            final var nfVirgule = new DecimalFormat();
            nfVirgule.getDecimalFormatSymbols().setDecimalSeparator(',');
            double mesureDouble;
            if (mesure.matches(REGEXP_NB_VIRGULE)) {
                // on utilise la virgule comme saparateur décimal
                mesureDouble = nfVirgule.parse(mesure).doubleValue();
            } else if (mesure.matches(REGEXP_NB_POINT) || mesure.matches(REGEXP_NB_SANS_DECIMAL)) {
                mesureDouble = Double.parseDouble(mesure);
            } else {
                log.error("Erreur PARSER pour la valeur de la mesure.");
                throw new ParseException(mesure, 0);
            }
            final var nomMesure = mR.group(1);
            jeuMesures.put(nomMesure, new DonneesDto(nomMesure, mesureDouble, mR.group(3)));
        }
        return jeuMesures;
    }

}
