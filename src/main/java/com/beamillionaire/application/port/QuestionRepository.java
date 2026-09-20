package com.beamillionaire.application.port;

import com.beamillionaire.domain.QuestionBank;
import java.io.IOException;

@FunctionalInterface
public interface QuestionRepository {
    QuestionBank load() throws IOException;
}
