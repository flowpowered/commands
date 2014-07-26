package com.flowpowered.commands;

public interface CommandArgumentException {

    public abstract String getCommand();

    public abstract String getInvalidArgName();

    public abstract String getReason();
}