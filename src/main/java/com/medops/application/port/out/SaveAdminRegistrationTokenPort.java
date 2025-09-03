package com.medops.application.port.out;

import com.medops.domain.model.Admin;
import com.medops.domain.model.AdminRegistrationToken;

public interface SaveAdminRegistrationTokenPort {
    String createAdminRegistrationToken(Admin admin);
    void updateAdminRegistrationToken(AdminRegistrationToken adminRegistrationToken);
}
