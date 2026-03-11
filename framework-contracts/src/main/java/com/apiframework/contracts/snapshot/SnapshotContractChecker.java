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
