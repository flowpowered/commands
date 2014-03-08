package com.flowpowered.commands.arguments;

import java.util.List;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3f;

public class CommandArgumentsDecorator implements CommandArguments {
    private final CommandArguments decorated;

    public CommandArgumentsDecorator(CommandArguments decorated) {
        this.decorated = decorated;
    }

    @Override
    public List<String> get() {
        return decorated.get();
    }

    @Override
    public int length() {
        return decorated.length();
    }

    @Override
    public int getDepth() {
        return decorated.getDepth();
    }

    @Override
    public String getSeparator() {
        return decorated.getSeparator();
    }

    @Override
    public boolean hasMore() {
        return decorated.hasMore();
    }

    @Override
    public int remaining() {
        return decorated.remaining();
    }

    @Override
    public String getUnclosedQuote() {
        return decorated.getUnclosedQuote();
    }

    @Override
    public Vector2i offsetToAbsoluteArgument(int cursor) {
        return decorated.offsetToAbsoluteArgument(cursor);
    }

    @Override
    public Vector2i offsetToArgument(int cursor) {
        return decorated.offsetToArgument(cursor);
    }

    @Override
    public int absoluteArgumentToOffset(Vector2i pos) {
        return decorated.absoluteArgumentToOffset(pos);
    }

    @Override
    public int argumentToOffset(Vector2i pos) {
        return decorated.argumentToOffset(pos);
    }

    @Override
    public CommandFlags flags() {
        return decorated.flags();
    }

    @Override
    public String getPastCommandString() {
        return decorated.getPastCommandString();
    }

    @Override
    public String unescape(String input) {
        return decorated.unescape(input);
    }

    @Override
    public ArgumentParseException failure(String argName, String error, boolean silenceable) {
        return decorated.failure(argName, error, silenceable);
    }

    @Override
    public <T> T success(String argName, T parsedValue) {
        return decorated.success(argName, parsedValue);
    }

    @Override
    public <T> T success(String argName, T parsedValue, boolean fallbackValue) {
        return decorated.success(argName, parsedValue, fallbackValue);
    }

    @Override
    public <T> T potentialDefault(ArgumentParseException e, T def) throws ArgumentParseException {
        return decorated.potentialDefault(e, def);
    }

    @Override
    public String currentArgument(String argName) throws ArgumentParseException {
        return decorated.currentArgument(argName);
    }

    @Override
    public String currentArgument(String argName, boolean ignoreUnclosedQuote) throws ArgumentParseException {
        return decorated.currentArgument(argName, ignoreUnclosedQuote);
    }

    @Override
    public String currentArgument(String argName, boolean ignoreUnclosedQuote, boolean unescape) throws ArgumentParseException {
        return decorated.currentArgument(argName, ignoreUnclosedQuote, unescape);
    }

    @Override
    public boolean setArgOverride(String name, String value) {
        return decorated.setArgOverride(name, value);
    }

    @Override
    public boolean advance() {
        return decorated.advance();
    }

    @Override
    public void assertCompletelyParsed() throws ArgumentParseException {
        decorated.assertCompletelyParsed();
    }

    @Override
    public <T> T pop(String argName, Class<T> type) throws ArgumentParseException {
        return decorated.pop(argName, type);
    }

    @Override
    public <T> T pop(String argName, Class<T> type, T def) throws ArgumentParseException {
        return decorated.pop(argName, type, def);
    }

    @Override
    public String popString(String argName) throws ArgumentParseException {
        return decorated.popString(argName);
    }

    @Override
    public String popString(String argName, String def) throws ArgumentParseException {
        return decorated.popString(argName, def);
    }

    @Override
    public int popInteger(String argName) throws ArgumentParseException {
        return decorated.popInteger(argName);
    }

    @Override
    public int popInteger(String argName, int def) throws ArgumentParseException {
        return decorated.popInteger(argName, def);
    }

    @Override
    public float popFloat(String argName) throws ArgumentParseException {
        return decorated.popFloat(argName);
    }

    @Override
    public float popFloat(String argName, float def) throws ArgumentParseException {
        return decorated.popFloat(argName, def);
    }

    @Override
    public double popDouble(String argName) throws ArgumentParseException {
        return decorated.popDouble(argName);
    }

    @Override
    public double popDouble(String argName, double def) throws ArgumentParseException {
        return decorated.popDouble(argName, def);
    }

    @Override
    public boolean popBoolean(String argName) throws ArgumentParseException {
        return decorated.popBoolean(argName);
    }

    @Override
    public boolean popBoolean(String argName, boolean def) throws ArgumentParseException {
        return decorated.popBoolean(argName, def);
    }

    @Override
    public String popSubCommand() throws ArgumentParseException {
        return decorated.popSubCommand();
    }

    @Override
    public Vector3f popVector3(String argName) throws ArgumentParseException {
        return decorated.popVector3(argName);
    }

    @Override
    public Vector3f popVector3(String argName, Vector3f def) throws ArgumentParseException {
        return decorated.popVector3(argName, def);
    }

    @Override
    public <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass) throws ArgumentParseException {
        return decorated.popEnumValue(argName, enumClass);
    }

    @Override
    public <T extends Enum<T>> T popEnumValue(String argName, Class<T> enumClass, T def) throws ArgumentParseException {
        return decorated.popEnumValue(argName, enumClass, def);
    }

    @Override
    public String popRemainingStrings(String argName) throws ArgumentParseException {
        return decorated.popRemainingStrings(argName);
    }

    @Override
    public String popRemainingStrings(String argName, String def) throws ArgumentParseException {
        return decorated.popRemainingStrings(argName, def);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return decorated.get(key, type);
    }

    @Override
    public <T> T get(String key, Class<T> type, T def) {
        return decorated.get(key, type, def);
    }

    @Override
    public boolean has(String key) {
        return decorated.has(key);
    }

    @Override
    public boolean hasOverride(String key) {
        return decorated.hasOverride(key);
    }

    @Override
    public String getString(String key) {
        return decorated.getString(key);
    }

    @Override
    public String getString(String key, String def) {
        return decorated.getString(key, def);
    }

    @Override
    public int getInteger(String key, int def) {
        return decorated.getInteger(key, def);
    }

    @Override
    public float getFloat(String key, float def) {
        return decorated.getFloat(key, def);
    }

    @Override
    public String[] toArray() {
        return decorated.toArray();
    }

    @Override
    public String toString() {
        return decorated.toString();
    }

}
