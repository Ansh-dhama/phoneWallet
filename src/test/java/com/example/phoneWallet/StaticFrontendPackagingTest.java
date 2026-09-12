package com.example.phoneWallet;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StaticFrontendPackagingTest {

    @Test
    void embeddedFrontendContainsIndexAndFavicons() {
        Path staticDir = Path.of("src", "main", "resources", "static");
        assertTrue(Files.isRegularFile(staticDir.resolve("index.html")), "React index.html must be embedded in Spring static resources");
        assertTrue(Files.isRegularFile(staticDir.resolve("favicon.ico")), "favicon.ico must exist so the browser does not hit a missing/error route");
        assertTrue(Files.isRegularFile(staticDir.resolve("favicon.svg")), "favicon.svg must exist");
    }

    @Test
    void embeddedFrontendContainsProductionAssets() throws Exception {
        Path assets = Path.of("src", "main", "resources", "static", "assets");
        assertTrue(Files.isDirectory(assets), "Vite assets directory must exist");
        try (var stream = Files.list(assets)) {
            assertTrue(stream.anyMatch(Files::isRegularFile), "Vite assets directory must not be empty");
        }
    }
}
