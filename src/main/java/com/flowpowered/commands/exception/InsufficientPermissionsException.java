package com.flowpowered.commands.exception;

public class InsufficientPermissionsException extends UserFriendlyCommandException {
    private static final long serialVersionUID = -2092374443452950609L;
    private final String permission;

    public InsufficientPermissionsException(String message, String permission) {
        super(message);
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }

}
