package com.eric.store.files.repository;

import com.eric.store.files.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FileRepository extends JpaRepository<FileEntity, String> {
}

