package com.example.instructions.repo;

import com.example.instructions.domain.MediaAsset;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий медиа-активов.
 */
public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
}
