package dev.jsinco.gringotts.obj;

import lombok.AllArgsConstructor;

import java.util.UUID;

// A snapshot of a vault with reduced information, used for listing or quick access
@AllArgsConstructor
public class SnapshotVault {

    private final UUID owner;
    private final int id;
    private final String customName;

}
