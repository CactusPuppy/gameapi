package com.github.cactuspuppy.gameapi;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.github.cactuspuppy.gameapi.game.Game;
import com.github.cactuspuppy.gameapi.game.GameRegistrar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameAPITest {
    private static ServerMock serverMock;
    private static GameAPI testAPI;

    @BeforeAll
    static void setupClass() {
        serverMock = MockBukkit.mock();
        testAPI = MockBukkit.load(GameAPI.class);
    }

    @AfterAll
    static void tearDownClass() {
        MockBukkit.unmock();
    }

    @BeforeEach
    void setup() {
        GameAPI.setGame(null);
        GameAPI.setLobbyState(LobbyState.LOBBY);
    }

    @AfterEach
    void tearDown() {
        GameRegistrar.clearGames();
    }

    /*
    TESTS
     */

    @Test
    void testFailToStart() {
        assertFalse(GameAPI.startGame());
        assertEquals(LobbyState.LOBBY, GameAPI.getLobbyState());
    }

    @Test
    void testNullGameReset() {
        GameAPI.setLobbyState(LobbyState.GAME);
        assertTrue(GameAPI.resetGame());
        assertEquals(LobbyState.LOBBY, GameAPI.getLobbyState());
    }

    @Test
    void registerAndLoad() {
        Game mock = mockGame();
        assertTrue(GameRegistrar.registerGame(mock));
        assertNotNull(GameRegistrar.getGame("mockGame"));
        assertNull(GameAPI.getGame());
        assertTrue(GameAPI.loadGame("mockGame"));
        assertEquals(mock, GameAPI.getGame());

        //Verify function calls
        verify(mock, times(2)).name(); // Registration + loadGame verification
        verify(mock).load(); // Loading
    }

    @Test
    void startGame() {
        Game mock = mockGame();
        assertTrue(GameRegistrar.registerGame(mock));
        assertTrue(GameAPI.loadGame("mockGame"));
        assertTrue(GameAPI.startGame());
        assertEquals(LobbyState.GAME, GameAPI.getLobbyState());

        verify(mock).start();
    }

    private Game mockGame() {
        Game mockedGame = mock(Game.class);
        when(mockedGame.name()).thenReturn("mockGame");
        when(mockedGame.load()).thenReturn(true);
        when(mockedGame.loadSave(Mockito.any(BufferedReader.class))).thenReturn(true);
        when(mockedGame.save(Mockito.any(BufferedWriter.class))).thenReturn(true);
        return mockedGame;
    }
}