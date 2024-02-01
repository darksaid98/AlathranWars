package com.github.alathra.AlathranWars.conflict.war;

import com.github.alathra.AlathranWars.conflict.war.side.SideCreationException;

public class WarCreationException extends SideCreationException {
    public WarCreationException(String errorMessage) {
        super(errorMessage);
    }
}
