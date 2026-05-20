package com.rent.util;

public enum MigrationResult {
    NO_LEGACY_DB,
    NO_BUSINESS_DATA,
    ALREADY_MIGRATED_OR_ADMIN_DB_NOT_EMPTY,
    MIGRATED,
    FAILED
}