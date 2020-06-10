package com.github.cactuspuppy.gameapi.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GAPICommandHandlerTest {

    @Test
    public void hasVersion() {
        assertNotNull(GAPICommandHandler.getGAPICommand("version"));
    }

    @Test
    public void testRegisterGAPICommand() {
    }

    @Test
    public void testGetGAPICommand() {
    }
}