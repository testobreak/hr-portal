package com.acme.hrms.document;

/**
 * Document categories aligned with {@code docs/rbac-matrix.md} §4.
 */
public enum DocumentType {
    PROFILE_PHOTO,
    OFFER_LETTER,
    ID_PROOF,
    OTHER;

    /** HR-only sensitive types; profile photo and generic OTHER are non-restricted. */
    public boolean isRestricted() {
        return this == OFFER_LETTER || this == ID_PROOF;
    }
}
