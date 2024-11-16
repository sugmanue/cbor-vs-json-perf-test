package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class RndValue {
    private static final Random RANDOM = new Random();

    private static final int[] EMOJI_RANGES = {
            0x1F600, 0x1F64F, // Emoticons
            0x1F300, 0x1F5FF, // Symbols and pictographs
            0x1F680, 0x1F6FF, // Transport and map symbols
            0x1F700, 0x1F77F, // Alchemical symbols
            0x1F900, 0x1F9FF  // Supplemental symbols and pictographs
    };

    private static final int[] CJK_RANGES = {
            0x4E00, 0x9FFF,  // CJK Unified Ideographs
            0x3400, 0x4DBF   // CJK Unified Ideographs Extension A
    };

    public enum Flavor {
        MIXED {
            @Override
            Function<Size, String> generator() {
                return new Function<Size, String>() {
                    private int idx = 0;
                    private final Flavor[] nonMixed = Arrays.stream(values()).filter(x -> x != MIXED).toArray(Flavor[]::new);

                    @Override
                    public String apply(Size size) {
                        return nonMixed[idx++ % nonMixed.length].generator().apply(size);
                    }
                };
            }
        },

        CJK {
            @Override
            Function<Size, String> generator() {
                return (size) -> generateCJKString(RANDOM.nextInt(size.max - size.min) + size.min);
            }
        },

        EMOJI {
            @Override
            Function<Size, String> generator() {
                return (size) -> generateEmojiString(RANDOM.nextInt(size.max - size.min) + size.min);
            }
        },

        ASCII_PRINTABLE {
            @Override
            Function<Size, String> generator() {
                return (size) -> generatePrintableASCIIString(RANDOM.nextInt(size.max - size.min) + size.min);
            }
        },

        FULL_ASCII {
            @Override
            Function<Size, String> generator() {
                return (size) -> generateFullASCIIString(RANDOM.nextInt(size.max - size.min) + size.min);
            }
        };

        abstract Function<Size, String> generator();
    }

    public enum Size {
        SMALL(7, 13),
        MEDIUM(89, 131),
        LARGE(193, 231);

        private final int min;
        private final int max;

        Size(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }


    public static MyValue2 rndValue(Size size, Flavor flavor) {
        Function<Size, String> generator = flavor.generator();
        MyValue2 myValue2 = new MyValue2();
        myValue2.value0 = generator.apply(size);
        myValue2.value1 = generator.apply(size);
        myValue2.value2 = generator.apply(size);
        myValue2.value3 = generator.apply(size);
        myValue2.value4 = generator.apply(size);
        return myValue2;
    }


    public static String generateCJKString(int length) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int rangeIndex = RANDOM.nextInt(CJK_RANGES.length / 2) * 2;
            int start = CJK_RANGES[rangeIndex];
            int end = CJK_RANGES[rangeIndex + 1];
            int codePoint = start + RANDOM.nextInt(end - start + 1);
            stringBuilder.append(Character.toChars(codePoint));
        }

        return stringBuilder.toString();
    }

    public static String generateEmojiString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int rangeIndex = RANDOM.nextInt(EMOJI_RANGES.length / 2) * 2;
            int start = EMOJI_RANGES[rangeIndex];
            int end = EMOJI_RANGES[rangeIndex + 1];
            int codePoint = start + RANDOM.nextInt(end - start + 1);
            stringBuilder.append(Character.toChars(codePoint));
        }

        return stringBuilder.toString();
    }

    public static String generatePrintableASCIIString(int length) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int codePoint = 32 + RANDOM.nextInt(126 - 32 + 1);
            // avoid escapes
            if (codePoint == '\\') {
                codePoint = 'a';
            } else if (codePoint == '"') {
                codePoint = 'b';
            }
            stringBuilder.append((char) codePoint);
        }

        return stringBuilder.toString();
    }


    public static String generateFullASCIIString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int codePoint = RANDOM.nextInt(128);
            stringBuilder.append((char) codePoint);
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        for (Size size : Size.values()) {
            for (Flavor flavor : Flavor.values()) {
                try (PrintWriter out = new PrintWriter(String.format("%s-%s.json", size, flavor))) {
                    MyValue2 myValue2 = rndValue(size, flavor);
                    out.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(myValue2));
                }
            }
        }
    }
}
