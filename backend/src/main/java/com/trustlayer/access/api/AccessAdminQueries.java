package com.trustlayer.access.api;

import java.util.Optional;
import java.util.UUID;

public interface AccessAdminQueries {

    Optional<AccessSummary> accessFor(UUID userId);
}
