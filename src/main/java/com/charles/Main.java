package com.charles;

import com.charles.misc.Config;
import com.charles.network.proxy.IProxy;
import com.charles.network.proxy.ProxyFactory;
import com.charles.ss.CryptFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/7/14.
 */
public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length != 0) {
            startCommand(args);
        } else {
            MainGui.launch(MainGui.class);
        }
    }

    private static void startCommand(String[] args) {
        Config config;
        config = parseArgumengt(args);
        if (config == null) {
            printUsage();
            return;
        }
    }

    private static Config parseArgumengt(String[] args) {
        //args transform to Config
        Config config = new Config();

        if (args.length == 2) {
            if (args[0].equals("--config")) {
                Path path = Paths.get(args[1]);
                try {
                    String json = new String(Files.readAllBytes(path));
                    //use loadFromJson load Json config
                    config.loadFromJson(json);
                } catch (IOException e) {
                    System.out.println("Unable read Config file" + args[1]);
                    return null;
                }
            } else {
                return null;
            }
        }

        if (args.length != 8) {
            //(ip port) * 2, method, password, logLevel, proxyType = 8
            return null;
        }

        for (int i = 0; i < args.length; i += 2) {
            String[] tempArgs;
            if (args[i].equals("--local")) {
                tempArgs = args[i+1].split(":");
                if (tempArgs.length < 2) {
                    System.out.print("Invalid local argument: " + args[i]);
                }
                config.setLocalIpAddress(tempArgs[0]);
                //Integer parseInt:String convert to Int
                config.setLocalPort(Integer.parseInt(tempArgs[1]));
            } else if (args[i].equals("--remote")) {
                tempArgs = args[i+1].split(":");
                if (tempArgs.length < 2) {
                    System.out.print("Invalid remote argument: " + args[i]);
                }
                config.setRemoteIpAddress(tempArgs[0]);
                //Integer parseInt:String convert to Int
                config.setRemotePort(Integer.parseInt(tempArgs[1]));
            } else if (args[i].equals("--cipher")) {
                config.setMethod(args[i+1]);
            } else if (args[i].equals("--password")) {
                config.setPassword(args[i+1]);
            } else if (args[i].equals("--proxy")) {
                config.setProxyType(args[i+1]);
            }
        }
        return config;
    }

    private static void printUsage() {
        System.out.println("Usage: ss --[option] value --[option] value...");
        System.out.println("Option:");
        System.out.println("  --local [IP:PORT]");
        System.out.println("  --remote [IP:PORT]");
        System.out.println("  --cipher [CIPHER_NAME]");
        System.out.println("  --password [PASSWORD]");
        System.out.println("  --config [CONFIG_FILE]");
        System.out.println("  --proxy [TYPE]");
        System.out.println("Support Proxy Type:");
        for (IProxy.TYPE t : ProxyFactory.getSupportedProxyTypes()) {
            System.out.printf(" %s\n", t.toString().toLowerCase());
        }
        System.out.println("Supprot Ciphers");
        for (String s : CryptFactory.getSupportedCiphers()) {
            System.out.printf(" %s\n", s);
        }
        System.out.println("  ss --local \"127.0.0.1:1080\" --remote \"[SS_SERVER_IP]:1080\" --cipher \"aes-256-cfb\" --password \"HelloWorld\"");
        System.out.println("  ss --config config.json");
    }
}
