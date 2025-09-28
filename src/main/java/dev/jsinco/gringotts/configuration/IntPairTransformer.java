package dev.jsinco.gringotts.configuration;

import dev.jsinco.gringotts.utility.Util;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;


public class IntPairTransformer extends BidirectionalTransformer<String, IntPair> {

    @Override
    public GenericsPair<String, IntPair> getPair() {
        return genericsPair(String.class, IntPair.class);
    }

    @Override
    public IntPair leftToRight(@NotNull String data, @NonNull SerdesContext serdesContext) {
        if (data.contains("-")) {
            String[] split = data.split("-");
            Integer int1 = Util.getInteger(split[0]);
            Integer int2 = Util.getInteger(split[1]);
            if (int1 != null && int2 != null) {
                return new IntPair(int1, int2);
            }
        }
        Integer int1 = Util.getInteger(data);
        if (int1 != null) {
            return new IntPair(int1, int1);
        }
        return null;
    }

    @Override
    public String rightToLeft(@NonNull IntPair data, @NonNull SerdesContext serdesContext) {
        return data.a() + "-" + data.b();
    }

}