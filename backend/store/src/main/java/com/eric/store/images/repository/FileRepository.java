package com.eric.store.images.repository;

import com.eric.store.images.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FileRepository extends JpaRepository<FileEntity, String> {
}

