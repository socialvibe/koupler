package com.monetate.koupler;

import org.apache.commons.cli.*;

/**
 * Main Class, and super class to all the koupler implementations.
 *
 * @author brianoneill
 */
public class Koupler {

    public static void main(String[] args) throws ParseException {
        Options options = new Options();

        options.addOption("http", false, "http mode");

        String propertiesFile = "./conf/kpl.properties";
        options.addOption("propertiesFile", true, "kpl properties file (default: " + propertiesFile + ")");

        int port = 4242;
        options.addOption("port", true, "listening port (default: " + port + ")");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("propertiesFile")) {
            propertiesFile = cmd.getOptionValue("propertiesFile");
        }
        if (cmd.hasOption("port")) {
            port = Integer.parseInt(cmd.getOptionValue("port"));
        }

        HttpKoupler koupler = new HttpKoupler(port, propertiesFile);

        Thread kouplerThread = new Thread(koupler);
        kouplerThread.start();
    }
}
