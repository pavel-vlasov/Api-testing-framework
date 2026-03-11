package com.apiframework.contracts.snapshot;

import com.apiframework.contracts.JsonUnitContractAssertions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SnapshotContractChecker {
    private final Path snapshotsRoot;

    public SnapshotContractChecker(Path snapshotsRoot) {
        this.snapshotsRoot = snapshotsRoot;
    }

    public static SnapshotContractChecker defaultChecker() {
        return new SnapshotContractChecker(Path.of("framework-contracts", "src", "main", "resources", "snapshots"));
    }

    /**
     * Creates a checker that resolves snapshots relative to the Gradle root project directory.
     * Walks up from user.dir until it finds settings.gradle.kts, then resolves the standard
     * snapshot path from there. Use this from test suites whose working directory differs
     * from the root (e.g. test-suites/tests-integration/).
     */
    public static SnapshotContractChecker fromRootDir() {
        Path current = Path.of(System.getProperty("user.dir"));
        while (current != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.getParent();
        }
        if (current == null) {
            return defaultChecker();
        }
        return new SnapshotContractChecker(
            current.resolve("framework-contracts").resolve("src")
                .resolve("main").resolve("resources").resolve("snapshots"));
    }

    public void assertMatchesSnapshot(String snapshotName, String actualJson, boolean updateSnapshots) {
        Path snapshotPath = snapshotsRoot.resolve(snapshotName + ".json");

        try {
            Files.createDirectories(snapshotPath.getParent());

            if (Files.notExists(snapshotPath)) {
                if (updateSnapshots) {
                    Files.writeString(snapshotPath, actualJson, StandardCharsets.UTF_8);
                    return;
                }
                throw new AssertionError("Snapshot not found: " + snapshotPath);
            }

            if (updateSnapshots) {
                Files.writeString(snapshotPath, actualJson, StandardCharsets.UTF_8);
                return;
            }

            String expectedJson = Files.readString(snapshotPath, StandardCharsets.UTF_8);
            JsonUnitContractAssertions.assertJsonEquals(actualJson, expectedJson);
        } catch (AssertionError assertionError) {
            throw assertionError;
        } catch (IOException ioException) {
            throw new IllegalStateException("Unable to work with snapshot file", ioException);
        }
    }
}
