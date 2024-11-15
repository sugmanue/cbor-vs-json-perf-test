package com.example;

import java.util.Objects;

public class MyValue2 {
    public String value0;
    public String value1;
    public String value2;
    public String value3;
    public String value4;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyValue2 myValue2 = (MyValue2) o;
        return Objects.equals(value0, myValue2.value0)
                && Objects.equals(value1, myValue2.value1)
                && Objects.equals(value2, myValue2.value2)
                && Objects.equals(value3, myValue2.value3)
                && Objects.equals(value4, myValue2.value4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value0, value1, value2, value3, value4);
    }
}
