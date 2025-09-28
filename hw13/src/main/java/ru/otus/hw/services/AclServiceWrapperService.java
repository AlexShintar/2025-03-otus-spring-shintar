package ru.otus.hw.services;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public interface AclServiceWrapperService {
    void createAcl(Object object);

    void addPermission(Object object, Sid recipient, Permission permission);

    void deleteAcl(Object object);
}
