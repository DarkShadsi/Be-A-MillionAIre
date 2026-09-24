package com.beamillionaire.application.port;

import com.beamillionaire.application.AppPreferences;
import java.io.IOException;

public interface SettingsRepository {
    AppPreferences load() throws IOException;
    void save(AppPreferences preferences) throws IOException;
}
