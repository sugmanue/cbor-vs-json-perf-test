/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
public class MyBenchmark {
    private static final ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());
    private static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());

    //@Benchmark
    public MyValue2 json(TestCase testCase) throws IOException {
        return jsonMapper.readValue(testCase.jsonData, MyValue2.class);
    }

    @Benchmark
    public MyValue2 cbor(TestCase testCase) throws IOException {
        return cborMapper.readValue(testCase.cborData, MyValue2.class);
    }


    @State(Scope.Thread)
    public static class TestCase {
        @Param({"SMALL", "MEDIUM", "LARGE", "X_LARGE", "XX_LARGE"})
        private RndValue.Size size;
        ;

        @Param({"MIXED", "CJK", "EMOJI", "ASCII_PRINTABLE", "FULL_ASCII"})
        private RndValue.Flavor flavor;

        private byte[] jsonData;
        private byte[] cborData;

        @Setup
        public void setup() throws IOException {
            String source = new String(Files.readAllBytes(Paths.get(String.format("%s-%s.json", size, flavor))), StandardCharsets.UTF_8);
            MyValue2 value = jsonMapper.readValue(source, MyValue2.class);
            jsonData = jsonMapper.writeValueAsBytes(value);
            byte[] cborDataFromFile = readCborFile(size, flavor);
            if (cborDataFromFile != null) {
                cborData = cborDataFromFile;
            } else {
                cborData = cborMapper.writeValueAsBytes(value);
            }
        }
    }


    private static byte[] readCborFile(RndValue.Size size, RndValue.Flavor flavor)  {
        try {
            return Files.readAllBytes(Paths.get(String.format("%s-%s.cbor", size, flavor)));
        } catch (IOException e) {
            return null;
        }
    }

    public static void main(String... args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(MyBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
