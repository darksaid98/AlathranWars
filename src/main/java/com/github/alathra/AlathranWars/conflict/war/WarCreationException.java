package com.github.alathra.alathranwars.conflict.war;

import com.github.alathra.alathranwars.conflict.war.side.SideCreationException;

public class WarCreationException extends SideCreationException {
    public WarCreationException(String errorMessage) {
        super(errorMessage);
    }
}
