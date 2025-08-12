package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AclServiceWrapperServiceImpl implements AclServiceWrapperService {
    private final MutableAclService mutableAclService;

    private final Sid adminSid = new GrantedAuthoritySid("ROLE_ADMIN");

    @Transactional
    @Override
    public void createAcl(Object object) {
        ObjectIdentity oid = new ObjectIdentityImpl(object);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated principal found to assign ACL ownership");
        }
        Sid ownerSid = new PrincipalSid(auth);

        MutableAcl acl = readOrCreateAcl(oid);
        boolean changed = false;

        if (acl.getOwner() == null || !acl.getOwner().equals(ownerSid)) {
            acl.setOwner(ownerSid);
            changed = true;
        }

        changed |= ensureFullControl(acl, ownerSid);
        changed |= ensureFullControl(acl, adminSid);

        if (changed) {
            mutableAclService.updateAcl(acl);
        }
    }

    private boolean ensureFullControl(MutableAcl acl, Sid sid) {
        boolean changed = false;
        changed |= ensureAce(acl, sid, BasePermission.READ);
        changed |= ensureAce(acl, sid, BasePermission.WRITE);
        changed |= ensureAce(acl, sid, BasePermission.DELETE);
        changed |= ensureAce(acl, sid, BasePermission.ADMINISTRATION);
        return changed;
    }

    private boolean ensureAce(MutableAcl acl, Sid sid, Permission permission) {
        boolean exists = acl.getEntries().stream()
                .anyMatch(e -> e.isGranting()
                        && e.getSid().equals(sid)
                        && e.getPermission().equals(permission));
        if (!exists) {
            acl.insertAce(acl.getEntries().size(), permission, sid, true);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public void addPermission(Object object, Sid recipient, Permission permission) {
        ObjectIdentity oid = new ObjectIdentityImpl(object);
        final MutableAcl acl;
        try {
            acl = (MutableAcl) mutableAclService.readAclById(oid);
        } catch (org.springframework.security.acls.model.NotFoundException e) {
            throw new IllegalStateException("ACL not found for object. Ensure ACL is created before adding permissions");
        }

        if (ensureAce(acl, recipient, permission)) {
            mutableAclService.updateAcl(acl);
        }
    }

    @Transactional
    @Override
    public void deleteAcl(Object object) {
        ObjectIdentity oid = new ObjectIdentityImpl(object);
        mutableAclService.deleteAcl(oid, true);
    }

    private MutableAcl readOrCreateAcl(ObjectIdentity oid) {
        try {
            return (MutableAcl) mutableAclService.readAclById(oid);
        } catch (org.springframework.security.acls.model.NotFoundException e) {
            return mutableAclService.createAcl(oid);
        }
    }
}