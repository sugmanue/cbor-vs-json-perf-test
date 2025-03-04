package com.example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private final ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());
    private final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    private final String source;
    private final byte[] cborData;

    public Main() throws IOException {
        //String size = "X_LARGE";
        //String size = "MEDIUM";
        //String size = "XX_LARGE";
        //String flavor = "ASCII_PRINTABLE";
        RndValue.Size size = RndValue.Size.X_LARGE;
        RndValue.Flavor flavor = RndValue.Flavor.ASCII_PRINTABLE;
        this.source = new String(Files.readAllBytes(Paths.get(String.format("%s-%s.json", size, flavor))),
                StandardCharsets.UTF_8);
        MyValue2 value = jsonMapper.readValue(source, MyValue2.class);
        byte[] bytes = readCborFile(size, flavor);
        if (bytes != null) {
            this.cborData = bytes;
        } else {
            this.cborData = this.cborMapper.writeValueAsBytes(value);
        }
    }

    private static byte[] readCborFile(RndValue.Size size, RndValue.Flavor flavor) {
        try {
            return Files.readAllBytes(Paths.get(String.format("%s-%s.cbor", size, flavor)));
        } catch (IOException e) {
            return null;
        }
    }

    public void run() throws IOException {
        MyValue2 value = this.cborMapper.readValue(this.cborData, MyValue2.class);

        System.out.printf("== hash: %s, value0.len: %d\n", value.hashCode(), value.value0.length());
    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.run();
    }
}
