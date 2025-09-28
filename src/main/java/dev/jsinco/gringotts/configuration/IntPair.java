package dev.jsinco.gringotts.configuration;

import dev.jsinco.gringotts.utility.Couple;

public class IntPair extends Couple<Integer, Integer> {
    public IntPair(Integer first, Integer second) {
        super(first, second);
    }

    public boolean includes(int value) {
        return value >= this.a() && value <= this.b();
    }

    public static IntPair of(int a, int b) {
        return new IntPair(a, b);
    }
}
